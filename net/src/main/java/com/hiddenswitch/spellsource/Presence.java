package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;
import com.hiddenswitch.spellsource.impl.UserId;
import io.vertx.core.Future;
import io.vertx.ext.mongo.UpdateOptions;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class Presence {
	public static void realtime() throws SuspendExecution {
		// A different method is responsible for keeping the user up to date on their accounts document
		Realtime.connected(connection -> {
			connection.handler(ignored -> {
				// Update the presence whenever the user sends a message
				setPresence(new UserId(connection.userId()), PresenceEnum.ONLINE);
			});

			connection.endHandler(ignored -> {
				setPresence(new UserId(connection.userId()), PresenceEnum.OFFLINE);
			});
		});
	}

	private static void setPresence(UserId userId, PresenceEnum online) {
		// This doesn't need to be blocking.
		mongo().client().updateCollectionWithOptions(Accounts.USERS,
						json("friends.friendId", userId.toString()),
						json("$set", json("friends.$.presence", online)), new UpdateOptions().setMulti(true), Future.future());
	}
}
