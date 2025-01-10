package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `DeckPlayerAttributeTuple` mutation.
 */
public class CreateDeckPlayerAttributeTupleInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private DeckPlayerAttributeTupleInput deckPlayerAttributeTuple;

    public CreateDeckPlayerAttributeTupleInput() {
    }

    public CreateDeckPlayerAttributeTupleInput(String clientMutationId, DeckPlayerAttributeTupleInput deckPlayerAttributeTuple) {
        this.clientMutationId = clientMutationId;
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public DeckPlayerAttributeTupleInput getDeckPlayerAttributeTuple() {
        return deckPlayerAttributeTuple;
    }
    public void setDeckPlayerAttributeTuple(DeckPlayerAttributeTupleInput deckPlayerAttributeTuple) {
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
    }



    public static CreateDeckPlayerAttributeTupleInput.Builder builder() {
        return new CreateDeckPlayerAttributeTupleInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private DeckPlayerAttributeTupleInput deckPlayerAttributeTuple;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDeckPlayerAttributeTuple(DeckPlayerAttributeTupleInput deckPlayerAttributeTuple) {
            this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
            return this;
        }


        public CreateDeckPlayerAttributeTupleInput build() {
            return new CreateDeckPlayerAttributeTupleInput(clientMutationId, deckPlayerAttributeTuple);
        }

    }
}
