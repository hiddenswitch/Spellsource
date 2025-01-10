package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `BotUser` mutation.
 */
public class CreateBotUserInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private BotUserInput botUser;

    public CreateBotUserInput() {
    }

    public CreateBotUserInput(String clientMutationId, BotUserInput botUser) {
        this.clientMutationId = clientMutationId;
        this.botUser = botUser;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public BotUserInput getBotUser() {
        return botUser;
    }
    public void setBotUser(BotUserInput botUser) {
        this.botUser = botUser;
    }



    public static CreateBotUserInput.Builder builder() {
        return new CreateBotUserInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private BotUserInput botUser;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setBotUser(BotUserInput botUser) {
            this.botUser = botUser;
            return this;
        }


        public CreateBotUserInput build() {
            return new CreateBotUserInput(clientMutationId, botUser);
        }

    }
}
