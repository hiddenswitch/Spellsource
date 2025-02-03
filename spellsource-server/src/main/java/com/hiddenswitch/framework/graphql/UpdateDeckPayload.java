package com.hiddenswitch.framework.graphql;


/**
 * The output of our update `Deck` mutation.
 */
public class UpdateDeckPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Deck deck;
    private Query query;
    private DecksEdge deckEdge;

    public UpdateDeckPayload() {
    }

    public UpdateDeckPayload(String clientMutationId, Deck deck, Query query, DecksEdge deckEdge) {
        this.clientMutationId = clientMutationId;
        this.deck = deck;
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
     * The `Deck` that was updated by this mutation.
     */
    public Deck getDeck() {
        return deck;
    }
    /**
     * The `Deck` that was updated by this mutation.
     */
    public void setDeck(Deck deck) {
        this.deck = deck;
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



    public static UpdateDeckPayload.Builder builder() {
        return new UpdateDeckPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private Deck deck;
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
         * The `Deck` that was updated by this mutation.
         */
        public Builder setDeck(Deck deck) {
            this.deck = deck;
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


        public UpdateDeckPayload build() {
            return new UpdateDeckPayload(clientMutationId, deck, query, deckEdge);
        }

    }
}
