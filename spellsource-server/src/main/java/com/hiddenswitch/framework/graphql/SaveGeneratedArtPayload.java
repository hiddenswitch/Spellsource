package com.hiddenswitch.framework.graphql;


/**
 * The output of our `saveGeneratedArt` mutation.
 */
public class SaveGeneratedArtPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GeneratedArt generatedArt;
    private Query query;

    public SaveGeneratedArtPayload() {
    }

    public SaveGeneratedArtPayload(String clientMutationId, GeneratedArt generatedArt, Query query) {
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

    public GeneratedArt getGeneratedArt() {
        return generatedArt;
    }
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



    public static SaveGeneratedArtPayload.Builder builder() {
        return new SaveGeneratedArtPayload.Builder();
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


        public SaveGeneratedArtPayload build() {
            return new SaveGeneratedArtPayload(clientMutationId, generatedArt, query);
        }

    }
}
