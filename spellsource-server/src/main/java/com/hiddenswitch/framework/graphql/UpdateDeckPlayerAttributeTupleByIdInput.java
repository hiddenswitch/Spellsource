package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateDeckPlayerAttributeTupleById` mutation.
 */
public class UpdateDeckPlayerAttributeTupleByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch;

    public UpdateDeckPlayerAttributeTupleByIdInput() {
    }

    public UpdateDeckPlayerAttributeTupleByIdInput(String clientMutationId, String id, DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.deckPlayerAttributeTuplePatch = deckPlayerAttributeTuplePatch;
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

    public DeckPlayerAttributeTuplePatch getDeckPlayerAttributeTuplePatch() {
        return deckPlayerAttributeTuplePatch;
    }
    public void setDeckPlayerAttributeTuplePatch(DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch) {
        this.deckPlayerAttributeTuplePatch = deckPlayerAttributeTuplePatch;
    }



    public static UpdateDeckPlayerAttributeTupleByIdInput.Builder builder() {
        return new UpdateDeckPlayerAttributeTupleByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch;

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

        public Builder setDeckPlayerAttributeTuplePatch(DeckPlayerAttributeTuplePatch deckPlayerAttributeTuplePatch) {
            this.deckPlayerAttributeTuplePatch = deckPlayerAttributeTuplePatch;
            return this;
        }


        public UpdateDeckPlayerAttributeTupleByIdInput build() {
            return new UpdateDeckPlayerAttributeTupleByIdInput(clientMutationId, id, deckPlayerAttributeTuplePatch);
        }

    }
}
