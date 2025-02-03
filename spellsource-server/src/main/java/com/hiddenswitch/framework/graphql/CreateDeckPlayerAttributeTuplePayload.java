package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `DeckPlayerAttributeTuple` mutation.
 */
public class CreateDeckPlayerAttributeTuplePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private DeckPlayerAttributeTuple deckPlayerAttributeTuple;
    private Query query;
    private DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge;
    private Deck deckByDeckId;

    public CreateDeckPlayerAttributeTuplePayload() {
    }

    public CreateDeckPlayerAttributeTuplePayload(String clientMutationId, DeckPlayerAttributeTuple deckPlayerAttributeTuple, Query query, DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge, Deck deckByDeckId) {
        this.clientMutationId = clientMutationId;
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
        this.query = query;
        this.deckPlayerAttributeTupleEdge = deckPlayerAttributeTupleEdge;
        this.deckByDeckId = deckByDeckId;
    }

    /**
     * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
     */
    public String getClientMutationId() {
        return clientMutationId;
    }
    /**
     * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
     */
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    /**
     * The `DeckPlayerAttributeTuple` that was created by this mutation.
     */
    public DeckPlayerAttributeTuple getDeckPlayerAttributeTuple() {
        return deckPlayerAttributeTuple;
    }
    /**
     * The `DeckPlayerAttributeTuple` that was created by this mutation.
     */
    public void setDeckPlayerAttributeTuple(DeckPlayerAttributeTuple deckPlayerAttributeTuple) {
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
    }

    /**
     * Our root query field type. Allows us to run any query from our mutation payload.
     */
    public Query getQuery() {
        return query;
    }
    /**
     * Our root query field type. Allows us to run any query from our mutation payload.
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1.
     */
    public DeckPlayerAttributeTuplesEdge getDeckPlayerAttributeTupleEdge() {
        return deckPlayerAttributeTupleEdge;
    }
    /**
     * An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1.
     */
    public void setDeckPlayerAttributeTupleEdge(DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge) {
        this.deckPlayerAttributeTupleEdge = deckPlayerAttributeTupleEdge;
    }

    /**
     * Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }



    public static CreateDeckPlayerAttributeTuplePayload.Builder builder() {
        return new CreateDeckPlayerAttributeTuplePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private DeckPlayerAttributeTuple deckPlayerAttributeTuple;
        private Query query;
        private DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge;
        private Deck deckByDeckId;

        public Builder() {
        }

        /**
         * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
         */
        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        /**
         * The `DeckPlayerAttributeTuple` that was created by this mutation.
         */
        public Builder setDeckPlayerAttributeTuple(DeckPlayerAttributeTuple deckPlayerAttributeTuple) {
            this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }

        /**
         * An edge for our `DeckPlayerAttributeTuple`. May be used by Relay 1.
         */
        public Builder setDeckPlayerAttributeTupleEdge(DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge) {
            this.deckPlayerAttributeTupleEdge = deckPlayerAttributeTupleEdge;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }


        public CreateDeckPlayerAttributeTuplePayload build() {
            return new CreateDeckPlayerAttributeTuplePayload(clientMutationId, deckPlayerAttributeTuple, query, deckPlayerAttributeTupleEdge, deckByDeckId);
        }

    }
}
