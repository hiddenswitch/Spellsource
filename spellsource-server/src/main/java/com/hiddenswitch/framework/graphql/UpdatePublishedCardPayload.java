package com.hiddenswitch.framework.graphql;


/**
 * The output of our update `PublishedCard` mutation.
 */
public class UpdatePublishedCardPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private PublishedCard publishedCard;
    private Query query;
    private PublishedCardsEdge publishedCardEdge;
    private Card cardBySuccession;

    public UpdatePublishedCardPayload() {
    }

    public UpdatePublishedCardPayload(String clientMutationId, PublishedCard publishedCard, Query query, PublishedCardsEdge publishedCardEdge, Card cardBySuccession) {
        this.clientMutationId = clientMutationId;
        this.publishedCard = publishedCard;
        this.query = query;
        this.publishedCardEdge = publishedCardEdge;
        this.cardBySuccession = cardBySuccession;
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
     * The `PublishedCard` that was updated by this mutation.
     */
    public PublishedCard getPublishedCard() {
        return publishedCard;
    }
    /**
     * The `PublishedCard` that was updated by this mutation.
     */
    public void setPublishedCard(PublishedCard publishedCard) {
        this.publishedCard = publishedCard;
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
     * An edge for our `PublishedCard`. May be used by Relay 1.
     */
    public PublishedCardsEdge getPublishedCardEdge() {
        return publishedCardEdge;
    }
    /**
     * An edge for our `PublishedCard`. May be used by Relay 1.
     */
    public void setPublishedCardEdge(PublishedCardsEdge publishedCardEdge) {
        this.publishedCardEdge = publishedCardEdge;
    }

    /**
     * Reads a single `Card` that is related to this `PublishedCard`.
     */
    public Card getCardBySuccession() {
        return cardBySuccession;
    }
    /**
     * Reads a single `Card` that is related to this `PublishedCard`.
     */
    public void setCardBySuccession(Card cardBySuccession) {
        this.cardBySuccession = cardBySuccession;
    }



    public static UpdatePublishedCardPayload.Builder builder() {
        return new UpdatePublishedCardPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private PublishedCard publishedCard;
        private Query query;
        private PublishedCardsEdge publishedCardEdge;
        private Card cardBySuccession;

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
         * The `PublishedCard` that was updated by this mutation.
         */
        public Builder setPublishedCard(PublishedCard publishedCard) {
            this.publishedCard = publishedCard;
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
         * An edge for our `PublishedCard`. May be used by Relay 1.
         */
        public Builder setPublishedCardEdge(PublishedCardsEdge publishedCardEdge) {
            this.publishedCardEdge = publishedCardEdge;
            return this;
        }

        /**
         * Reads a single `Card` that is related to this `PublishedCard`.
         */
        public Builder setCardBySuccession(Card cardBySuccession) {
            this.cardBySuccession = cardBySuccession;
            return this;
        }


        public UpdatePublishedCardPayload build() {
            return new UpdatePublishedCardPayload(clientMutationId, publishedCard, query, publishedCardEdge, cardBySuccession);
        }

    }
}
