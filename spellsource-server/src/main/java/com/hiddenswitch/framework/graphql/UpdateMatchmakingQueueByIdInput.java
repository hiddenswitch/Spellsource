package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateMatchmakingQueueById` mutation.
 */
public class UpdateMatchmakingQueueByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private MatchmakingQueuePatch matchmakingQueuePatch;

    public UpdateMatchmakingQueueByIdInput() {
    }

    public UpdateMatchmakingQueueByIdInput(String clientMutationId, String id, MatchmakingQueuePatch matchmakingQueuePatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.matchmakingQueuePatch = matchmakingQueuePatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public MatchmakingQueuePatch getMatchmakingQueuePatch() {
        return matchmakingQueuePatch;
    }
    public void setMatchmakingQueuePatch(MatchmakingQueuePatch matchmakingQueuePatch) {
        this.matchmakingQueuePatch = matchmakingQueuePatch;
    }



    public static UpdateMatchmakingQueueByIdInput.Builder builder() {
        return new UpdateMatchmakingQueueByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private MatchmakingQueuePatch matchmakingQueuePatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setMatchmakingQueuePatch(MatchmakingQueuePatch matchmakingQueuePatch) {
            this.matchmakingQueuePatch = matchmakingQueuePatch;
            return this;
        }


        public UpdateMatchmakingQueueByIdInput build() {
            return new UpdateMatchmakingQueueByIdInput(clientMutationId, id, matchmakingQueuePatch);
        }

    }
}
