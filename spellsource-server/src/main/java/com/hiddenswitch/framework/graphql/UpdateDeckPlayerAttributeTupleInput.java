package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateDeckPlayerAttributeTuple` mutation.
 */
public class UpdateDeckPlayerAttributeTupleInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch;

    public UpdateDeckPlayerAttributeTupleInput() {
    }

    public UpdateDeckPlayerAttributeTupleInput(String clientMutationId, String nodeId, DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.deckPlayerAttributeTuplePatch = deckPlayerAttributeTuplePatch;
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

    public DeckPlayerAttributeTuplePatch getDeckPlayerAttributeTuplePatch() {
        return deckPlayerAttributeTuplePatch;
    }
    public void setDeckPlayerAttributeTuplePatch(DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch) {
        this.deckPlayerAttributeTuplePatch = deckPlayerAttributeTuplePatch;
    }



    public static UpdateDeckPlayerAttributeTupleInput.Builder builder() {
        return new UpdateDeckPlayerAttributeTupleInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch;

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

        public Builder setDeckPlayerAttributeTuplePatch(DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch) {
            this.deckPlayerAttributeTuplePatch = deckPlayerAttributeTuplePatch;
            return this;
        }


        public UpdateDeckPlayerAttributeTupleInput build() {
            return new UpdateDeckPlayerAttributeTupleInput(clientMutationId, nodeId, deckPlayerAttributeTuplePatch);
        }

    }
}
