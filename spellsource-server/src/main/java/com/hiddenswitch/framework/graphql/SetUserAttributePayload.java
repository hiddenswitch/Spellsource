package com.hiddenswitch.framework.graphql;


/**
 * The output of our `setUserAttribute` mutation.
 */
public class SetUserAttributePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Query query;

    public SetUserAttributePayload() {
    }

    public SetUserAttributePayload(String clientMutationId, Query query) {
        this.clientMutationId = clientMutationId;
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



    public static SetUserAttributePayload.Builder builder() {
        return new SetUserAttributePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
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

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public SetUserAttributePayload build() {
            return new SetUserAttributePayload(clientMutationId, query);
        }

    }
}
