package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `DeckShare` mutation.
 */
public class DeleteDeckSharePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private DeckShare deckShare;
    private String deletedDeckShareId;
    private Query query;
    private DeckSharesEdge deckShareEdge;
    private Deck deckByDeckId;

    public DeleteDeckSharePayload() {
    }

    public DeleteDeckSharePayload(String clientMutationId, DeckShare deckShare, String deletedDeckShareId, Query query, DeckSharesEdge deckShareEdge, Deck deckByDeckId) {
        this.clientMutationId = clientMutationId;
        this.deckShare = deckShare;
        this.deletedDeckShareId = deletedDeckShareId;
        this.query = query;
        this.deckShareEdge = deckShareEdge;
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
     * The `DeckShare` that was deleted by this mutation.
     */
    public DeckShare getDeckShare() {
        return deckShare;
    }
    /**
     * The `DeckShare` that was deleted by this mutation.
     */
    public void setDeckShare(DeckShare deckShare) {
        this.deckShare = deckShare;
    }

    public String getDeletedDeckShareId() {
        return deletedDeckShareId;
    }
    public void setDeletedDeckShareId(String deletedDeckShareId) {
        this.deletedDeckShareId = deletedDeckShareId;
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
     * An edge for our `DeckShare`. May be used by Relay 1.
     */
    public DeckSharesEdge getDeckShareEdge() {
        return deckShareEdge;
    }
    /**
     * An edge for our `DeckShare`. May be used by Relay 1.
     */
    public void setDeckShareEdge(DeckSharesEdge deckShareEdge) {
        this.deckShareEdge = deckShareEdge;
    }

    /**
     * Reads a single `Deck` that is related to this `DeckShare`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `DeckShare`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }



    public static DeleteDeckSharePayload.Builder builder() {
        return new DeleteDeckSharePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private DeckShare deckShare;
        private String deletedDeckShareId;
        private Query query;
        private DeckSharesEdge deckShareEdge;
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
         * The `DeckShare` that was deleted by this mutation.
         */
        public Builder setDeckShare(DeckShare deckShare) {
            this.deckShare = deckShare;
            return this;
        }

        public Builder setDeletedDeckShareId(String deletedDeckShareId) {
            this.deletedDeckShareId = deletedDeckShareId;
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
         * An edge for our `DeckShare`. May be used by Relay 1.
         */
        public Builder setDeckShareEdge(DeckSharesEdge deckShareEdge) {
            this.deckShareEdge = deckShareEdge;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `DeckShare`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }


        public DeleteDeckSharePayload build() {
            return new DeleteDeckSharePayload(clientMutationId, deckShare, deletedDeckShareId, query, deckShareEdge, deckByDeckId);
        }

    }
}
