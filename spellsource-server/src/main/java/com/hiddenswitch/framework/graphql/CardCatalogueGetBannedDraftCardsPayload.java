package com.hiddenswitch.framework.graphql;


/**
 * The output of our `cardCatalogueGetBannedDraftCards` mutation.
 */
public class CardCatalogueGetBannedDraftCardsPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private java.util.List<String> cardIds;
    private Query query;

    public CardCatalogueGetBannedDraftCardsPayload() {
    }

    public CardCatalogueGetBannedDraftCardsPayload(String clientMutationId, java.util.List<String> cardIds, Query query) {
        this.clientMutationId = clientMutationId;
        this.cardIds = cardIds;
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

    public java.util.List<String> getCardIds() {
        return cardIds;
    }
    public void setCardIds(java.util.List<String> cardIds) {
        this.cardIds = cardIds;
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



    public static CardCatalogueGetBannedDraftCardsPayload.Builder builder() {
        return new CardCatalogueGetBannedDraftCardsPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private java.util.List<String> cardIds;
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

        public Builder setCardIds(java.util.List<String> cardIds) {
            this.cardIds = cardIds;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public CardCatalogueGetBannedDraftCardsPayload build() {
            return new CardCatalogueGetBannedDraftCardsPayload(clientMutationId, cardIds, query);
        }

    }
}
