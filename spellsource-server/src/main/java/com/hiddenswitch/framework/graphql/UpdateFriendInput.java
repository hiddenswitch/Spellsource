package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateFriend` mutation.
 */
public class UpdateFriendInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private FriendPatch friendPatch;

    public UpdateFriendInput() {
    }

    public UpdateFriendInput(String clientMutationId, String nodeId, FriendPatch friendPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.friendPatch = friendPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public FriendPatch getFriendPatch() {
        return friendPatch;
    }
    public void setFriendPatch(FriendPatch friendPatch) {
        this.friendPatch = friendPatch;
    }



    public static UpdateFriendInput.Builder builder() {
        return new UpdateFriendInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private FriendPatch friendPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setFriendPatch(FriendPatch friendPatch) {
            this.friendPatch = friendPatch;
            return this;
        }


        public UpdateFriendInput build() {
            return new UpdateFriendInput(clientMutationId, nodeId, friendPatch);
        }

    }
}
