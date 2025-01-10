package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteFriendByIdAndFriend` mutation.
 */
public class DeleteFriendByIdAndFriendInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private String friend;

    public DeleteFriendByIdAndFriendInput() {
    }

    public DeleteFriendByIdAndFriendInput(String clientMutationId, String id, String friend) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.friend = friend;
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



    public static DeleteFriendByIdAndFriendInput.Builder builder() {
        return new DeleteFriendByIdAndFriendInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private String friend;

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


        public DeleteFriendByIdAndFriendInput build() {
            return new DeleteFriendByIdAndFriendInput(clientMutationId, id, friend);
        }

    }
}
