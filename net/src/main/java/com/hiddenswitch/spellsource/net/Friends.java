package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.impl.Mongo;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.FriendRecord;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import io.vertx.core.Future;
import io.vertx.core.streams.WriteStream;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.defer;

/**
 * Provides a way for users to friend each other.
 */
public interface Friends {
	/**
	 * Sends the player their friend list as "added" on their first connection
	 */
	static void handleConnections() {
		Connection.connected((connection, fut) -> {
			defer(v -> {
				try {
					UserRecord user = mongo().findOne(Accounts.USERS, json("_id", connection.userId()), UserRecord.class);
					for (FriendRecord friend : user.getFriends()) {
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
		String userId = thisAccount.getId();

		// lookup friend user record
		UserRecord friendAccount;
		if (req.getUsernameWithToken() != null) {
			String[] tokens = req.getUsernameWithToken().split("#");
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

		String friendId = friendAccount.getId();

		//check if already friends
		if (thisAccount.isFriend(friendId)) {
			throw new IllegalArgumentException("Friend already friend");
		}

		long startOfFriendship = System.currentTimeMillis();

		FriendRecord friendRecord = new FriendRecord()
				.setFriendId(friendId)
				.setSince(startOfFriendship)
				.setDisplayName(friendAccount.getUsername());
		FriendRecord friendOfFriendRecord = new FriendRecord()
				.setFriendId(userId)
				.setSince(startOfFriendship)
				.setDisplayName(thisAccount.getUsername());

		// Update both sides
		mongo().updateCollection(Accounts.USERS, json("_id", userId), json("$push", json("friends", json(friendRecord))));
		mongo().updateCollection(Accounts.USERS, json("_id", friendId), json("$push", json("friends", json(friendOfFriendRecord))));

		// Update both users with the new friend records
		WriteStream<Envelope> userConnection = Connection.writeStream(userId);
		Friend clientFriendRecord = friendRecord.toFriendDto()
				.presence(Presence.presence(new UserId(friendRecord.getFriendId())));
		userConnection.write(new Envelope().added(new EnvelopeAdded().friend(clientFriendRecord)));

		WriteStream<Envelope> friendConnection = Connection.writeStream(friendId);
		friendConnection.write(new Envelope().added(
				new EnvelopeAdded().friend(friendOfFriendRecord.toFriendDto()
						.presence(Presence.presence(new UserId(friendOfFriendRecord.getFriendId()))))));

		return new FriendPutResponse().friend(clientFriendRecord);
	}

	static UnfriendResponse unfriend(UserRecord myAccount, String friendId) throws SuspendExecution, InterruptedException {
		String userId = myAccount.getId();
		// Lookup friend user record
		UserRecord friendAccount = Accounts.get(friendId);

		// Doesn't exist?
		if (friendAccount == null) {
			throw new NullPointerException("Friend account not found");
		}

		// Friends?
		FriendRecord friendRecord = myAccount.getFriendById(friendId);
		if (friendRecord == null) {
			throw new NullPointerException("Not friends");
		}

		// Oops
		FriendRecord friendOfFriendRecord = friendAccount.getFriendById(userId);
		if (friendOfFriendRecord == null) {
			throw new IllegalStateException("Presence not balanced.");
		}

		// Delete from both sides
		Mongo.mongo().updateCollection(Accounts.USERS, json("_id", userId), json("$pull",
				json("friends", json("friendId", friendId))));
		Mongo.mongo().updateCollection(Accounts.USERS, json("_id", friendId), json("$pull",
				json("friends", json("friendId", userId))));

		// Update both users with the new friend records
		WriteStream<Envelope> userConnection = Connection.writeStream(userId);
		userConnection.write(new Envelope().removed(new EnvelopeRemoved().friendId(friendRecord.getFriendId())));
		WriteStream<Envelope> friendConnection = Connection.writeStream(friendId);
		friendConnection.write(new Envelope().removed(new EnvelopeRemoved().friendId(friendOfFriendRecord.getFriendId())));

		return new UnfriendResponse().deletedFriend(friendRecord.toFriendDto().presence(PresenceEnum.UNKNOWN));
	}
}
