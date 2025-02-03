package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `Friend` object types. All fields are tested for equality and combined with a logical ‘and.’
 */
public class FriendCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String friend;
    private String createdAt;

    public FriendCondition() {
    }

    public FriendCondition(String id, String friend, String createdAt) {
        this.id = id;
        this.friend = friend;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getFriend() {
        return friend;
    }
    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }



    public static FriendCondition.Builder builder() {
        return new FriendCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String friend;
        private String createdAt;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setFriend(String friend) {
            this.friend = friend;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }


        public FriendCondition build() {
            return new FriendCondition(id, friend, createdAt);
        }

    }
}
