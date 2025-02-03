package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `GameUser` object types. All fields are combined with a logical ‘and.’
 */
public class GameUserFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private IntFilter playerIndex;
    private BigIntFilter gameId;
    private StringFilter userId;
    private StringFilter deckId;
    private GameUserVictoryEnumFilter victoryStatus;
    private DeckFilter deckByDeckId;
    private Boolean deckByDeckIdExists;
    private GameFilter gameByGameId;
    private java.util.List<GameUserFilter> and;
    private java.util.List<GameUserFilter> or;
    private GameUserFilter not;

    public GameUserFilter() {
    }

    public GameUserFilter(IntFilter playerIndex, BigIntFilter gameId, StringFilter userId, StringFilter deckId, GameUserVictoryEnumFilter victoryStatus, DeckFilter deckByDeckId, Boolean deckByDeckIdExists, GameFilter gameByGameId, java.util.List<GameUserFilter> and, java.util.List<GameUserFilter> or, GameUserFilter not) {
        this.playerIndex = playerIndex;
        this.gameId = gameId;
        this.userId = userId;
        this.deckId = deckId;
        this.victoryStatus = victoryStatus;
        this.deckByDeckId = deckByDeckId;
        this.deckByDeckIdExists = deckByDeckIdExists;
        this.gameByGameId = gameByGameId;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public IntFilter getPlayerIndex() {
        return playerIndex;
    }
    public void setPlayerIndex(IntFilter playerIndex) {
        this.playerIndex = playerIndex;
    }

    public BigIntFilter getGameId() {
        return gameId;
    }
    public void setGameId(BigIntFilter gameId) {
        this.gameId = gameId;
    }

    public StringFilter getUserId() {
        return userId;
    }
    public void setUserId(StringFilter userId) {
        this.userId = userId;
    }

    public StringFilter getDeckId() {
        return deckId;
    }
    public void setDeckId(StringFilter deckId) {
        this.deckId = deckId;
    }

    public GameUserVictoryEnumFilter getVictoryStatus() {
        return victoryStatus;
    }
    public void setVictoryStatus(GameUserVictoryEnumFilter victoryStatus) {
        this.victoryStatus = victoryStatus;
    }

    public DeckFilter getDeckByDeckId() {
        return deckByDeckId;
    }
    public void setDeckByDeckId(DeckFilter deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    public Boolean getDeckByDeckIdExists() {
        return deckByDeckIdExists;
    }
    public void setDeckByDeckIdExists(Boolean deckByDeckIdExists) {
        this.deckByDeckIdExists = deckByDeckIdExists;
    }

    public GameFilter getGameByGameId() {
        return gameByGameId;
    }
    public void setGameByGameId(GameFilter gameByGameId) {
        this.gameByGameId = gameByGameId;
    }

    public java.util.List<GameUserFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<GameUserFilter> and) {
        this.and = and;
    }

    public java.util.List<GameUserFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<GameUserFilter> or) {
        this.or = or;
    }

    public GameUserFilter getNot() {
        return not;
    }
    public void setNot(GameUserFilter not) {
        this.not = not;
    }



    public static GameUserFilter.Builder builder() {
        return new GameUserFilter.Builder();
    }

    public static class Builder {

        private IntFilter playerIndex;
        private BigIntFilter gameId;
        private StringFilter userId;
        private StringFilter deckId;
        private GameUserVictoryEnumFilter victoryStatus;
        private DeckFilter deckByDeckId;
        private Boolean deckByDeckIdExists;
        private GameFilter gameByGameId;
        private java.util.List<GameUserFilter> and;
        private java.util.List<GameUserFilter> or;
        private GameUserFilter not;

        public Builder() {
        }

        public Builder setPlayerIndex(IntFilter playerIndex) {
            this.playerIndex = playerIndex;
            return this;
        }

        public Builder setGameId(BigIntFilter gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder setUserId(StringFilter userId) {
            this.userId = userId;
            return this;
        }

        public Builder setDeckId(StringFilter deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setVictoryStatus(GameUserVictoryEnumFilter victoryStatus) {
            this.victoryStatus = victoryStatus;
            return this;
        }

        public Builder setDeckByDeckId(DeckFilter deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        public Builder setDeckByDeckIdExists(Boolean deckByDeckIdExists) {
            this.deckByDeckIdExists = deckByDeckIdExists;
            return this;
        }

        public Builder setGameByGameId(GameFilter gameByGameId) {
            this.gameByGameId = gameByGameId;
            return this;
        }

        public Builder setAnd(java.util.List<GameUserFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<GameUserFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(GameUserFilter not) {
            this.not = not;
            return this;
        }


        public GameUserFilter build() {
            return new GameUserFilter(playerIndex, gameId, userId, deckId, victoryStatus, deckByDeckId, deckByDeckIdExists, gameByGameId, and, or, not);
        }

    }
}
