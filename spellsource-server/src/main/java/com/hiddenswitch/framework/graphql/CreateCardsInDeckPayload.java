package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `CardsInDeck` mutation.
 */
public class CreateCardsInDeckPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private CardsInDeck cardsInDeck;
    private Query query;
    private CardsInDecksEdge cardsInDeckEdge;
    private PublishedCard publishedCardByCardId;
    private Deck deckByDeckId;

    public CreateCardsInDeckPayload() {
    }

    public CreateCardsInDeckPayload(String clientMutationId, CardsInDeck cardsInDeck, Query query, CardsInDecksEdge cardsInDeckEdge, PublishedCard publishedCardByCardId, Deck deckByDeckId) {
        this.clientMutationId = clientMutationId;
        this.cardsInDeck = cardsInDeck;
        this.query = query;
        this.cardsInDeckEdge = cardsInDeckEdge;
        this.publishedCardByCardId = publishedCardByCardId;
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
     * The `CardsInDeck` that was created by this mutation.
     */
    public CardsInDeck getCardsInDeck() {
        return cardsInDeck;
    }
    /**
     * The `CardsInDeck` that was created by this mutation.
     */
    public void setCardsInDeck(CardsInDeck cardsInDeck) {
        this.cardsInDeck = cardsInDeck;
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
     * An edge for our `CardsInDeck`. May be used by Relay 1.
     */
    public CardsInDecksEdge getCardsInDeckEdge() {
        return cardsInDeckEdge;
    }
    /**
     * An edge for our `CardsInDeck`. May be used by Relay 1.
     */
    public void setCardsInDeckEdge(CardsInDecksEdge cardsInDeckEdge) {
        this.cardsInDeckEdge = cardsInDeckEdge;
    }

    /**
     * Reads a single `PublishedCard` that is related to this `CardsInDeck`.
     */
    public PublishedCard getPublishedCardByCardId() {
        return publishedCardByCardId;
    }
    /**
     * Reads a single `PublishedCard` that is related to this `CardsInDeck`.
     */
    public void setPublishedCardByCardId(PublishedCard publishedCardByCardId) {
        this.publishedCardByCardId = publishedCardByCardId;
    }

    /**
     * Reads a single `Deck` that is related to this `CardsInDeck`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `CardsInDeck`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }



    public static CreateCardsInDeckPayload.Builder builder() {
        return new CreateCardsInDeckPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private CardsInDeck cardsInDeck;
        private Query query;
        private CardsInDecksEdge cardsInDeckEdge;
        private PublishedCard publishedCardByCardId;
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
         * The `CardsInDeck` that was created by this mutation.
         */
        public Builder setCardsInDeck(CardsInDeck cardsInDeck) {
            this.cardsInDeck = cardsInDeck;
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
         * An edge for our `CardsInDeck`. May be used by Relay 1.
         */
        public Builder setCardsInDeckEdge(CardsInDecksEdge cardsInDeckEdge) {
            this.cardsInDeckEdge = cardsInDeckEdge;
            return this;
        }

        /**
         * Reads a single `PublishedCard` that is related to this `CardsInDeck`.
         */
        public Builder setPublishedCardByCardId(PublishedCard publishedCardByCardId) {
            this.publishedCardByCardId = publishedCardByCardId;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `CardsInDeck`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }


        public CreateCardsInDeckPayload build() {
            return new CreateCardsInDeckPayload(clientMutationId, cardsInDeck, query, cardsInDeckEdge, publishedCardByCardId, deckByDeckId);
        }

    }
}
