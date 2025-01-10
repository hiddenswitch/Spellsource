package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `Game` mutation.
 */
public class CreateGameInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GameInput game;

    public CreateGameInput() {
    }

    public CreateGameInput(String clientMutationId, GameInput game) {
        this.clientMutationId = clientMutationId;
        this.game = game;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public GameInput getGame() {
        return game;
    }
    public void setGame(GameInput game) {
        this.game = game;
    }



    public static CreateGameInput.Builder builder() {
        return new CreateGameInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private GameInput game;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setGame(GameInput game) {
            this.game = game;
            return this;
        }


        public CreateGameInput build() {
            return new CreateGameInput(clientMutationId, game);
        }

    }
}
