package com.hiddenswitch.framework.graphql;


public class GameUser implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private Integer playerIndex;
    private String gameId;
    private String userId;
    private String deckId;
    private GameUserVictoryEnum victoryStatus;
    private Deck deckByDeckId;
    private Game gameByGameId;

    public GameUser() {
    }

    public GameUser(String nodeId, Integer playerIndex, String gameId, String userId, String deckId, GameUserVictoryEnum victoryStatus, Deck deckByDeckId, Game gameByGameId) {
        this.nodeId = nodeId;
        this.playerIndex = playerIndex;
        this.gameId = gameId;
        this.userId = userId;
        this.deckId = deckId;
        this.victoryStatus = victoryStatus;
        this.deckByDeckId = deckByDeckId;
        this.gameByGameId = gameByGameId;
    }

    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public String getNodeId() {
        return nodeId;
    }
    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getPlayerIndex() {
        return playerIndex;
    }
    public void setPlayerIndex(Integer playerIndex) {
        this.playerIndex = playerIndex;
    }

    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public GameUserVictoryEnum getVictoryStatus() {
        return victoryStatus;
    }
    public void setVictoryStatus(GameUserVictoryEnum victoryStatus) {
        this.victoryStatus = victoryStatus;
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



    public static GameUser.Builder builder() {
        return new GameUser.Builder();
    }

    public static class Builder {

        private String nodeId;
        private Integer playerIndex;
        private String gameId;
        private String userId;
        private String deckId;
        private GameUserVictoryEnum victoryStatus;
        private Deck deckByDeckId;
        private Game gameByGameId;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setPlayerIndex(Integer playerIndex) {
            this.playerIndex = playerIndex;
            return this;
        }

        public Builder setGameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setVictoryStatus(GameUserVictoryEnum victoryStatus) {
            this.victoryStatus = victoryStatus;
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


        public GameUser build() {
            return new GameUser(nodeId, playerIndex, gameId, userId, deckId, victoryStatus, deckByDeckId, gameByGameId);
        }

    }
}
