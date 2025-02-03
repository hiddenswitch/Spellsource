package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteMatchmakingQueue` mutation.
 */
public class DeleteMatchmakingQueueInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;

    public DeleteMatchmakingQueueInput() {
    }

    public DeleteMatchmakingQueueInput(String clientMutationId, String nodeId) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
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



    public static DeleteMatchmakingQueueInput.Builder builder() {
        return new DeleteMatchmakingQueueInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;

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


        public DeleteMatchmakingQueueInput build() {
            return new DeleteMatchmakingQueueInput(clientMutationId, nodeId);
        }

    }
}
