package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.impl.Mongo;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.FriendRecord;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import io.vertx.core.Future;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.defer;

/**
 * Provides a way for users to friend each other.
 */
public interface Friends {
	/**
	 * Sends the player their friend list as "added" on their first connection
	 */
	static void handleConnections() {
		Connection.connected("Friends/handleConnections", (connection, fut) -> {
			defer(v -> {
				try {
					var user = mongo().findOne(Accounts.USERS, json("_id", connection.userId()), UserRecord.class);
					for (var friend : user.getFriends()) {
						connection.write(new Envelope().added(new EnvelopeAdded().friend(
								friend.toFriendDto()
										.presence(Presence.presence(new UserId(friend.getFriendId()))))));
					}
					fut.handle(Future.succeededFuture());
				} catch (RuntimeException any) {
					fut.handle(Future.failedFuture(any));
				}
			});
		});
	}

	/**
	 * Friends the specified users on behalf of the specified user.
	 *
	 * @param thisAccount The user who is initiating the friending.
	 * @param req         The request containing the username and privacy token; or the exact friend ID of the user to
	 *                    befriend.
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static FriendPutResponse putFriend(UserRecord thisAccount, FriendPutRequest req) throws SuspendExecution, InterruptedException {
		var userId = thisAccount.getId();

		// lookup friend user record
		UserRecord friendAccount;
		if (req.getUsernameWithToken() != null) {
			var tokens = req.getUsernameWithToken().split("#");
			friendAccount = Mongo.mongo().findOne(Accounts.USERS, json("username", tokens[0], "privacyToken", tokens[1]), UserRecord.class);
		} else if (req.getFriendId() != null) {
			friendAccount = mongo().findOne(Accounts.USERS, json("_id", req.getFriendId()), UserRecord.class);
		} else {
			friendAccount = null;
		}

		//if no friend, return 404
		if (friendAccount == null) {
			throw new NullPointerException("Friend account not found, or an invalid username and token were provided.");
		}

		var friendId = friendAccount.getId();

		//check if already friends
		if (thisAccount.isFriend(friendId)) {
			throw new IllegalArgumentException("Friend already friend");
		}

		var startOfFriendship = System.currentTimeMillis();

		var friendRecord = new FriendRecord()
				.setFriendId(friendId)
				.setSince(startOfFriendship)
				.setDisplayName(friendAccount.getUsername());
		var friendOfFriendRecord = new FriendRecord()
				.setFriendId(userId)
				.setSince(startOfFriendship)
				.setDisplayName(thisAccount.getUsername());

		// Update both sides
		mongo().updateCollection(Accounts.USERS, json("_id", userId), json("$push", json("friends", json(friendRecord))));
		mongo().updateCollection(Accounts.USERS, json("_id", friendId), json("$push", json("friends", json(friendOfFriendRecord))));

		// Update both users with the new friend records
		var userConnection = Connection.writeStream(userId);
		var clientFriendRecord = friendRecord.toFriendDto()
				.presence(Presence.presence(new UserId(friendRecord.getFriendId())));
		userConnection.write(new Envelope().added(new EnvelopeAdded().friend(clientFriendRecord)));

		var friendConnection = Connection.writeStream(friendId);
		friendConnection.write(new Envelope().added(
				new EnvelopeAdded().friend(friendOfFriendRecord.toFriendDto()
						.presence(Presence.presence(new UserId(friendOfFriendRecord.getFriendId()))))));

		return new FriendPutResponse().friend(clientFriendRecord);
	}

	static UnfriendResponse unfriend(UserRecord myAccount, String friendId) throws SuspendExecution, InterruptedException {
		var userId = myAccount.getId();
		// Lookup friend user record
		var friendAccount = Accounts.get(friendId);

		// Doesn't exist?
		if (friendAccount == null) {
			throw new NullPointerException("Friend account not found");
		}

		// Friends?
		var friendRecord = myAccount.getFriendById(friendId);
		if (friendRecord == null) {
			throw new NullPointerException("Not friends");
		}

		// Oops
		var friendOfFriendRecord = friendAccount.getFriendById(userId);
		if (friendOfFriendRecord == null) {
			throw new IllegalStateException("Presence not balanced.");
		}

		// Delete from both sides
		Mongo.mongo().updateCollection(Accounts.USERS, json("_id", userId), json("$pull",
				json("friends", json("friendId", friendId))));
		Mongo.mongo().updateCollection(Accounts.USERS, json("_id", friendId), json("$pull",
				json("friends", json("friendId", userId))));

		// Update both users with the new friend records
		var userConnection = Connection.writeStream(userId);
		userConnection.write(new Envelope().removed(new EnvelopeRemoved().friendId(friendRecord.getFriendId())));
		var friendConnection = Connection.writeStream(friendId);
		friendConnection.write(new Envelope().removed(new EnvelopeRemoved().friendId(friendOfFriendRecord.getFriendId())));

		return new UnfriendResponse().deletedFriend(friendRecord.toFriendDto().presence(PresenceEnum.UNKNOWN));
	}
}
