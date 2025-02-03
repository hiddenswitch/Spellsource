package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `HardRemovalCard` mutation.
 */
public class DeleteHardRemovalCardPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private HardRemovalCard hardRemovalCard;
    private String deletedHardRemovalCardId;
    private Query query;
    private HardRemovalCardsEdge hardRemovalCardEdge;

    public DeleteHardRemovalCardPayload() {
    }

    public DeleteHardRemovalCardPayload(String clientMutationId, HardRemovalCard hardRemovalCard, String deletedHardRemovalCardId, Query query, HardRemovalCardsEdge hardRemovalCardEdge) {
        this.clientMutationId = clientMutationId;
        this.hardRemovalCard = hardRemovalCard;
        this.deletedHardRemovalCardId = deletedHardRemovalCardId;
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
     * The `HardRemovalCard` that was deleted by this mutation.
     */
    public HardRemovalCard getHardRemovalCard() {
        return hardRemovalCard;
    }
    /**
     * The `HardRemovalCard` that was deleted by this mutation.
     */
    public void setHardRemovalCard(HardRemovalCard hardRemovalCard) {
        this.hardRemovalCard = hardRemovalCard;
    }

    public String getDeletedHardRemovalCardId() {
        return deletedHardRemovalCardId;
    }
    public void setDeletedHardRemovalCardId(String deletedHardRemovalCardId) {
        this.deletedHardRemovalCardId = deletedHardRemovalCardId;
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



    public static DeleteHardRemovalCardPayload.Builder builder() {
        return new DeleteHardRemovalCardPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private HardRemovalCard hardRemovalCard;
        private String deletedHardRemovalCardId;
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
         * The `HardRemovalCard` that was deleted by this mutation.
         */
        public Builder setHardRemovalCard(HardRemovalCard hardRemovalCard) {
            this.hardRemovalCard = hardRemovalCard;
            return this;
        }

        public Builder setDeletedHardRemovalCardId(String deletedHardRemovalCardId) {
            this.deletedHardRemovalCardId = deletedHardRemovalCardId;
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


        public DeleteHardRemovalCardPayload build() {
            return new DeleteHardRemovalCardPayload(clientMutationId, hardRemovalCard, deletedHardRemovalCardId, query, hardRemovalCardEdge);
        }

    }
}
