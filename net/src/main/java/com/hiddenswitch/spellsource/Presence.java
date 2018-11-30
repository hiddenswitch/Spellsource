package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeChanged;
import com.hiddenswitch.spellsource.client.models.Friend;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;

/**
 * Provides presence information to players who are each other's friends.
 */
public interface Presence {
	static void handleConnections() {
		// A node that is updating presences may not be the same node that has a user that needs to be notified
		Connection.connected(Sync.suspendableHandler(connection -> {
			final UserId key = new UserId(connection.userId());
			connection.endHandler(Sync.suspendableHandler(ignored -> {
				setPresence(key, PresenceEnum.OFFLINE);
			}));

			// Once the user is connected, set their status to online
			setPresence(key, PresenceEnum.ONLINE);
		}));
	}

	static void setPresence(UserId userId, PresenceEnum presence) {
		// This doesn't need to be blocking.
		mongo().client().updateCollectionWithOptions(Accounts.USERS,
						json("friends.friendId", userId.toString()),
						json("$set", json("friends.$.presence", presence.name())), new UpdateOptions().setMulti(true), Future.future());

		FindOptions findOptions = new FindOptions()
						.setFields(json("_id", 1, "friends.friendId", 1, "friends.presence", 1));
		mongo().client().findWithOptions(Accounts.USERS, json("friends.friendId", userId.toString()), findOptions, res -> {
			for (JsonObject user : res.result()) {
				Connection.writeStream(user.getString("_id"), otherUser -> {
					if (otherUser.failed() || otherUser.result() == null) {
						return;
					}
					otherUser.result().write(new Envelope().changed(new EnvelopeChanged().friend(new Friend().friendId(userId.toString()).presence(presence))));
				});
			}
		});
	}

	static void setPresence(String userId) {
		final UserId key = new UserId(userId);
		Connection.writeStream(userId, res -> {
			if (res.failed() || res.result() == null) {
				setPresence(key, PresenceEnum.OFFLINE);
			} else {
				setPresence(key, PresenceEnum.ONLINE);
			}
		});
	}
}