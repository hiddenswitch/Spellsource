package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `Friend` mutation.
 */
public class CreateFriendInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private FriendInput friend;

    public CreateFriendInput() {
    }

    public CreateFriendInput(String clientMutationId, FriendInput friend) {
        this.clientMutationId = clientMutationId;
        this.friend = friend;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public FriendInput getFriend() {
        return friend;
    }
    public void setFriend(FriendInput friend) {
        this.friend = friend;
    }



    public static CreateFriendInput.Builder builder() {
        return new CreateFriendInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private FriendInput friend;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setFriend(FriendInput friend) {
            this.friend = friend;
            return this;
        }


        public CreateFriendInput build() {
            return new CreateFriendInput(clientMutationId, friend);
        }

    }
}
