package com.hiddenswitch.framework.graphql;


public class MatchFound implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String url;
    private String playerKey;
    private String playerSecret;

    public MatchFound() {
    }

    public MatchFound(String gameId, String url, String playerKey, String playerSecret) {
        this.gameId = gameId;
        this.url = url;
        this.playerKey = playerKey;
        this.playerSecret = playerSecret;
    }

    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlayerKey() {
        return playerKey;
    }
    public void setPlayerKey(String playerKey) {
        this.playerKey = playerKey;
    }

    public String getPlayerSecret() {
        return playerSecret;
    }
    public void setPlayerSecret(String playerSecret) {
        this.playerSecret = playerSecret;
    }



    public static MatchFound.Builder builder() {
        return new MatchFound.Builder();
    }

    public static class Builder {

        private String gameId;
        private String url;
        private String playerKey;
        private String playerSecret;

        public Builder() {
        }

        public Builder setGameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setPlayerKey(String playerKey) {
            this.playerKey = playerKey;
            return this;
        }

        public Builder setPlayerSecret(String playerSecret) {
            this.playerSecret = playerSecret;
            return this;
        }


        public MatchFound build() {
            return new MatchFound(gameId, url, playerKey, playerSecret);
        }

    }
}
