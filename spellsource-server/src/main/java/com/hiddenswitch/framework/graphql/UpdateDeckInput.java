package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateDeck` mutation.
 */
public class UpdateDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private DeckPatch deckPatch;

    public UpdateDeckInput() {
    }

    public UpdateDeckInput(String clientMutationId, String nodeId, DeckPatch deckPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.deckPatch = deckPatch;
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

    public DeckPatch getDeckPatch() {
        return deckPatch;
    }
    public void setDeckPatch(DeckPatch deckPatch) {
        this.deckPatch = deckPatch;
    }



    public static UpdateDeckInput.Builder builder() {
        return new UpdateDeckInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private DeckPatch deckPatch;

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

        public Builder setDeckPatch(DeckPatch deckPatch) {
            this.deckPatch = deckPatch;
            return this;
        }


        public UpdateDeckInput build() {
            return new UpdateDeckInput(clientMutationId, nodeId, deckPatch);
        }

    }
}
