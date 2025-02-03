package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateFriendByIdAndFriend` mutation.
 */
public class UpdateFriendByIdAndFriendInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private String friend;
    private FriendPatch friendPatch;

    public UpdateFriendByIdAndFriendInput() {
    }

    public UpdateFriendByIdAndFriendInput(String clientMutationId, String id, String friend, FriendPatch friendPatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.friend = friend;
        this.friendPatch = friendPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
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

    public FriendPatch getFriendPatch() {
        return friendPatch;
    }
    public void setFriendPatch(FriendPatch friendPatch) {
        this.friendPatch = friendPatch;
    }



    public static UpdateFriendByIdAndFriendInput.Builder builder() {
        return new UpdateFriendByIdAndFriendInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private String friend;
        private FriendPatch friendPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setFriend(String friend) {
            this.friend = friend;
            return this;
        }

        public Builder setFriendPatch(FriendPatch friendPatch) {
            this.friendPatch = friendPatch;
            return this;
        }


        public UpdateFriendByIdAndFriendInput build() {
            return new UpdateFriendByIdAndFriendInput(clientMutationId, id, friend, friendPatch);
        }

    }
}
