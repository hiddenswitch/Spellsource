package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.FriendPutRequest;
import com.hiddenswitch.spellsource.client.models.FriendPutResponse;
import com.hiddenswitch.spellsource.client.models.UnfriendResponse;
import com.hiddenswitch.spellsource.impl.util.FriendRecord;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.Mongo;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public interface Friends {
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
		String friendId = req.getFriendId();
		String userId = thisAccount.getId();

		String[] tokens = req.getUsernameWithToken().split("#");

		// lookup friend user record
		UserRecord friendAccount;
		if (req.getUsernameWithToken() != null) {
			friendAccount = Mongo.mongo().findOne(Accounts.USERS, json("username", tokens[0], "privacyToken", tokens[1]), UserRecord.class);
		} else if (req.getFriendId() != null) {
			friendAccount = Accounts.findOne(req.getFriendId());
		} else {
			friendAccount = null;
		}

		//if no friend, return 404
		if (friendAccount == null) {
			throw new NullPointerException("Friend account not found, or an invalid username and token were provided.");
		}

		friendId = friendAccount.getId();

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
		Accounts.update(userId, json("$push", json("friends", json(friendRecord))));
		Accounts.update(friendId, json("$push", json("friends", json(friendOfFriendRecord))));

		// Update presence for both users
		Presence.setPresence(userId);
		Presence.setPresence(friendId);
		return new FriendPutResponse().friend(friendRecord.toFriendDto());
	}

	static UnfriendResponse unfriend(UserRecord myAccount, String friendId) throws SuspendExecution, InterruptedException {
		String userId = myAccount.getId();
		//lookup friend user record
		UserRecord friendAccount = Accounts.get(friendId);

		//doesn't exist?
		if (friendAccount == null) {
			throw new NullPointerException("Friend account not found");
		}

		//friends?
		FriendRecord friendRecord = myAccount.getFriendById(friendId);
		if (friendRecord == null) {
			throw new NullPointerException("Not friends");
		}

		//Oops
		FriendRecord friendOfFriendRecord = friendAccount.getFriendById(userId);
		if (friendOfFriendRecord == null) {
			throw new IllegalStateException("Presence not balanced.");
		}

		//delete from both sides
		Accounts.update(Mongo.mongo().client(), userId, json("$pull",
				json("friends", json("friendId", friendId))));
		Accounts.update(Mongo.mongo().client(), friendId, json("$pull",
				json("friends", json("friendId", userId))));

		return new UnfriendResponse().deletedFriend(friendRecord.toFriendDto());
	}
}
