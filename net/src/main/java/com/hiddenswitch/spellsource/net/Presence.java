package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeChanged;
import com.hiddenswitch.spellsource.client.models.Friend;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableCounter;
import com.hiddenswitch.spellsource.net.impl.Sync;
import com.hiddenswitch.spellsource.net.impl.UserId;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.defer;
import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;

/**
 * Provides presence information to players who are each other's friends.
 */
public interface Presence {
	Logger LOGGER = LoggerFactory.getLogger(Presence.class);

	static void handleConnections() {
		// A node that is updating presences may not be the same node that has a user that needs to be notified
		Connection.connected((connection, fut) -> {
			connection.endHandler(Sync.fiber(ignored -> {
				SuspendableCounter connections = connections(connection.userId());
				if (connections.decrementAndGet() == 0L) {
					notifyFriendsOfPresence(new UserId(connection.userId()), PresenceEnum.OFFLINE);
					connections.close();
				}
			}));

			defer(v -> {
				// Once the user is connected, set their status to online
				SuspendableCounter connections = connections(connection.userId());
				long numConnections = connections.incrementAndGet();
				if (numConnections == 1L) {
					updatePresence(connection.userId());
				}
				if (numConnections > 20L) {
					LOGGER.warn("handleConnections: User has {} connections", numConnections);
				}
			});
			fut.handle(Future.succeededFuture());
		});
	}

	@Suspendable
	static void notifyFriendsOfPresence(UserId userId, PresenceEnum presence) {
		FindOptions findOptions = new FindOptions()
				.setFields(json("_id", 1, "friends.friendId", 1));

		// Friends of userId, notify them of userId presence's
		mongo().client().findWithOptions(Accounts.USERS, json("friends.friendId", userId.toString()), findOptions, res -> {
			if (res.failed()) {
				LOGGER.error("updatePresence {} {}: {}", userId, presence, res.cause().getMessage(), res.cause());
				return;
			}

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

	@Suspendable
	static PresenceEnum getPresence(String userId) throws SuspendExecution {
		SuspendableCounter connections = connections(userId);
		boolean isInGame = Games.getUsersInGames().containsKey(new UserId(userId));
		if (connections.get() == 0L) {
			return PresenceEnum.OFFLINE;
		} else {
			return isInGame ? PresenceEnum.IN_GAME : PresenceEnum.ONLINE;
		}
	}

	static void updatePresence(String userId) throws SuspendExecution {
		notifyFriendsOfPresence(new UserId(userId), getPresence(userId));
	}

	@NotNull
	@Suspendable
	static SuspendableCounter connections(String userId) {
		return SuspendableCounter.getOrCreate("Presence/connections/" + userId);
	}
}