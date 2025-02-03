package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `MatchmakingTicket` mutation.
 */
public class CreateMatchmakingTicketInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private MatchmakingTicketInput matchmakingTicket;

    public CreateMatchmakingTicketInput() {
    }

    public CreateMatchmakingTicketInput(String clientMutationId, MatchmakingTicketInput matchmakingTicket) {
        this.clientMutationId = clientMutationId;
        this.matchmakingTicket = matchmakingTicket;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public MatchmakingTicketInput getMatchmakingTicket() {
        return matchmakingTicket;
    }
    public void setMatchmakingTicket(MatchmakingTicketInput matchmakingTicket) {
        this.matchmakingTicket = matchmakingTicket;
    }



    public static CreateMatchmakingTicketInput.Builder builder() {
        return new CreateMatchmakingTicketInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private MatchmakingTicketInput matchmakingTicket;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setMatchmakingTicket(MatchmakingTicketInput matchmakingTicket) {
            this.matchmakingTicket = matchmakingTicket;
            return this;
        }


        public CreateMatchmakingTicketInput build() {
            return new CreateMatchmakingTicketInput(clientMutationId, matchmakingTicket);
        }

    }
}
