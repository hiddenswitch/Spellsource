package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `HardRemovalCard` mutation.
 */
public class CreateHardRemovalCardPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private HardRemovalCard hardRemovalCard;
    private Query query;
    private HardRemovalCardsEdge hardRemovalCardEdge;

    public CreateHardRemovalCardPayload() {
    }

    public CreateHardRemovalCardPayload(String clientMutationId, HardRemovalCard hardRemovalCard, Query query, HardRemovalCardsEdge hardRemovalCardEdge) {
        this.clientMutationId = clientMutationId;
        this.hardRemovalCard = hardRemovalCard;
        this.query = query;
        this.hardRemovalCardEdge = hardRemovalCardEdge;
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
     * The `HardRemovalCard` that was created by this mutation.
     */
    public HardRemovalCard getHardRemovalCard() {
        return hardRemovalCard;
    }
    /**
     * The `HardRemovalCard` that was created by this mutation.
     */
    public void setHardRemovalCard(HardRemovalCard hardRemovalCard) {
        this.hardRemovalCard = hardRemovalCard;
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
     * An edge for our `HardRemovalCard`. May be used by Relay 1.
     */
    public HardRemovalCardsEdge getHardRemovalCardEdge() {
        return hardRemovalCardEdge;
    }
    /**
     * An edge for our `HardRemovalCard`. May be used by Relay 1.
     */
    public void setHardRemovalCardEdge(HardRemovalCardsEdge hardRemovalCardEdge) {
        this.hardRemovalCardEdge = hardRemovalCardEdge;
    }



    public static CreateHardRemovalCardPayload.Builder builder() {
        return new CreateHardRemovalCardPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private HardRemovalCard hardRemovalCard;
        private Query query;
        private HardRemovalCardsEdge hardRemovalCardEdge;

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
         * The `HardRemovalCard` that was created by this mutation.
         */
        public Builder setHardRemovalCard(HardRemovalCard hardRemovalCard) {
            this.hardRemovalCard = hardRemovalCard;
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
         * An edge for our `HardRemovalCard`. May be used by Relay 1.
         */
        public Builder setHardRemovalCardEdge(HardRemovalCardsEdge hardRemovalCardEdge) {
            this.hardRemovalCardEdge = hardRemovalCardEdge;
            return this;
        }


        public CreateHardRemovalCardPayload build() {
            return new CreateHardRemovalCardPayload(clientMutationId, hardRemovalCard, query, hardRemovalCardEdge);
        }

    }
}
