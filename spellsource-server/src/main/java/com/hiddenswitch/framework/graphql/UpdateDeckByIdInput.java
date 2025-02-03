package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateDeckById` mutation.
 */
public class UpdateDeckByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private DeckPatch deckPatch;

    public UpdateDeckByIdInput() {
    }

    public UpdateDeckByIdInput(String clientMutationId, String id, DeckPatch deckPatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.deckPatch = deckPatch;
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

    public DeckPatch getDeckPatch() {
        return deckPatch;
    }
    public void setDeckPatch(DeckPatch deckPatch) {
        this.deckPatch = deckPatch;
    }



    public static UpdateDeckByIdInput.Builder builder() {
        return new UpdateDeckByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private DeckPatch deckPatch;

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

        public Builder setDeckPatch(DeckPatch deckPatch) {
            this.deckPatch = deckPatch;
            return this;
        }


        public UpdateDeckByIdInput build() {
            return new UpdateDeckByIdInput(clientMutationId, id, deckPatch);
        }

    }
}
