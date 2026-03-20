package com.hiddenswitch.framework.graphql;


public class InvitePostInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String toUserId;
    private String toUserNameWithToken;
    private String deckId;
    private String queueId;
    private Boolean friend;
    private String message;

    public InvitePostInput() {
    }

    public InvitePostInput(String toUserId, String toUserNameWithToken, String deckId, String queueId, Boolean friend, String message) {
        this.toUserId = toUserId;
        this.toUserNameWithToken = toUserNameWithToken;
        this.deckId = deckId;
        this.queueId = queueId;
        this.friend = friend;
        this.message = message;
    }

    public String getToUserId() {
        return toUserId;
    }
    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserNameWithToken() {
        return toUserNameWithToken;
    }
    public void setToUserNameWithToken(String toUserNameWithToken) {
        this.toUserNameWithToken = toUserNameWithToken;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getQueueId() {
        return queueId;
    }
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public Boolean getFriend() {
        return friend;
    }
    public void setFriend(Boolean friend) {
        this.friend = friend;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }



    public static InvitePostInput.Builder builder() {
        return new InvitePostInput.Builder();
    }

    public static class Builder {

        private String toUserId;
        private String toUserNameWithToken;
        private String deckId;
        private String queueId;
        private Boolean friend;
        private String message;

        public Builder() {
        }

        public Builder setToUserId(String toUserId) {
            this.toUserId = toUserId;
            return this;
        }

        public Builder setToUserNameWithToken(String toUserNameWithToken) {
            this.toUserNameWithToken = toUserNameWithToken;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder setFriend(Boolean friend) {
            this.friend = friend;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }


        public InvitePostInput build() {
            return new InvitePostInput(toUserId, toUserNameWithToken, deckId, queueId, friend, message);
        }

    }
}
