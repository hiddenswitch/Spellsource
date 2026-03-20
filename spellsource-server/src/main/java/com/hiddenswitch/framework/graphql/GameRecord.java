package com.hiddenswitch.framework.graphql;


/**
 * ─── Game record types ────────────────────────────────────────
 */
public class GameRecord implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.lang.Long completedAt;
    private String completedAtLocalized;
    private boolean isBotGame;
    private java.util.List<String> playerNames;

    public GameRecord() {
    }

    public GameRecord(java.lang.Long completedAt, String completedAtLocalized, boolean isBotGame, java.util.List<String> playerNames) {
        this.completedAt = completedAt;
        this.completedAtLocalized = completedAtLocalized;
        this.isBotGame = isBotGame;
        this.playerNames = playerNames;
    }

    public java.lang.Long getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(java.lang.Long completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletedAtLocalized() {
        return completedAtLocalized;
    }
    public void setCompletedAtLocalized(String completedAtLocalized) {
        this.completedAtLocalized = completedAtLocalized;
    }

    public boolean getIsBotGame() {
        return isBotGame;
    }
    public void setIsBotGame(boolean isBotGame) {
        this.isBotGame = isBotGame;
    }

    public java.util.List<String> getPlayerNames() {
        return playerNames;
    }
    public void setPlayerNames(java.util.List<String> playerNames) {
        this.playerNames = playerNames;
    }



    public static GameRecord.Builder builder() {
        return new GameRecord.Builder();
    }

    public static class Builder {

        private java.lang.Long completedAt;
        private String completedAtLocalized;
        private boolean isBotGame;
        private java.util.List<String> playerNames;

        public Builder() {
        }

        public Builder setCompletedAt(java.lang.Long completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder setCompletedAtLocalized(String completedAtLocalized) {
            this.completedAtLocalized = completedAtLocalized;
            return this;
        }

        public Builder setIsBotGame(boolean isBotGame) {
            this.isBotGame = isBotGame;
            return this;
        }

        public Builder setPlayerNames(java.util.List<String> playerNames) {
            this.playerNames = playerNames;
            return this;
        }


        public GameRecord build() {
            return new GameRecord(completedAt, completedAtLocalized, isBotGame, playerNames);
        }

    }
}
