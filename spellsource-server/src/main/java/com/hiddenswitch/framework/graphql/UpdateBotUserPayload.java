package com.hiddenswitch.framework.graphql;


/**
 * The output of our update `BotUser` mutation.
 */
public class UpdateBotUserPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private BotUser botUser;
    private Query query;
    private BotUsersEdge botUserEdge;

    public UpdateBotUserPayload() {
    }

    public UpdateBotUserPayload(String clientMutationId, BotUser botUser, Query query, BotUsersEdge botUserEdge) {
        this.clientMutationId = clientMutationId;
        this.botUser = botUser;
        this.query = query;
        this.botUserEdge = botUserEdge;
    }

    /**
     * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
     */
    public String getClientMutationId() {
        return clientMutationId;
    }
    /**
     * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
     */
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    /**
     * The `BotUser` that was updated by this mutation.
     */
    public BotUser getBotUser() {
        return botUser;
    }
    /**
     * The `BotUser` that was updated by this mutation.
     */
    public void setBotUser(BotUser botUser) {
        this.botUser = botUser;
    }

    /**
     * Our root query field type. Allows us to run any query from our mutation payload.
     */
    public Query getQuery() {
        return query;
    }
    /**
     * Our root query field type. Allows us to run any query from our mutation payload.
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * An edge for our `BotUser`. May be used by Relay 1.
     */
    public BotUsersEdge getBotUserEdge() {
        return botUserEdge;
    }
    /**
     * An edge for our `BotUser`. May be used by Relay 1.
     */
    public void setBotUserEdge(BotUsersEdge botUserEdge) {
        this.botUserEdge = botUserEdge;
    }



    public static UpdateBotUserPayload.Builder builder() {
        return new UpdateBotUserPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private BotUser botUser;
        private Query query;
        private BotUsersEdge botUserEdge;

        public Builder() {
        }

        /**
         * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
         */
        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        /**
         * The `BotUser` that was updated by this mutation.
         */
        public Builder setBotUser(BotUser botUser) {
            this.botUser = botUser;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }

        /**
         * An edge for our `BotUser`. May be used by Relay 1.
         */
        public Builder setBotUserEdge(BotUsersEdge botUserEdge) {
            this.botUserEdge = botUserEdge;
            return this;
        }


        public UpdateBotUserPayload build() {
            return new UpdateBotUserPayload(clientMutationId, botUser, query, botUserEdge);
        }

    }
}
