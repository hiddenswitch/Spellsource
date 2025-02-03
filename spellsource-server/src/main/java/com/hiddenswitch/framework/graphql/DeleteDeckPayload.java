package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `Deck` mutation.
 */
public class DeleteDeckPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Deck deck;
    private String deletedDeckId;
    private Query query;
    private DecksEdge deckEdge;

    public DeleteDeckPayload() {
    }

    public DeleteDeckPayload(String clientMutationId, Deck deck, String deletedDeckId, Query query, DecksEdge deckEdge) {
        this.clientMutationId = clientMutationId;
        this.deck = deck;
        this.deletedDeckId = deletedDeckId;
        this.query = query;
        this.deckEdge = deckEdge;
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
     * The `Deck` that was deleted by this mutation.
     */
    public Deck getDeck() {
        return deck;
    }
    /**
     * The `Deck` that was deleted by this mutation.
     */
    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getDeletedDeckId() {
        return deletedDeckId;
    }
    public void setDeletedDeckId(String deletedDeckId) {
        this.deletedDeckId = deletedDeckId;
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
     * An edge for our `Deck`. May be used by Relay 1.
     */
    public DecksEdge getDeckEdge() {
        return deckEdge;
    }
    /**
     * An edge for our `Deck`. May be used by Relay 1.
     */
    public void setDeckEdge(DecksEdge deckEdge) {
        this.deckEdge = deckEdge;
    }



    public static DeleteDeckPayload.Builder builder() {
        return new DeleteDeckPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private Deck deck;
        private String deletedDeckId;
        private Query query;
        private DecksEdge deckEdge;

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
         * The `Deck` that was deleted by this mutation.
         */
        public Builder setDeck(Deck deck) {
            this.deck = deck;
            return this;
        }

        public Builder setDeletedDeckId(String deletedDeckId) {
            this.deletedDeckId = deletedDeckId;
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
         * An edge for our `Deck`. May be used by Relay 1.
         */
        public Builder setDeckEdge(DecksEdge deckEdge) {
            this.deckEdge = deckEdge;
            return this;
        }


        public DeleteDeckPayload build() {
            return new DeleteDeckPayload(clientMutationId, deck, deletedDeckId, query, deckEdge);
        }

    }
}
