package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `MatchmakingQueue` mutation.
 */
public class CreateMatchmakingQueuePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private MatchmakingQueue matchmakingQueue;
    private Query query;
    private MatchmakingQueuesEdge matchmakingQueueEdge;

    public CreateMatchmakingQueuePayload() {
    }

    public CreateMatchmakingQueuePayload(String clientMutationId, MatchmakingQueue matchmakingQueue, Query query, MatchmakingQueuesEdge matchmakingQueueEdge) {
        this.clientMutationId = clientMutationId;
        this.matchmakingQueue = matchmakingQueue;
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
     * The `MatchmakingQueue` that was created by this mutation.
     */
    public MatchmakingQueue getMatchmakingQueue() {
        return matchmakingQueue;
    }
    /**
     * The `MatchmakingQueue` that was created by this mutation.
     */
    public void setMatchmakingQueue(MatchmakingQueue matchmakingQueue) {
        this.matchmakingQueue = matchmakingQueue;
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



    public static CreateMatchmakingQueuePayload.Builder builder() {
        return new CreateMatchmakingQueuePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private MatchmakingQueue matchmakingQueue;
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
         * The `MatchmakingQueue` that was created by this mutation.
         */
        public Builder setMatchmakingQueue(MatchmakingQueue matchmakingQueue) {
            this.matchmakingQueue = matchmakingQueue;
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


        public CreateMatchmakingQueuePayload build() {
            return new CreateMatchmakingQueuePayload(clientMutationId, matchmakingQueue, query, matchmakingQueueEdge);
        }

    }
}
