package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `DeckShare` mutation.
 */
public class CreateDeckShareInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private DeckShareInput deckShare;

    public CreateDeckShareInput() {
    }

    public CreateDeckShareInput(String clientMutationId, DeckShareInput deckShare) {
        this.clientMutationId = clientMutationId;
        this.deckShare = deckShare;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public DeckShareInput getDeckShare() {
        return deckShare;
    }
    public void setDeckShare(DeckShareInput deckShare) {
        this.deckShare = deckShare;
    }



    public static CreateDeckShareInput.Builder builder() {
        return new CreateDeckShareInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private DeckShareInput deckShare;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDeckShare(DeckShareInput deckShare) {
            this.deckShare = deckShare;
            return this;
        }


        public CreateDeckShareInput build() {
            return new CreateDeckShareInput(clientMutationId, deckShare);
        }

    }
}
