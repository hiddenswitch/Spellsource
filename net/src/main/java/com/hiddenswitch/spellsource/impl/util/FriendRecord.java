package com.hiddenswitch.spellsource.impl.util;

import com.hiddenswitch.spellsource.client.models.Friend;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;

import java.io.Serializable;

/**
 * Created by weller on 6/5/17. This class represents an internal record, thus does not extend MongoRecord
 */
public class FriendRecord implements Serializable {
	private String friendId;
	private long since;
	private String displayName;
	private PresenceEnum presence;

	public long getSince() {
		return since;
	}

	public String getFriendId() {
		return friendId;
	}

	public FriendRecord setFriendId(String friendId) {
		this.friendId = friendId;
		return this;
	}

	public FriendRecord setSince(long since) {
		this.since = since;
		return this;
	}

	public String getDisplayName() {
		return displayName;
	}

	public FriendRecord setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}


	public void setPresence(PresenceEnum presence) {
		this.presence = presence;
	}

	public Friend toFriendDto() {
		return new Friend().friendId(this.friendId).since(this.since).friendName(this.displayName).presence(presence);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FriendRecord that = (FriendRecord) o;

		if (since != that.since) return false;
		if (friendId != null ? !friendId.equals(that.friendId) : that.friendId != null) return false;
		return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
	}

	@Override
	public int hashCode() {
		int result = friendId != null ? friendId.hashCode() : 0;
		result = 31 * result + (int) (since ^ (since >>> 32));
		result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
		return result;
	}

	public PresenceEnum getPresence() {
		return presence;
	}
}
