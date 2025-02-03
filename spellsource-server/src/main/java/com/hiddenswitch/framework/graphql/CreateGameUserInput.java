package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `GameUser` mutation.
 */
public class CreateGameUserInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GameUserInput gameUser;

    public CreateGameUserInput() {
    }

    public CreateGameUserInput(String clientMutationId, GameUserInput gameUser) {
        this.clientMutationId = clientMutationId;
        this.gameUser = gameUser;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public GameUserInput getGameUser() {
        return gameUser;
    }
    public void setGameUser(GameUserInput gameUser) {
        this.gameUser = gameUser;
    }



    public static CreateGameUserInput.Builder builder() {
        return new CreateGameUserInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private GameUserInput gameUser;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setGameUser(GameUserInput gameUser) {
            this.gameUser = gameUser;
            return this;
        }


        public CreateGameUserInput build() {
            return new CreateGameUserInput(clientMutationId, gameUser);
        }

    }
}
