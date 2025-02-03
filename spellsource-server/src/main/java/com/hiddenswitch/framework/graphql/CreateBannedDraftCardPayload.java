package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `BannedDraftCard` mutation.
 */
public class CreateBannedDraftCardPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private BannedDraftCard bannedDraftCard;
    private Query query;
    private BannedDraftCardsEdge bannedDraftCardEdge;

    public CreateBannedDraftCardPayload() {
    }

    public CreateBannedDraftCardPayload(String clientMutationId, BannedDraftCard bannedDraftCard, Query query, BannedDraftCardsEdge bannedDraftCardEdge) {
        this.clientMutationId = clientMutationId;
        this.bannedDraftCard = bannedDraftCard;
        this.query = query;
        this.bannedDraftCardEdge = bannedDraftCardEdge;
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
     * The `BannedDraftCard` that was created by this mutation.
     */
    public BannedDraftCard getBannedDraftCard() {
        return bannedDraftCard;
    }
    /**
     * The `BannedDraftCard` that was created by this mutation.
     */
    public void setBannedDraftCard(BannedDraftCard bannedDraftCard) {
        this.bannedDraftCard = bannedDraftCard;
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
     * An edge for our `BannedDraftCard`. May be used by Relay 1.
     */
    public BannedDraftCardsEdge getBannedDraftCardEdge() {
        return bannedDraftCardEdge;
    }
    /**
     * An edge for our `BannedDraftCard`. May be used by Relay 1.
     */
    public void setBannedDraftCardEdge(BannedDraftCardsEdge bannedDraftCardEdge) {
        this.bannedDraftCardEdge = bannedDraftCardEdge;
    }



    public static CreateBannedDraftCardPayload.Builder builder() {
        return new CreateBannedDraftCardPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private BannedDraftCard bannedDraftCard;
        private Query query;
        private BannedDraftCardsEdge bannedDraftCardEdge;

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
         * The `BannedDraftCard` that was created by this mutation.
         */
        public Builder setBannedDraftCard(BannedDraftCard bannedDraftCard) {
            this.bannedDraftCard = bannedDraftCard;
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
         * An edge for our `BannedDraftCard`. May be used by Relay 1.
         */
        public Builder setBannedDraftCardEdge(BannedDraftCardsEdge bannedDraftCardEdge) {
            this.bannedDraftCardEdge = bannedDraftCardEdge;
            return this;
        }


        public CreateBannedDraftCardPayload build() {
            return new CreateBannedDraftCardPayload(clientMutationId, bannedDraftCard, query, bannedDraftCardEdge);
        }

    }
}
