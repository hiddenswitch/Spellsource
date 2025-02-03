package com.hiddenswitch.framework.graphql;


/**
 * The output of our `cardCatalogueGetFormat` mutation.
 */
public class CardCatalogueGetFormatPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private java.util.List<Card> cards;
    private Query query;

    public CardCatalogueGetFormatPayload() {
    }

    public CardCatalogueGetFormatPayload(String clientMutationId, java.util.List<Card> cards, Query query) {
        this.clientMutationId = clientMutationId;
        this.cards = cards;
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

    public java.util.List<Card> getCards() {
        return cards;
    }
    public void setCards(java.util.List<Card> cards) {
        this.cards = cards;
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



    public static CardCatalogueGetFormatPayload.Builder builder() {
        return new CardCatalogueGetFormatPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private java.util.List<Card> cards;
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

        public Builder setCards(java.util.List<Card> cards) {
            this.cards = cards;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public CardCatalogueGetFormatPayload build() {
            return new CardCatalogueGetFormatPayload(clientMutationId, cards, query);
        }

    }
}
