package com.hiddenswitch.framework.graphql;


/**
 * All input for the `clusteredGamesUpdateGameAndUsers` mutation.
 */
public class ClusteredGamesUpdateGameAndUsersInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String pUserIdWinner;
    private String pUserIdLoser;
    private String pGameId;
    private String pTrace;

    public ClusteredGamesUpdateGameAndUsersInput() {
    }

    public ClusteredGamesUpdateGameAndUsersInput(String clientMutationId, String pUserIdWinner, String pUserIdLoser, String pGameId, String pTrace) {
        this.clientMutationId = clientMutationId;
        this.pUserIdWinner = pUserIdWinner;
        this.pUserIdLoser = pUserIdLoser;
        this.pGameId = pGameId;
        this.pTrace = pTrace;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getPUserIdWinner() {
        return pUserIdWinner;
    }
    public void setPUserIdWinner(String pUserIdWinner) {
        this.pUserIdWinner = pUserIdWinner;
    }

    public String getPUserIdLoser() {
        return pUserIdLoser;
    }
    public void setPUserIdLoser(String pUserIdLoser) {
        this.pUserIdLoser = pUserIdLoser;
    }

    public String getPGameId() {
        return pGameId;
    }
    public void setPGameId(String pGameId) {
        this.pGameId = pGameId;
    }

    public String getPTrace() {
        return pTrace;
    }
    public void setPTrace(String pTrace) {
        this.pTrace = pTrace;
    }



    public static ClusteredGamesUpdateGameAndUsersInput.Builder builder() {
        return new ClusteredGamesUpdateGameAndUsersInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String pUserIdWinner;
        private String pUserIdLoser;
        private String pGameId;
        private String pTrace;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setPUserIdWinner(String pUserIdWinner) {
            this.pUserIdWinner = pUserIdWinner;
            return this;
        }

        public Builder setPUserIdLoser(String pUserIdLoser) {
            this.pUserIdLoser = pUserIdLoser;
            return this;
        }

        public Builder setPGameId(String pGameId) {
            this.pGameId = pGameId;
            return this;
        }

        public Builder setPTrace(String pTrace) {
            this.pTrace = pTrace;
            return this;
        }


        public ClusteredGamesUpdateGameAndUsersInput build() {
            return new ClusteredGamesUpdateGameAndUsersInput(clientMutationId, pUserIdWinner, pUserIdLoser, pGameId, pTrace);
        }

    }
}
