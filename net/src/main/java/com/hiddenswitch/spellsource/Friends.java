package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.FriendPutRequest;
import com.hiddenswitch.spellsource.client.models.FriendPutResponse;
import com.hiddenswitch.spellsource.impl.util.FriendRecord;
import com.hiddenswitch.spellsource.impl.util.UserRecord;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public interface Friends {
	static FriendPutResponse putFriend(UserRecord thisAccount, FriendPutRequest req) throws SuspendExecution, InterruptedException {
		String friendId = req.getFriendId();
		String userId = thisAccount.getId();

		//lookup friend user record
		UserRecord friendAccount = Accounts.findOne(req.getFriendId());

		//if no friend, return 404
		if (friendAccount == null) {
			throw new NullPointerException("Friend account not found");
		}

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
}
