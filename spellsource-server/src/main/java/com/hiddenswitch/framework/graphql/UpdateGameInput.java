package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGame` mutation.
 */
public class UpdateGameInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private GamePatch gamePatch;

    public UpdateGameInput() {
    }

    public UpdateGameInput(String clientMutationId, String nodeId, GamePatch gamePatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.gamePatch = gamePatch;
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

    public GamePatch getGamePatch() {
        return gamePatch;
    }
    public void setGamePatch(GamePatch gamePatch) {
        this.gamePatch = gamePatch;
    }



    public static UpdateGameInput.Builder builder() {
        return new UpdateGameInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private GamePatch gamePatch;

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

        public Builder setGamePatch(GamePatch gamePatch) {
            this.gamePatch = gamePatch;
            return this;
        }


        public UpdateGameInput build() {
            return new UpdateGameInput(clientMutationId, nodeId, gamePatch);
        }

    }
}
