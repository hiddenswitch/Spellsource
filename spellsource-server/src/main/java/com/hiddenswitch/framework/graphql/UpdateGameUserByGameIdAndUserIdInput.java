package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGameUserByGameIdAndUserId` mutation.
 */
public class UpdateGameUserByGameIdAndUserIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String gameId;
    private String userId;
    private GameUserPatch gameUserPatch;

    public UpdateGameUserByGameIdAndUserIdInput() {
    }

    public UpdateGameUserByGameIdAndUserIdInput(String clientMutationId, String gameId, String userId, GameUserPatch gameUserPatch) {
        this.clientMutationId = clientMutationId;
        this.gameId = gameId;
        this.userId = userId;
        this.gameUserPatch = gameUserPatch;
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

    public GameUserPatch getGameUserPatch() {
        return gameUserPatch;
    }
    public void setGameUserPatch(GameUserPatch gameUserPatch) {
        this.gameUserPatch = gameUserPatch;
    }



    public static UpdateGameUserByGameIdAndUserIdInput.Builder builder() {
        return new UpdateGameUserByGameIdAndUserIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String gameId;
        private String userId;
        private GameUserPatch gameUserPatch;

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

        public Builder setGameUserPatch(GameUserPatch gameUserPatch) {
            this.gameUserPatch = gameUserPatch;
            return this;
        }


        public UpdateGameUserByGameIdAndUserIdInput build() {
            return new UpdateGameUserByGameIdAndUserIdInput(clientMutationId, gameId, userId, gameUserPatch);
        }

    }
}
