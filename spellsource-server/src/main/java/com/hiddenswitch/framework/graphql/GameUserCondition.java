package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `GameUser` object types. All fields are tested
for equality and combined with a logical ‘and.’
 */
public class GameUserCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Integer playerIndex;
    private String gameId;
    private String userId;
    private String deckId;
    private GameUserVictoryEnum victoryStatus;

    public GameUserCondition() {
    }

    public GameUserCondition(Integer playerIndex, String gameId, String userId, String deckId, GameUserVictoryEnum victoryStatus) {
        this.playerIndex = playerIndex;
        this.gameId = gameId;
        this.userId = userId;
        this.deckId = deckId;
        this.victoryStatus = victoryStatus;
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



    public static GameUserCondition.Builder builder() {
        return new GameUserCondition.Builder();
    }

    public static class Builder {

        private Integer playerIndex;
        private String gameId;
        private String userId;
        private String deckId;
        private GameUserVictoryEnum victoryStatus;

        public Builder() {
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


        public GameUserCondition build() {
            return new GameUserCondition(playerIndex, gameId, userId, deckId, victoryStatus);
        }

    }
}
