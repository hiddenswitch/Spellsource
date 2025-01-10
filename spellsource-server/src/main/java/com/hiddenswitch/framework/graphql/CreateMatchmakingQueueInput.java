package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `MatchmakingQueue` mutation.
 */
public class CreateMatchmakingQueueInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private MatchmakingQueueInput matchmakingQueue;

    public CreateMatchmakingQueueInput() {
    }

    public CreateMatchmakingQueueInput(String clientMutationId, MatchmakingQueueInput matchmakingQueue) {
        this.clientMutationId = clientMutationId;
        this.matchmakingQueue = matchmakingQueue;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public MatchmakingQueueInput getMatchmakingQueue() {
        return matchmakingQueue;
    }
    public void setMatchmakingQueue(MatchmakingQueueInput matchmakingQueue) {
        this.matchmakingQueue = matchmakingQueue;
    }



    public static CreateMatchmakingQueueInput.Builder builder() {
        return new CreateMatchmakingQueueInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private MatchmakingQueueInput matchmakingQueue;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setMatchmakingQueue(MatchmakingQueueInput matchmakingQueue) {
            this.matchmakingQueue = matchmakingQueue;
            return this;
        }


        public CreateMatchmakingQueueInput build() {
            return new CreateMatchmakingQueueInput(clientMutationId, matchmakingQueue);
        }

    }
}
