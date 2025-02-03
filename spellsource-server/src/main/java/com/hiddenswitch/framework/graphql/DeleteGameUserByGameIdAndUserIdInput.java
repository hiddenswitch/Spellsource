package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteGameUserByGameIdAndUserId` mutation.
 */
public class DeleteGameUserByGameIdAndUserIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String gameId;
    private String userId;

    public DeleteGameUserByGameIdAndUserIdInput() {
    }

    public DeleteGameUserByGameIdAndUserIdInput(String clientMutationId, String gameId, String userId) {
        this.clientMutationId = clientMutationId;
        this.gameId = gameId;
        this.userId = userId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public static DeleteGameUserByGameIdAndUserIdInput.Builder builder() {
        return new DeleteGameUserByGameIdAndUserIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String gameId;
        private String userId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setGameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }


        public DeleteGameUserByGameIdAndUserIdInput build() {
            return new DeleteGameUserByGameIdAndUserIdInput(clientMutationId, gameId, userId);
        }

    }
}
