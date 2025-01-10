package com.hiddenswitch.framework.graphql;


/**
 * The output of our `publishCard` mutation.
 */
public class PublishCardPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String bigInt;
    private Query query;

    public PublishCardPayload() {
    }

    public PublishCardPayload(String clientMutationId, String bigInt, Query query) {
        this.clientMutationId = clientMutationId;
        this.bigInt = bigInt;
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

    public String getBigInt() {
        return bigInt;
    }
    public void setBigInt(String bigInt) {
        this.bigInt = bigInt;
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



    public static PublishCardPayload.Builder builder() {
        return new PublishCardPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String bigInt;
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

        public Builder setBigInt(String bigInt) {
            this.bigInt = bigInt;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public PublishCardPayload build() {
            return new PublishCardPayload(clientMutationId, bigInt, query);
        }

    }
}
