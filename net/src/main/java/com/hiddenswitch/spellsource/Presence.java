package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeChanged;
import com.hiddenswitch.spellsource.client.models.Friend;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;
import com.hiddenswitch.spellsource.concurrent.SuspendableCounter;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.UpdateOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.Sync.defer;

/**
 * Provides presence information to players who are each other's friends.
 */
public interface Presence {
	Logger LOGGER = LoggerFactory.getLogger(Presence.class);

	static void handleConnections() {
		// A node that is updating presences may not be the same node that has a user that needs to be notified
		Connection.connected((connection, fut) -> {
			defer(v -> {
				try {
					UserId key = new UserId(connection.userId());
					connection.endHandler(Sync.suspendableHandler(ignored -> {
						SuspendableCounter connections = SuspendableCounter.create("Presence::connections[" + connection.userId() + "]");
						if (connections.decrementAndGet() == 0L) {
							updatePresence(key, PresenceEnum.OFFLINE);
						}
					}));

					SuspendableCounter connections = SuspendableCounter.create("Presence::connections[" + connection.userId() + "]");
					// Once the user is connected, set their status to online
					long numConnections = connections.incrementAndGet();
					if (numConnections == 1L) {
						updatePresence(key, PresenceEnum.ONLINE);
					}
					if (numConnections > 20L) {
						LOGGER.warn("handleConnections: User has {} connections", numConnections);
					}
					fut.handle(Future.succeededFuture());
				} catch (RuntimeException any) {
					fut.handle(Future.failedFuture(any));
				}
			});

		});
	}

	static void updatePresence(UserId userId, PresenceEnum presence) {
		// This doesn't need to be blocking.
		mongo().client().updateCollectionWithOptions(Accounts.USERS,
				json("friends.friendId", userId.toString()),
				json("$set", json("friends.$.presence", presence.name())), new UpdateOptions().setMulti(true), Future.future());

		FindOptions findOptions = new FindOptions()
				.setFields(json("_id", 1, "friends.friendId", 1, "friends.presence", 1));
		mongo().client().findWithOptions(Accounts.USERS, json("friends.friendId", userId.toString()), findOptions, res -> {
			for (JsonObject user : res.result()) {
				Connection.writeStream(user.getString("_id"))
						.write(new Envelope()
								.changed(new EnvelopeChanged()
										.friend(new Friend()
												.friendId(userId.toString())
												.presence(presence))));
			}
		});
	}


	static void updatePresence(String userId) throws SuspendExecution {
		SuspendableCounter connections = SuspendableCounter.create("Presence::connections[" + userId + "]");
		boolean isInGame = Games.getUsersInGames().containsKey(new UserId(userId));
		if (connections.get() == 0L) {
			updatePresence(new UserId(userId), PresenceEnum.OFFLINE);
		} else {
			updatePresence(new UserId(userId), isInGame ? PresenceEnum.IN_GAME : PresenceEnum.ONLINE);
		}
	}
}