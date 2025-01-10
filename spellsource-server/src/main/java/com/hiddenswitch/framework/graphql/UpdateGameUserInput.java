package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGameUser` mutation.
 */
public class UpdateGameUserInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private GameUserPatch gameUserPatch;

    public UpdateGameUserInput() {
    }

    public UpdateGameUserInput(String clientMutationId, String nodeId, GameUserPatch gameUserPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.gameUserPatch = gameUserPatch;
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

    public GameUserPatch getGameUserPatch() {
        return gameUserPatch;
    }
    public void setGameUserPatch(GameUserPatch gameUserPatch) {
        this.gameUserPatch = gameUserPatch;
    }



    public static UpdateGameUserInput.Builder builder() {
        return new UpdateGameUserInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private GameUserPatch gameUserPatch;

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

        public Builder setGameUserPatch(GameUserPatch gameUserPatch) {
            this.gameUserPatch = gameUserPatch;
            return this;
        }


        public UpdateGameUserInput build() {
            return new UpdateGameUserInput(clientMutationId, nodeId, gameUserPatch);
        }

    }
}
