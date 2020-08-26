package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeChanged;
import com.hiddenswitch.spellsource.client.models.Friend;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.UserId;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
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
	int TIMEOUT_MILLIS = 10000;

	/**
	 * Cleans up old presence values
	 */
	@Suspendable
	static void vacuum() {
		var presence = SuspendableMap.<UserId, JsonObject>getOrCreate("Presence");
		for (var key : presence.keySet()) {
			var json = presence.get(key);
			if (json == null) {
				continue;
			}
			if (System.currentTimeMillis() - json.getLong("time") > TIMEOUT_MILLIS + 1) {
				presence.remove(key);
			}
		}
	}

	static void handleConnections() {
		// A node that is updating presences may not be the same node that has a user that needs to be notified
		Connection.connected((connection, fut) -> {
			var vertx = Vertx.currentContext().owner();

			var userId = new UserId(connection.userId());
			SuspendableAction1<Long> connectedPresence = timerId -> {
				var presence = SuspendableMap.<UserId, JsonObject>getOrCreate("Presence");
				if (!connection.isOpen()) {
					vertx.cancelTimer(timerId);
					presence.remove(userId);
					notifyFriendsOfPresence(userId, PresenceEnum.OFFLINE);
					return;
				}
				var latestPresence = (Games.isInGame(userId) || Matchmaking.getUsersInQueues().containsKey(userId.toString())) ? PresenceEnum.IN_GAME : PresenceEnum.ONLINE;

				var currentPresence = presence.put(userId, json("time", System.currentTimeMillis(), "presence", latestPresence.getValue()), TIMEOUT_MILLIS * 2);
				if (currentPresence == null || PresenceEnum.fromValue(currentPresence.getString("presence")) != latestPresence) {
					notifyFriendsOfPresence(userId);
				}
			};

			var timer = vertx.setPeriodic(TIMEOUT_MILLIS / 2, fiber(connectedPresence));

			connection.addCloseHandler(fiber(v -> {
				vertx.cancelTimer(timer);
				var presence = SuspendableMap.<UserId, JsonObject>getOrCreate("Presence");
				presence.remove(userId);
				notifyFriendsOfPresence(userId, PresenceEnum.OFFLINE);
				v.complete();
			}));

			defer(v -> {
				connectedPresence.call(timer);
				notifyFriendsOfPresence(userId);
			});
			fut.handle(Future.succeededFuture());
		});
	}

	@Suspendable
	static void notifyFriendsOfPresence(UserId userId) {
		notifyFriendsOfPresence(userId, presence(userId));
	}

	@Suspendable
	static void notifyFriendsOfPresence(UserId userId, PresenceEnum presence) {
		var findOptions = new FindOptions()
				.setFields(json("_id", 1, "friends.friendId", 1));

		// Friends of userId, notify them of userId presence's
		mongo().client().findWithOptions(Accounts.USERS, json("friends.friendId", userId.toString()), findOptions, res -> {
			if (res.failed()) {
				LOGGER.error("updatePresence {} {}: {}", userId, presence, res.cause().getMessage(), res.cause());
				return;
			}

			for (var user : res.result()) {
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
	static PresenceEnum presence(UserId userId) {
		var presence = SuspendableMap.<UserId, JsonObject>getOrCreate("Presence");
		var res = presence.get(userId);
		if (res == null) {
			return PresenceEnum.OFFLINE;
		}
		if (!res.containsKey("time") || System.currentTimeMillis() - res.getLong("time") > TIMEOUT_MILLIS) {
			return PresenceEnum.OFFLINE;
		}

		return PresenceEnum.fromValue(res.getString("presence"));
	}
}