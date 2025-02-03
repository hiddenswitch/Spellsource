package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `Game` mutation.
 */
public class CreateGamePayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Game game;
    private Query query;
    private GamesEdge gameEdge;

    public CreateGamePayload() {
    }

    public CreateGamePayload(String clientMutationId, Game game, Query query, GamesEdge gameEdge) {
        this.clientMutationId = clientMutationId;
        this.game = game;
        this.query = query;
        this.gameEdge = gameEdge;
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
     * The `Game` that was created by this mutation.
     */
    public Game getGame() {
        return game;
    }
    /**
     * The `Game` that was created by this mutation.
     */
    public void setGame(Game game) {
        this.game = game;
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
     * An edge for our `Game`. May be used by Relay 1.
     */
    public GamesEdge getGameEdge() {
        return gameEdge;
    }
    /**
     * An edge for our `Game`. May be used by Relay 1.
     */
    public void setGameEdge(GamesEdge gameEdge) {
        this.gameEdge = gameEdge;
    }



    public static CreateGamePayload.Builder builder() {
        return new CreateGamePayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private Game game;
        private Query query;
        private GamesEdge gameEdge;

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
         * The `Game` that was created by this mutation.
         */
        public Builder setGame(Game game) {
            this.game = game;
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
         * An edge for our `Game`. May be used by Relay 1.
         */
        public Builder setGameEdge(GamesEdge gameEdge) {
            this.gameEdge = gameEdge;
            return this;
        }


        public CreateGamePayload build() {
            return new CreateGamePayload(clientMutationId, game, query, gameEdge);
        }

    }
}
