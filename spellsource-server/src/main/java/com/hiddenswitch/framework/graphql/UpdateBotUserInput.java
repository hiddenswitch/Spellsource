package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateBotUser` mutation.
 */
public class UpdateBotUserInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private BotUserPatch botUserPatch;

    public UpdateBotUserInput() {
    }

    public UpdateBotUserInput(String clientMutationId, String nodeId, BotUserPatch botUserPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.botUserPatch = botUserPatch;
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

    public BotUserPatch getBotUserPatch() {
        return botUserPatch;
    }
    public void setBotUserPatch(BotUserPatch botUserPatch) {
        this.botUserPatch = botUserPatch;
    }



    public static UpdateBotUserInput.Builder builder() {
        return new UpdateBotUserInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private BotUserPatch botUserPatch;

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

        public Builder setBotUserPatch(BotUserPatch botUserPatch) {
            this.botUserPatch = botUserPatch;
            return this;
        }


        public UpdateBotUserInput build() {
            return new UpdateBotUserInput(clientMutationId, nodeId, botUserPatch);
        }

    }
}
