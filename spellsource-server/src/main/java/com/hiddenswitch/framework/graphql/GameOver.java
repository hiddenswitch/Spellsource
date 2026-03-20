package com.hiddenswitch.framework.graphql;


public class GameOver implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private boolean localPlayerWon;
    private Integer winningPlayerId;

    public GameOver() {
    }

    public GameOver(boolean localPlayerWon, Integer winningPlayerId) {
        this.localPlayerWon = localPlayerWon;
        this.winningPlayerId = winningPlayerId;
    }

    public boolean getLocalPlayerWon() {
        return localPlayerWon;
    }
    public void setLocalPlayerWon(boolean localPlayerWon) {
        this.localPlayerWon = localPlayerWon;
    }

    public Integer getWinningPlayerId() {
        return winningPlayerId;
    }
    public void setWinningPlayerId(Integer winningPlayerId) {
        this.winningPlayerId = winningPlayerId;
    }



    public static GameOver.Builder builder() {
        return new GameOver.Builder();
    }

    public static class Builder {

        private boolean localPlayerWon;
        private Integer winningPlayerId;

        public Builder() {
        }

        public Builder setLocalPlayerWon(boolean localPlayerWon) {
            this.localPlayerWon = localPlayerWon;
            return this;
        }

        public Builder setWinningPlayerId(Integer winningPlayerId) {
            this.winningPlayerId = winningPlayerId;
            return this;
        }


        public GameOver build() {
            return new GameOver(localPlayerWon, winningPlayerId);
        }

    }
}
