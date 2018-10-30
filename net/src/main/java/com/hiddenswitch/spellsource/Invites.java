package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.InviteId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.MatchmakingRequest;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.mongo.UpdateOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static io.vertx.core.json.JsonObject.mapFrom;

/**
 * This service allows players to invite each other to private games.
 */
public interface Invites {
	String INVITES = "invites";
	JsonArray PENDING_STATUSES = new JsonArray().add(Invite.StatusEnum.UNDELIVERED.getValue()).add(Invite.StatusEnum.PENDING.getValue());


	static void handleConnections() throws SuspendExecution {
		Connection.connected(suspendableHandler((SuspendableAction1<Connection>) connection -> {
			// Notify recipients of all pending invites.
			List<Invite> invites = mongo().find(INVITES, json("toUserId", connection.userId(), "status", json("$in", PENDING_STATUSES)), Invite.class);
			for (Invite invite : invites) {
				invite.status(Invite.StatusEnum.PENDING);
				connection.write(new Envelope().added(new EnvelopeAdded().invite(invite)));
			}

			// Set undelivered to pending
			JsonArray ids = new JsonArray(invites.stream().map(Invite::getId).collect(Collectors.toList()));
			mongo().updateCollectionWithOptions(INVITES,
					json("_id", json("$in", ids)),
					json("$set",
							json("status", Invite.StatusEnum.PENDING.getValue())),
					new UpdateOptions().setMulti(true));
		}));
	}

	static InviteResponse invite(InvitePostRequest request, UserRecord user) throws SuspendExecution {
		UserRecord toUser;
		if (request.getToUserId() != null) {
			// Check if the other player is a friend
			toUser = Accounts.get(request.getToUserId());
			// Throws null pointer exception and will be handled above
			if (!toUser.isFriend(user.getId())) {
				throw new SecurityException("Not friends");
			}
		} else if (request.getToUserNameWithToken() != null) {
			try {
				String[] tokens = request.getToUserNameWithToken().split("#");
				toUser = mongo().findOne(Accounts.USERS, json("username", tokens[0], "privacyToken", tokens[1]), UserRecord.class);
				// Throws null pointer exception, should be handled in parent
				// Users found this way do not need to be friends
			} catch (IndexOutOfBoundsException ex) {
				throw new RuntimeException("Invalid token and username specification. Should look like username#1234");
			}
		} else {
			throw new IllegalArgumentException("No user ID or username with token specified.");
		}

		InviteId inviteId = InviteId.create();

		// Configure expiration
		Calendar expiryTime = Calendar.getInstance();
		expiryTime.setTime(new Date());
		expiryTime.add(Calendar.MINUTE, 15);

		Vertx vertx = Vertx.currentContext().owner();

		// Set timer to expire the invite after 15 minutes
		vertx.setTimer(15 * 60 * 1000L, suspendableHandler(timerId -> {
			// If the invite hasn't been acted on, expire it
			mongo().updateCollection(INVITES,
					json("_id", inviteId.toString(), "status", json("$in", PENDING_STATUSES)),
					json("$set", json("status", Invite.StatusEnum.TIMEOUT.getValue())));
		}));

		Invite invite = new Invite()
				.id(inviteId.toString())
				.fromName(user.getUsername())
				.fromUserId(user.getId())
				.toUserId(toUser.getId())
				.message(request.getMessage())
				.expiresAt(expiryTime.getTimeInMillis())
				.status(Invite.StatusEnum.UNDELIVERED);

		if (request.isFriend() != null) {
			// This is a friend request
			invite.setFriendId(user.getId());
		} else if (request.getQueueId() != null) {
			// This is a matchmaking queue request
			// Create a new queue just for this invite.
			// Right now, anyone can wait in any queue, but this is probably the most convenient.
			String customQueueId = request.getQueueId() + "-" + inviteId;
			invite.queueId(customQueueId);
		}

		mongo().insert(INVITES, mapFrom(invite));

		updateInvite(invite);

		return new InviteResponse()
				.invite(invite);
	}

	static void updateInvite(@NotNull Invite invite) throws SuspendExecution {
		// Notify both users of the new invite, but only wait to see if the recipient is around to actually receive it right
		// now. We'll update the record immediately and only insert it into the db with the proper status
		WriteStream<Envelope> toUserConnection = Connection.writeStream(invite.getToUserId());
		if (toUserConnection != null) {
			if (invite.getStatus() == Invite.StatusEnum.UNDELIVERED) {
				invite.status(Invite.StatusEnum.PENDING);
			}
			toUserConnection.write(
					new Envelope().added(new EnvelopeAdded().invite(invite))
			);
		}

		// The sender can receive this invite at any time through the channel since they will receive it in their post
		// response also.
		Connection.writeStream(invite.getFromUserId(), res -> {
			WriteStream<Envelope> conn = res.result();
			if (conn == null) {
				return;
			}

			conn.write(
					new Envelope().added(new EnvelopeAdded().invite(invite))
			);
		});
	}

	/**
	 * Accepts an invite. If it's a matchmaking invitation, the user will automatically be put into a matchmaking queue
	 * and will wait.
	 *
	 * @param inviteId The invite to accept
	 * @param request  The request
	 * @param user     The user executing the request
	 * @return An updated invite and potentially information about the new friend or game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static @NotNull
	AcceptInviteResponse accept(@NotNull InviteId inviteId, @NotNull AcceptInviteRequest request, @NotNull UserRecord user) throws SuspendExecution, InterruptedException {
		Invite invite = mongo().findOne(INVITES, json("_id", inviteId.toString()), Invite.class);
		if (invite == null) {
			throw new NullPointerException(String.format("Invite not found: %s", inviteId));
		}
		if (!invite.getToUserId().equals(user.getId())) {
			throw new SecurityException("You did not have access to change this invite.");
		}
		switch (invite.getStatus()) {
			case PENDING:
			case UNDELIVERED:
				invite.setStatus(Invite.StatusEnum.ACCEPTED);
				break;
			case ACCEPTED:
				throw new IllegalStateException("The invite was already accepted.");
			case TIMEOUT:
				throw new IllegalStateException("The invite timed out.");
			case REJECTED:
				throw new IllegalStateException("The invite was rejected.");
			case CANCELLED:
				throw new IllegalStateException("The invite was canceled.");
		}

		mongo().updateCollection(INVITES, json("_id", invite.getId()), json("$set", json("status", Invite.StatusEnum.ACCEPTED.getValue())));
		updateInvite(invite);
		AcceptInviteResponse res = new AcceptInviteResponse();
		if (invite.getFriendId() != null) {
			// Make them friends
			res.friend(Friends.putFriend(user, new FriendPutRequest().friendId(invite.getFriendId())));
		}
		if (invite.getQueueId() != null) {
			// Enqueue the player automatically
//			try {
			Matchmaking.enqueue(new MatchmakingRequest(request.getMatch(), user.getId()));
//
//				res.match(new MatchmakingQueuePutResponse().unityConnection(new MatchmakingQueuePutResponseUnityConnection()));
//			} catch (InterruptedException ex) {
//				mongo().updateCollection(INVITES, json("_id", invite.getId()), json("$set", json("status", Invite.StatusEnum.REJECTED.getValue())));
//				updateInvite(invite);
//				throw new IllegalStateException("Matchmaking was canceled, so the invite was rejected by the recipient.");
//			}
		}
		return res;
	}

	static @NotNull
	InviteResponse deleteInvite(@NotNull InviteId inviteId, @NotNull UserRecord user) throws SuspendExecution {
		Invite invite = mongo().findOne(INVITES, json("_id", inviteId.toString()), Invite.class);

		if (invite == null) {
			throw new NullPointerException(String.format("Invite not found: %s", inviteId));
		}

		boolean isSender = invite.getFromUserId().equals(user.getId());
		boolean isRecipient = invite.getToUserId().equals(user.getId());
		if (!isSender && !isRecipient) {
			throw new SecurityException("You did not have access to change this invite.");
		}

		Invite.StatusEnum status = isSender ? Invite.StatusEnum.CANCELLED : Invite.StatusEnum.REJECTED;
		switch (invite.getStatus()) {
			case PENDING:
			case UNDELIVERED:
				invite.setStatus(status);
				break;
			case ACCEPTED:
				throw new IllegalStateException("The invite was already accepted.");
			case TIMEOUT:
				throw new IllegalStateException("The invite timed out.");
			case REJECTED:
				throw new IllegalStateException("The invite was rejected.");
			case CANCELLED:
				throw new IllegalStateException("The invite was already canceled.");
		}

		mongo().updateCollection(INVITES, json("_id", invite.getId()), json("$set", json("status", status)));

		updateInvite(invite);
		return new InviteResponse().invite(invite);
	}
}
