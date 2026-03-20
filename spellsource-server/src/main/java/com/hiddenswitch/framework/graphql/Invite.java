package com.hiddenswitch.framework.graphql;


/**
 * ─── Invite types ─────────────────────────────────────────────
 */
public class Invite implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private java.lang.Long expiresAt;
    private String fromName;
    private String fromUserId;
    private String toName;
    private String toUserId;
    private String friendId;
    private String message;
    private String queueId;
    private InviteStatus status;

    public Invite() {
    }

    public Invite(String id, java.lang.Long expiresAt, String fromName, String fromUserId, String toName, String toUserId, String friendId, String message, String queueId, InviteStatus status) {
        this.id = id;
        this.expiresAt = expiresAt;
        this.fromName = fromName;
        this.fromUserId = fromUserId;
        this.toName = toName;
        this.toUserId = toUserId;
        this.friendId = friendId;
        this.message = message;
        this.queueId = queueId;
        this.status = status;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public java.lang.Long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(java.lang.Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getFromName() {
        return fromName;
    }
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromUserId() {
        return fromUserId;
    }
    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToName() {
        return toName;
    }
    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getToUserId() {
        return toUserId;
    }
    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFriendId() {
        return friendId;
    }
    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getQueueId() {
        return queueId;
    }
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public InviteStatus getStatus() {
        return status;
    }
    public void setStatus(InviteStatus status) {
        this.status = status;
    }



    public static Invite.Builder builder() {
        return new Invite.Builder();
    }

    public static class Builder {

        private String id;
        private java.lang.Long expiresAt;
        private String fromName;
        private String fromUserId;
        private String toName;
        private String toUserId;
        private String friendId;
        private String message;
        private String queueId;
        private InviteStatus status;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setExpiresAt(java.lang.Long expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder setFromName(String fromName) {
            this.fromName = fromName;
            return this;
        }

        public Builder setFromUserId(String fromUserId) {
            this.fromUserId = fromUserId;
            return this;
        }

        public Builder setToName(String toName) {
            this.toName = toName;
            return this;
        }

        public Builder setToUserId(String toUserId) {
            this.toUserId = toUserId;
            return this;
        }

        public Builder setFriendId(String friendId) {
            this.friendId = friendId;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder setStatus(InviteStatus status) {
            this.status = status;
            return this;
        }


        public Invite build() {
            return new Invite(id, expiresAt, fromName, fromUserId, toName, toUserId, friendId, message, queueId, status);
        }

    }
}
