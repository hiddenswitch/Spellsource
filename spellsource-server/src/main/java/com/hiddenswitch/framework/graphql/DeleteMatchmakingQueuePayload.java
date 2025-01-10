package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `MatchmakingQueue` mutation.
 */
public class DeleteMatchmakingQueuePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private MatchmakingQueue matchmakingQueue;
    private String deletedMatchmakingQueueId;
    private Query query;
    private MatchmakingQueuesEdge matchmakingQueueEdge;

    public DeleteMatchmakingQueuePayload() {
    }

    public DeleteMatchmakingQueuePayload(String clientMutationId, MatchmakingQueue matchmakingQueue, String deletedMatchmakingQueueId, Query query, MatchmakingQueuesEdge matchmakingQueueEdge) {
        this.clientMutationId = clientMutationId;
        this.matchmakingQueue = matchmakingQueue;
        this.deletedMatchmakingQueueId = deletedMatchmakingQueueId;
        this.query = query;
        this.matchmakingQueueEdge = matchmakingQueueEdge;
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
     * The `MatchmakingQueue` that was deleted by this mutation.
     */
    public MatchmakingQueue getMatchmakingQueue() {
        return matchmakingQueue;
    }
    /**
     * The `MatchmakingQueue` that was deleted by this mutation.
     */
    public void setMatchmakingQueue(MatchmakingQueue matchmakingQueue) {
        this.matchmakingQueue = matchmakingQueue;
    }

    public String getDeletedMatchmakingQueueId() {
        return deletedMatchmakingQueueId;
    }
    public void setDeletedMatchmakingQueueId(String deletedMatchmakingQueueId) {
        this.deletedMatchmakingQueueId = deletedMatchmakingQueueId;
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
     * An edge for our `MatchmakingQueue`. May be used by Relay 1.
     */
    public MatchmakingQueuesEdge getMatchmakingQueueEdge() {
        return matchmakingQueueEdge;
    }
    /**
     * An edge for our `MatchmakingQueue`. May be used by Relay 1.
     */
    public void setMatchmakingQueueEdge(MatchmakingQueuesEdge matchmakingQueueEdge) {
        this.matchmakingQueueEdge = matchmakingQueueEdge;
    }



    public static DeleteMatchmakingQueuePayload.Builder builder() {
        return new DeleteMatchmakingQueuePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private MatchmakingQueue matchmakingQueue;
        private String deletedMatchmakingQueueId;
        private Query query;
        private MatchmakingQueuesEdge matchmakingQueueEdge;

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
         * The `MatchmakingQueue` that was deleted by this mutation.
         */
        public Builder setMatchmakingQueue(MatchmakingQueue matchmakingQueue) {
            this.matchmakingQueue = matchmakingQueue;
            return this;
        }

        public Builder setDeletedMatchmakingQueueId(String deletedMatchmakingQueueId) {
            this.deletedMatchmakingQueueId = deletedMatchmakingQueueId;
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
         * An edge for our `MatchmakingQueue`. May be used by Relay 1.
         */
        public Builder setMatchmakingQueueEdge(MatchmakingQueuesEdge matchmakingQueueEdge) {
            this.matchmakingQueueEdge = matchmakingQueueEdge;
            return this;
        }


        public DeleteMatchmakingQueuePayload build() {
            return new DeleteMatchmakingQueuePayload(clientMutationId, matchmakingQueue, deletedMatchmakingQueueId, query, matchmakingQueueEdge);
        }

    }
}
