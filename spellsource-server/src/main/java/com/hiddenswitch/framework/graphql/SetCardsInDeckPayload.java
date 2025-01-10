package com.hiddenswitch.framework.graphql;


/**
 * The output of our `setCardsInDeck` mutation.
 */
public class SetCardsInDeckPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private java.util.List<CardsInDeck> cardsInDecks;
    private Query query;

    public SetCardsInDeckPayload() {
    }

    public SetCardsInDeckPayload(String clientMutationId, java.util.List<CardsInDeck> cardsInDecks, Query query) {
        this.clientMutationId = clientMutationId;
        this.cardsInDecks = cardsInDecks;
        this.query = query;
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

    public java.util.List<CardsInDeck> getCardsInDecks() {
        return cardsInDecks;
    }
    public void setCardsInDecks(java.util.List<CardsInDeck> cardsInDecks) {
        this.cardsInDecks = cardsInDecks;
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



    public static SetCardsInDeckPayload.Builder builder() {
        return new SetCardsInDeckPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private java.util.List<CardsInDeck> cardsInDecks;
        private Query query;

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

        public Builder setCardsInDecks(java.util.List<CardsInDeck> cardsInDecks) {
            this.cardsInDecks = cardsInDecks;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public SetCardsInDeckPayload build() {
            return new SetCardsInDeckPayload(clientMutationId, cardsInDecks, query);
        }

    }
}
