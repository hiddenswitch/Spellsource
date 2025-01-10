package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteCardsInDeck` mutation.
 */
public class DeleteCardsInDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;

    public DeleteCardsInDeckInput() {
    }

    public DeleteCardsInDeckInput(String clientMutationId, String nodeId) {
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



    public static DeleteCardsInDeckInput.Builder builder() {
        return new DeleteCardsInDeckInput.Builder();
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


        public DeleteCardsInDeckInput build() {
            return new DeleteCardsInDeckInput(clientMutationId, nodeId);
        }

    }
}
