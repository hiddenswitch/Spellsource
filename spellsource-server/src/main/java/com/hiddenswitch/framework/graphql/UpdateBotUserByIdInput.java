package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateBotUserById` mutation.
 */
public class UpdateBotUserByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private BotUserPatch botUserPatch;

    public UpdateBotUserByIdInput() {
    }

    public UpdateBotUserByIdInput(String clientMutationId, String id, BotUserPatch botUserPatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.botUserPatch = botUserPatch;
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

    public BotUserPatch getBotUserPatch() {
        return botUserPatch;
    }
    public void setBotUserPatch(BotUserPatch botUserPatch) {
        this.botUserPatch = botUserPatch;
    }



    public static UpdateBotUserByIdInput.Builder builder() {
        return new UpdateBotUserByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private BotUserPatch botUserPatch;

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

        public Builder setBotUserPatch(BotUserPatch botUserPatch) {
            this.botUserPatch = botUserPatch;
            return this;
        }


        public UpdateBotUserByIdInput build() {
            return new UpdateBotUserByIdInput(clientMutationId, id, botUserPatch);
        }

    }
}
