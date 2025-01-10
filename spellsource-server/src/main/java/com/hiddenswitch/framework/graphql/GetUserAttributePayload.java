package com.hiddenswitch.framework.graphql;


/**
 * The output of our `getUserAttribute` mutation.
 */
public class GetUserAttributePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String string;
    private Query query;

    public GetUserAttributePayload() {
    }

    public GetUserAttributePayload(String clientMutationId, String string, Query query) {
        this.clientMutationId = clientMutationId;
        this.string = string;
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

    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
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



    public static GetUserAttributePayload.Builder builder() {
        return new GetUserAttributePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String string;
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

        public Builder setString(String string) {
            this.string = string;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public GetUserAttributePayload build() {
            return new GetUserAttributePayload(clientMutationId, string, query);
        }

    }
}
