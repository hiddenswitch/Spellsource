package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `Card` mutation.
 */
public class CreateCardPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Card card;
    private Query query;
    private CardsEdge cardEdge;

    public CreateCardPayload() {
    }

    public CreateCardPayload(String clientMutationId, Card card, Query query, CardsEdge cardEdge) {
        this.clientMutationId = clientMutationId;
        this.card = card;
        this.query = query;
        this.cardEdge = cardEdge;
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
     * The `Card` that was created by this mutation.
     */
    public Card getCard() {
        return card;
    }
    /**
     * The `Card` that was created by this mutation.
     */
    public void setCard(Card card) {
        this.card = card;
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
     * An edge for our `Card`. May be used by Relay 1.
     */
    public CardsEdge getCardEdge() {
        return cardEdge;
    }
    /**
     * An edge for our `Card`. May be used by Relay 1.
     */
    public void setCardEdge(CardsEdge cardEdge) {
        this.cardEdge = cardEdge;
    }



    public static CreateCardPayload.Builder builder() {
        return new CreateCardPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private Card card;
        private Query query;
        private CardsEdge cardEdge;

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
         * The `Card` that was created by this mutation.
         */
        public Builder setCard(Card card) {
            this.card = card;
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
         * An edge for our `Card`. May be used by Relay 1.
         */
        public Builder setCardEdge(CardsEdge cardEdge) {
            this.cardEdge = cardEdge;
            return this;
        }


        public CreateCardPayload build() {
            return new CreateCardPayload(clientMutationId, card, query, cardEdge);
        }

    }
}
