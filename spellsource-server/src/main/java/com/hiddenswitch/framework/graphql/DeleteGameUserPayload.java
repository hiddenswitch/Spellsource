package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `GameUser` mutation.
 */
public class DeleteGameUserPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GameUser gameUser;
    private String deletedGameUserId;
    private Query query;
    private GameUsersEdge gameUserEdge;
    private Deck deckByDeckId;
    private Game gameByGameId;

    public DeleteGameUserPayload() {
    }

    public DeleteGameUserPayload(String clientMutationId, GameUser gameUser, String deletedGameUserId, Query query, GameUsersEdge gameUserEdge, Deck deckByDeckId, Game gameByGameId) {
        this.clientMutationId = clientMutationId;
        this.gameUser = gameUser;
        this.deletedGameUserId = deletedGameUserId;
        this.query = query;
        this.gameUserEdge = gameUserEdge;
        this.deckByDeckId = deckByDeckId;
        this.gameByGameId = gameByGameId;
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
     * The `GameUser` that was deleted by this mutation.
     */
    public GameUser getGameUser() {
        return gameUser;
    }
    /**
     * The `GameUser` that was deleted by this mutation.
     */
    public void setGameUser(GameUser gameUser) {
        this.gameUser = gameUser;
    }

    public String getDeletedGameUserId() {
        return deletedGameUserId;
    }
    public void setDeletedGameUserId(String deletedGameUserId) {
        this.deletedGameUserId = deletedGameUserId;
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
     * An edge for our `GameUser`. May be used by Relay 1.
     */
    public GameUsersEdge getGameUserEdge() {
        return gameUserEdge;
    }
    /**
     * An edge for our `GameUser`. May be used by Relay 1.
     */
    public void setGameUserEdge(GameUsersEdge gameUserEdge) {
        this.gameUserEdge = gameUserEdge;
    }

    /**
     * Reads a single `Deck` that is related to this `GameUser`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `GameUser`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    /**
     * Reads a single `Game` that is related to this `GameUser`.
     */
    public Game getGameByGameId() {
        return gameByGameId;
    }
    /**
     * Reads a single `Game` that is related to this `GameUser`.
     */
    public void setGameByGameId(Game gameByGameId) {
        this.gameByGameId = gameByGameId;
    }



    public static DeleteGameUserPayload.Builder builder() {
        return new DeleteGameUserPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private GameUser gameUser;
        private String deletedGameUserId;
        private Query query;
        private GameUsersEdge gameUserEdge;
        private Deck deckByDeckId;
        private Game gameByGameId;

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
         * The `GameUser` that was deleted by this mutation.
         */
        public Builder setGameUser(GameUser gameUser) {
            this.gameUser = gameUser;
            return this;
        }

        public Builder setDeletedGameUserId(String deletedGameUserId) {
            this.deletedGameUserId = deletedGameUserId;
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
         * An edge for our `GameUser`. May be used by Relay 1.
         */
        public Builder setGameUserEdge(GameUsersEdge gameUserEdge) {
            this.gameUserEdge = gameUserEdge;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `GameUser`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        /**
         * Reads a single `Game` that is related to this `GameUser`.
         */
        public Builder setGameByGameId(Game gameByGameId) {
            this.gameByGameId = gameByGameId;
            return this;
        }


        public DeleteGameUserPayload build() {
            return new DeleteGameUserPayload(clientMutationId, gameUser, deletedGameUserId, query, gameUserEdge, deckByDeckId, gameByGameId);
        }

    }
}
