package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.InviteId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.core.json.JsonObject.mapFrom;

public interface Invites {
	String INVITES = "invites";
	JsonArray PENDING_STATUSES = new JsonArray().add(Invite.StatusEnum.UNDELIVERED.getValue()).add(Invite.StatusEnum.PENDING.getValue());


	static void realtime() throws SuspendExecution {
		Realtime.connected(Sync.suspendableHandler(connection -> {
			// Notify recipients of undelivered invites
			List<Invite> invites = mongo().find(INVITES, json("toUserId", connection.userId(), "status", Invite.StatusEnum.UNDELIVERED.getValue()), Invite.class);
			for (Invite invite : invites) {
				invite.status(Invite.StatusEnum.PENDING);
				connection.write(JsonObject.mapFrom(new Envelope().added(new EnvelopeAdded().invite(invite))));
			}

			JsonArray ids = new JsonArray(invites.stream().map(Invite::getId).collect(Collectors.toList()));
			mongo().updateCollectionWithOptions(INVITES,
					json("_id", json("$in", ids)),
					json("$set",
							json("status", Invite.StatusEnum.PENDING.getValue())),
					new UpdateOptions().setMulti(true));
		}));
	}

	static InvitePostResponse postInvite(InvitePostRequest request, UserRecord user) throws SuspendExecution {
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
		// Create a new queue just for this invite.
		// Right now, anyone can wait in any queue, but this is probably the most convenient.
		String customQueueId = request.getQueueId() + "-" + inviteId;
		Calendar expiryTime = Calendar.getInstance();
		expiryTime.setTime(new Date());
		expiryTime.add(Calendar.MINUTE, 15);

		Vertx vertx = Vertx.currentContext().owner();

		vertx.setTimer(15 * 60 * 1000L, Sync.suspendableHandler(timerId -> {
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
				.status(Invite.StatusEnum.UNDELIVERED)
				.queueId(customQueueId);

		// Notify both users of the new invite, but only wait to see if the recipient is around to actually receive it right
		// now.
		WriteStream<Buffer> toUserConnection = Connection.get(toUser.getId());
		if (toUserConnection != null) {
			invite.status(Invite.StatusEnum.PENDING);
			toUserConnection.write(Json.encodeToBuffer(
					new Envelope().added(new EnvelopeAdded().invite(invite))
			));
		}

		Connection.get(user.getId(), res -> {
			WriteStream<Buffer> conn = res.result();
			if (conn == null) {
				return;
			}

			conn.write(Json.encodeToBuffer(
					new Envelope().added(new EnvelopeAdded().invite(invite))
			));
		});

		mongo().insert(INVITES, mapFrom(invite));

		return new InvitePostResponse()
				.invite(invite);
	}
}
