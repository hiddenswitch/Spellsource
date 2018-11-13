package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.Invite.StatusEnum;
import com.hiddenswitch.spellsource.impl.InviteId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.MatchmakingRequest;
import com.hiddenswitch.spellsource.util.MatchmakingQueueConfiguration;
import io.vertx.core.Closeable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.lang3.RandomStringUtils;
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
	JsonArray PENDING_STATUSES = new JsonArray().add(StatusEnum.UNDELIVERED.getValue()).add(StatusEnum.PENDING.getValue());


	static void handleConnections() throws SuspendExecution {
		Connection.connected(suspendableHandler((SuspendableAction1<Connection>) connection -> {
			// Notify recipients of all pending invites.
			List<Invite> invites = mongo().find(INVITES, json("toUserId", connection.userId(), "status", json("$in", PENDING_STATUSES)), Invite.class);
			for (Invite invite : invites) {
				if (invite.getStatus() == StatusEnum.UNDELIVERED) {
					invite.status(StatusEnum.PENDING);
				}

				connection.write(new Envelope().added(new EnvelopeAdded().invite(invite)));
			}

			// Set undelivered to pending
			JsonArray ids = new JsonArray(invites.stream().map(Invite::getId).collect(Collectors.toList()));
			// State may have changed in between, only mess with the undelivered/pending ones
			mongo().updateCollectionWithOptions(INVITES,
					json("_id", json("$in", ids), "status", json("$in", PENDING_STATUSES)),
					json("$set",
							json("status", StatusEnum.PENDING.getValue())),
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
					json("$set", json("status", StatusEnum.TIMEOUT.getValue())));
		}));

		Invite invite = new Invite()
				.id(inviteId.toString())
				.fromName(user.getUsername())
				.fromUserId(user.getId())
				.toUserId(toUser.getId())
				.message(request.getMessage())
				.expiresAt(expiryTime.getTimeInMillis())
				.status(StatusEnum.UNDELIVERED);

		if (request.isFriend() != null) {
			// This is a friend request
			invite.setFriendId(user.getId());
		} else if (request.getQueueId() != null) {
			// This is a matchmaking queue request
			// Create a new queue just for this invite.
			request.setQueueId(RandomStringUtils.randomAlphanumeric(10));
			// Right now, anyone can wait in any queue, but this is probably the most convenient.
			// TODO: Destroy the user's missing queues
			String customQueueId = user.getUsername() + "-" + inviteId + "-" + request.getQueueId();
			invite.queueId(customQueueId);

			// The matchmaker will close itself automatically if no one joins after 4s or the
			Closeable matchmaker = Matchmaking.startMatchmaker(customQueueId, new MatchmakingQueueConfiguration()
					.setAwaitingLobbyTimeout(30000L)
					.setBotOpponent(false)
					.setEmptyLobbyTimeout(4000L)
					.setLobbySize(2)
					.setName(String.format("%s's private match", user.getUsername()))
					.setOnce(true)
					.setPrivateLobby(true)
					.setRanked(false)
					.setStillConnectedTimeout(2000L)
					.setStartsAutomatically(true)
					.setRules(new CardDesc[0]));

			// A user that starts a private lobby queues automatically in this method if their request includes a deckId
			if (request.getDeckId() != null) {
				Matchmaking.enqueue(new MatchmakingRequest()
						.setQueueId(customQueueId)
						.withUserId(user.getId())
						.withDeckId(request.getDeckId()));
			}
		}

		mongo().insert(INVITES, mapFrom(invite));

		sendInvite(invite);

		return new InviteResponse()
				.invite(invite);
	}

	/**
	 * Sends the invite to both users on it across the {@link Connection}.
	 * <p>
	 * Mutates the incoming invite if it's {@link StatusEnum#UNDELIVERED}.
	 *
	 * @param invite
	 * @throws SuspendExecution
	 */
	static void sendInvite(@NotNull Invite invite) throws SuspendExecution {
		// Notify both users of the new invite, but only wait to see if the recipient is around to actually receive it right
		// now. We'll update the record immediately and only insert it into the db with the proper status
		WriteStream<Envelope> toUserConnection = Connection.writeStream(invite.getToUserId());
		if (toUserConnection != null) {
			if (invite.getStatus() == StatusEnum.UNDELIVERED) {
				invite.status(StatusEnum.PENDING);
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
	 * Accepts an invite.
	 * <p>
	 * <ul>
	 * <li>If it's a matchmaking invitation, the user will automatically be put into a matchmaking queue and will
	 * wait.</li>
	 * <li>If it's a friend invitation, the user will do a mutual friend adding.</li>
	 * </ul>
	 *
	 * @param inviteId  The invite to accept
	 * @param request   The request
	 * @param recipient The user executing the request
	 * @return An updated invite and potentially information about the new friend or game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static @NotNull
	AcceptInviteResponse accept(@NotNull InviteId inviteId, @NotNull AcceptInviteRequest request, @NotNull UserRecord recipient) throws SuspendExecution, InterruptedException {
		Invite invite = mongo().findOne(INVITES, json("_id", inviteId.toString()), Invite.class);
		if (invite == null) {
			throw new NullPointerException(String.format("Invite not found: %s", inviteId));
		}
		if (!invite.getToUserId().equals(recipient.getId())) {
			throw new SecurityException("You did not have access to change this invite.");
		}
		if (request.getMatch() != null) {
			if (request.getMatch().getDeckId() == null) {
				throw new NullPointerException("No deckId specified.");
			}
			if (request.getMatch().getQueueId() != null && !request.getMatch().getQueueId().equals(invite.getQueueId())) {
				throw new IllegalArgumentException("Invalid queueId specified. Leave this field empty or use the invite's queueId");
			}
		}

		switch (invite.getStatus()) {
			case PENDING:
			case UNDELIVERED:
				invite.setStatus(StatusEnum.ACCEPTED);
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


		AcceptInviteResponse res = new AcceptInviteResponse();
		res.invite(invite);
		Envelope env = new Envelope();
		if (invite.getFriendId() != null) {
			// Make them friends, regardless if they are already friends.
			res.friend(Friends.putFriend(recipient, new FriendPutRequest().friendId(invite.getFriendId())));
		} else if (invite.getQueueId() != null) {
			try {
				// Regardless of what the client specified as its queueId, use the one from the invite
				MatchmakingQueuePutRequest match = request.getMatch();
				match.queueId(invite.getQueueId());
				Matchmaking.enqueue(new MatchmakingRequest(match, recipient.getId()));
				env.result(new EnvelopeResult().enqueue(new MatchmakingQueuePutResponse()));
				res.match(new MatchmakingQueuePutResponse());
			} catch (IllegalStateException alreadyInMatch) {
				// Reject the invite if the player was already in a match.
				invite.setStatus(StatusEnum.REJECTED);
				// This does the updating
				deleteInvite(new InviteId(invite.getId()), recipient);
				return res;
			}
		}

		// Only actually accept the invite if the actions suceeded.
		mongo().updateCollection(INVITES, json("_id", invite.getId()), json("$set", json("status", StatusEnum.ACCEPTED.getValue())));

		env.changed(new EnvelopeChanged().invite(invite));
		WriteStream<Envelope> recipientConnection = Connection.writeStream(recipient.getId());
		if (recipientConnection != null) {
			// Notify the client they've been enqueued by accepting. This should shortly lead to the game starting.
			recipientConnection.write(env);
		}

		WriteStream<Envelope> senderConnection = Connection.writeStream(invite.getFromUserId());
		if (senderConnection != null) {
			// Notify the client they've been enqueued by accepting. This should shortly lead to the game starting.
			senderConnection.write(new Envelope().changed(new EnvelopeChanged().invite(invite)));
		}

		// TODO: Only yield to the caller when the game has started?

		return res;
	}

	/**
	 * Deletes the invite only if it is pending or undelivered. Other kinds of invites cannot be deleted.
	 * <p>
	 * This will cancel the invite, or reject it if the recipient is deleting it.
	 *
	 * @param inviteId
	 * @param user
	 * @return
	 * @throws SuspendExecution
	 */
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

		StatusEnum status = isSender ? StatusEnum.CANCELLED : StatusEnum.REJECTED;
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

		Envelope env = new Envelope().changed(new EnvelopeChanged().invite(invite));
		WriteStream<Envelope> recipientConnection = Connection.writeStream(invite.getToUserId());
		if (recipientConnection != null) {
			// Notify the client they've been enqueued by accepting. This should shortly lead to the game starting.
			recipientConnection.write(env);
		}

		WriteStream<Envelope> senderConnection = Connection.writeStream(invite.getFromUserId());
		if (senderConnection != null) {
			// Notify the client they've been enqueued by accepting. This should shortly lead to the game starting.
			senderConnection.write(env);
		}

		return new InviteResponse().invite(invite);
	}
}
