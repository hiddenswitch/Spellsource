package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGameById` mutation.
 */
public class UpdateGameByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private GamePatch gamePatch;

    public UpdateGameByIdInput() {
    }

    public UpdateGameByIdInput(String clientMutationId, String id, GamePatch gamePatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.gamePatch = gamePatch;
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

    public GamePatch getGamePatch() {
        return gamePatch;
    }
    public void setGamePatch(GamePatch gamePatch) {
        this.gamePatch = gamePatch;
    }



    public static UpdateGameByIdInput.Builder builder() {
        return new UpdateGameByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private GamePatch gamePatch;

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

        public Builder setGamePatch(GamePatch gamePatch) {
            this.gamePatch = gamePatch;
            return this;
        }


        public UpdateGameByIdInput build() {
            return new UpdateGameByIdInput(clientMutationId, id, gamePatch);
        }

    }
}
