package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateMatchmakingTicket` mutation.
 */
public class UpdateMatchmakingTicketInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private MatchmakingTicketPatch matchmakingTicketPatch;

    public UpdateMatchmakingTicketInput() {
    }

    public UpdateMatchmakingTicketInput(String clientMutationId, String nodeId, MatchmakingTicketPatch matchmakingTicketPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.matchmakingTicketPatch = matchmakingTicketPatch;
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

    public MatchmakingTicketPatch getMatchmakingTicketPatch() {
        return matchmakingTicketPatch;
    }
    public void setMatchmakingTicketPatch(MatchmakingTicketPatch matchmakingTicketPatch) {
        this.matchmakingTicketPatch = matchmakingTicketPatch;
    }



    public static UpdateMatchmakingTicketInput.Builder builder() {
        return new UpdateMatchmakingTicketInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private MatchmakingTicketPatch matchmakingTicketPatch;

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

        public Builder setMatchmakingTicketPatch(MatchmakingTicketPatch matchmakingTicketPatch) {
            this.matchmakingTicketPatch = matchmakingTicketPatch;
            return this;
        }


        public UpdateMatchmakingTicketInput build() {
            return new UpdateMatchmakingTicketInput(clientMutationId, nodeId, matchmakingTicketPatch);
        }

    }
}
