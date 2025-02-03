package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteGame` mutation.
 */
public class DeleteGameInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;

    public DeleteGameInput() {
    }

    public DeleteGameInput(String clientMutationId, String nodeId) {
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



    public static DeleteGameInput.Builder builder() {
        return new DeleteGameInput.Builder();
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


        public DeleteGameInput build() {
            return new DeleteGameInput(clientMutationId, nodeId);
        }

    }
}
