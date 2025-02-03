package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateMatchmakingQueue` mutation.
 */
public class UpdateMatchmakingQueueInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private MatchmakingQueuePatch matchmakingQueuePatch;

    public UpdateMatchmakingQueueInput() {
    }

    public UpdateMatchmakingQueueInput(String clientMutationId, String nodeId, MatchmakingQueuePatch matchmakingQueuePatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.matchmakingQueuePatch = matchmakingQueuePatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public MatchmakingQueuePatch getMatchmakingQueuePatch() {
        return matchmakingQueuePatch;
    }
    public void setMatchmakingQueuePatch(MatchmakingQueuePatch matchmakingQueuePatch) {
        this.matchmakingQueuePatch = matchmakingQueuePatch;
    }



    public static UpdateMatchmakingQueueInput.Builder builder() {
        return new UpdateMatchmakingQueueInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private MatchmakingQueuePatch matchmakingQueuePatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setMatchmakingQueuePatch(MatchmakingQueuePatch matchmakingQueuePatch) {
            this.matchmakingQueuePatch = matchmakingQueuePatch;
            return this;
        }


        public UpdateMatchmakingQueueInput build() {
            return new UpdateMatchmakingQueueInput(clientMutationId, nodeId, matchmakingQueuePatch);
        }

    }
}
