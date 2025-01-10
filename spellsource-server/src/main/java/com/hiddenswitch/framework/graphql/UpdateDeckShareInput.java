package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateDeckShare` mutation.
 */
public class UpdateDeckShareInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private DeckSharePatch deckSharePatch;

    public UpdateDeckShareInput() {
    }

    public UpdateDeckShareInput(String clientMutationId, String nodeId, DeckSharePatch deckSharePatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.deckSharePatch = deckSharePatch;
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

    public DeckSharePatch getDeckSharePatch() {
        return deckSharePatch;
    }
    public void setDeckSharePatch(DeckSharePatch deckSharePatch) {
        this.deckSharePatch = deckSharePatch;
    }



    public static UpdateDeckShareInput.Builder builder() {
        return new UpdateDeckShareInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private DeckSharePatch deckSharePatch;

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

        public Builder setDeckSharePatch(DeckSharePatch deckSharePatch) {
            this.deckSharePatch = deckSharePatch;
            return this;
        }


        public UpdateDeckShareInput build() {
            return new UpdateDeckShareInput(clientMutationId, nodeId, deckSharePatch);
        }

    }
}
