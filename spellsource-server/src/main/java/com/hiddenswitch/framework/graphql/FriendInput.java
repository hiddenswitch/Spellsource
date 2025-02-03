package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `Friend`
 */
public class FriendInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String friend;
    private String createdAt;

    public FriendInput() {
    }

    public FriendInput(String id, String friend, String createdAt) {
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



    public static FriendInput.Builder builder() {
        return new FriendInput.Builder();
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


        public FriendInput build() {
            return new FriendInput(id, friend, createdAt);
        }

    }
}
