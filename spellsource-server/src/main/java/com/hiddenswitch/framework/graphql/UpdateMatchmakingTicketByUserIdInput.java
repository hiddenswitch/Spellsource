package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateMatchmakingTicketByUserId` mutation.
 */
public class UpdateMatchmakingTicketByUserIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String userId;
    private MatchmakingTicketPatch matchmakingTicketPatch;

    public UpdateMatchmakingTicketByUserIdInput() {
    }

    public UpdateMatchmakingTicketByUserIdInput(String clientMutationId, String userId, MatchmakingTicketPatch matchmakingTicketPatch) {
        this.clientMutationId = clientMutationId;
        this.userId = userId;
        this.matchmakingTicketPatch = matchmakingTicketPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MatchmakingTicketPatch getMatchmakingTicketPatch() {
        return matchmakingTicketPatch;
    }
    public void setMatchmakingTicketPatch(MatchmakingTicketPatch matchmakingTicketPatch) {
        this.matchmakingTicketPatch = matchmakingTicketPatch;
    }



    public static UpdateMatchmakingTicketByUserIdInput.Builder builder() {
        return new UpdateMatchmakingTicketByUserIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String userId;
        private MatchmakingTicketPatch matchmakingTicketPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setMatchmakingTicketPatch(MatchmakingTicketPatch matchmakingTicketPatch) {
            this.matchmakingTicketPatch = matchmakingTicketPatch;
            return this;
        }


        public UpdateMatchmakingTicketByUserIdInput build() {
            return new UpdateMatchmakingTicketByUserIdInput(clientMutationId, userId, matchmakingTicketPatch);
        }

    }
}
