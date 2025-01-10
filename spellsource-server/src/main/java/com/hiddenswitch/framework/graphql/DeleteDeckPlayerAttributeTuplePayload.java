package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `DeckPlayerAttributeTuple` mutation.
 */
public class DeleteDeckPlayerAttributeTuplePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private DeckPlayerAttributeTuple deckPlayerAttributeTuple;
    private String deletedDeckPlayerAttributeTupleId;
    private Query query;
    private DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge;
    private Deck deckByDeckId;

    public DeleteDeckPlayerAttributeTuplePayload() {
    }

    public DeleteDeckPlayerAttributeTuplePayload(String clientMutationId, DeckPlayerAttributeTuple deckPlayerAttributeTuple, String deletedDeckPlayerAttributeTupleId, Query query, DeckPlayerAttributeTuplesEdge deckPlayerAttributeTupleEdge, Deck deckByDeckId) {
        this.clientMutationId = clientMutationId;
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
        this.deletedDeckPlayerAttributeTupleId = deletedDeckPlayerAttributeTupleId;
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
     * The `DeckPlayerAttributeTuple` that was deleted by this mutation.
     */
    public DeckPlayerAttributeTuple getDeckPlayerAttributeTuple() {
        return deckPlayerAttributeTuple;
    }
    /**
     * The `DeckPlayerAttributeTuple` that was deleted by this mutation.
     */
    public void setDeckPlayerAttributeTuple(DeckPlayerAttributeTuple deckPlayerAttributeTuple) {
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
    }

    public String getDeletedDeckPlayerAttributeTupleId() {
        return deletedDeckPlayerAttributeTupleId;
    }
    public void setDeletedDeckPlayerAttributeTupleId(String deletedDeckPlayerAttributeTupleId) {
        this.deletedDeckPlayerAttributeTupleId = deletedDeckPlayerAttributeTupleId;
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



    public static DeleteDeckPlayerAttributeTuplePayload.Builder builder() {
        return new DeleteDeckPlayerAttributeTuplePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private DeckPlayerAttributeTuple deckPlayerAttributeTuple;
        private String deletedDeckPlayerAttributeTupleId;
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
         * The `DeckPlayerAttributeTuple` that was deleted by this mutation.
         */
        public Builder setDeckPlayerAttributeTuple(DeckPlayerAttributeTuple deckPlayerAttributeTuple) {
            this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
            return this;
        }

        public Builder setDeletedDeckPlayerAttributeTupleId(String deletedDeckPlayerAttributeTupleId) {
            this.deletedDeckPlayerAttributeTupleId = deletedDeckPlayerAttributeTupleId;
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


        public DeleteDeckPlayerAttributeTuplePayload build() {
            return new DeleteDeckPlayerAttributeTuplePayload(clientMutationId, deckPlayerAttributeTuple, deletedDeckPlayerAttributeTupleId, query, deckPlayerAttributeTupleEdge, deckByDeckId);
        }

    }
}
