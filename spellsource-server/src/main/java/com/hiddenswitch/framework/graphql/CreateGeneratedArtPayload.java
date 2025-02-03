package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `GeneratedArt` mutation.
 */
public class CreateGeneratedArtPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GeneratedArt generatedArt;
    private Query query;

    public CreateGeneratedArtPayload() {
    }

    public CreateGeneratedArtPayload(String clientMutationId, GeneratedArt generatedArt, Query query) {
        this.clientMutationId = clientMutationId;
        this.generatedArt = generatedArt;
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
     * The `GeneratedArt` that was created by this mutation.
     */
    public GeneratedArt getGeneratedArt() {
        return generatedArt;
    }
    /**
     * The `GeneratedArt` that was created by this mutation.
     */
    public void setGeneratedArt(GeneratedArt generatedArt) {
        this.generatedArt = generatedArt;
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



    public static CreateGeneratedArtPayload.Builder builder() {
        return new CreateGeneratedArtPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private GeneratedArt generatedArt;
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
         * The `GeneratedArt` that was created by this mutation.
         */
        public Builder setGeneratedArt(GeneratedArt generatedArt) {
            this.generatedArt = generatedArt;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }


        public CreateGeneratedArtPayload build() {
            return new CreateGeneratedArtPayload(clientMutationId, generatedArt, query);
        }

    }
}
