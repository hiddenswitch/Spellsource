package com.hiddenswitch.framework.graphql;


public class Friend implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String friendId;
    private String friendName;
    private Presence presence;
    private java.lang.Long since;

    public Friend() {
    }

    public Friend(String friendId, String friendName, Presence presence, java.lang.Long since) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.presence = presence;
        this.since = since;
    }

    public String getFriendId() {
        return friendId;
    }
    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }
    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public Presence getPresence() {
        return presence;
    }
    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    public java.lang.Long getSince() {
        return since;
    }
    public void setSince(java.lang.Long since) {
        this.since = since;
    }



    public static Friend.Builder builder() {
        return new Friend.Builder();
    }

    public static class Builder {

        private String friendId;
        private String friendName;
        private Presence presence;
        private java.lang.Long since;

        public Builder() {
        }

        public Builder setFriendId(String friendId) {
            this.friendId = friendId;
            return this;
        }

        public Builder setFriendName(String friendName) {
            this.friendName = friendName;
            return this;
        }

        public Builder setPresence(Presence presence) {
            this.presence = presence;
            return this;
        }

        public Builder setSince(java.lang.Long since) {
            this.since = since;
            return this;
        }


        public Friend build() {
            return new Friend(friendId, friendName, presence, since);
        }

    }
}
