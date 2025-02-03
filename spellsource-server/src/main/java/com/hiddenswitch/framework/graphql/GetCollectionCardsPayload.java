package com.hiddenswitch.framework.graphql;


/**
 * The output of our `getCollectionCards` mutation.
 */
public class GetCollectionCardsPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private java.util.List<GetCollectionCardsRecord> results;
    private Query query;

    public GetCollectionCardsPayload() {
    }

    public GetCollectionCardsPayload(String clientMutationId, java.util.List<GetCollectionCardsRecord> results, Query query) {
        this.clientMutationId = clientMutationId;
        this.results = results;
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

    public java.util.List<GetCollectionCardsRecord> getResults() {
        return results;
    }
    public void setResults(java.util.List<GetCollectionCardsRecord> results) {
        this.results = results;
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



    public static GetCollectionCardsPayload.Builder builder() {
        return new GetCollectionCardsPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private java.util.List<GetCollectionCardsRecord> results;
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

        public Builder setResults(java.util.List<GetCollectionCardsRecord> results) {
            this.results = results;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public GetCollectionCardsPayload build() {
            return new GetCollectionCardsPayload(clientMutationId, results, query);
        }

    }
}
