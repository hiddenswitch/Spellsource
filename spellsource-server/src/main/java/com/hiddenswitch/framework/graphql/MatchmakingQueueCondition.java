package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `MatchmakingQueue` object types. All fields are
tested for equality and combined with a logical ‘and.’
 */
public class MatchmakingQueueCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Boolean botOpponent;
    private Boolean privateLobby;
    private Boolean startsAutomatically;
    private String stillConnectedTimeout;
    private String emptyLobbyTimeout;
    private String awaitingLobbyTimeout;
    private Boolean once;
    private Boolean automaticallyClose;
    private Integer lobbySize;
    private String queueCreatedAt;

    public MatchmakingQueueCondition() {
    }

    public MatchmakingQueueCondition(String id, String name, Boolean botOpponent, Boolean privateLobby, Boolean startsAutomatically, String stillConnectedTimeout, String emptyLobbyTimeout, String awaitingLobbyTimeout, Boolean once, Boolean automaticallyClose, Integer lobbySize, String queueCreatedAt) {
        this.id = id;
        this.name = name;
        this.botOpponent = botOpponent;
        this.privateLobby = privateLobby;
        this.startsAutomatically = startsAutomatically;
        this.stillConnectedTimeout = stillConnectedTimeout;
        this.emptyLobbyTimeout = emptyLobbyTimeout;
        this.awaitingLobbyTimeout = awaitingLobbyTimeout;
        this.once = once;
        this.automaticallyClose = automaticallyClose;
        this.lobbySize = lobbySize;
        this.queueCreatedAt = queueCreatedAt;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Boolean getBotOpponent() {
        return botOpponent;
    }
    public void setBotOpponent(Boolean botOpponent) {
        this.botOpponent = botOpponent;
    }

    public Boolean getPrivateLobby() {
        return privateLobby;
    }
    public void setPrivateLobby(Boolean privateLobby) {
        this.privateLobby = privateLobby;
    }

    public Boolean getStartsAutomatically() {
        return startsAutomatically;
    }
    public void setStartsAutomatically(Boolean startsAutomatically) {
        this.startsAutomatically = startsAutomatically;
    }

    public String getStillConnectedTimeout() {
        return stillConnectedTimeout;
    }
    public void setStillConnectedTimeout(String stillConnectedTimeout) {
        this.stillConnectedTimeout = stillConnectedTimeout;
    }

    public String getEmptyLobbyTimeout() {
        return emptyLobbyTimeout;
    }
    public void setEmptyLobbyTimeout(String emptyLobbyTimeout) {
        this.emptyLobbyTimeout = emptyLobbyTimeout;
    }

    public String getAwaitingLobbyTimeout() {
        return awaitingLobbyTimeout;
    }
    public void setAwaitingLobbyTimeout(String awaitingLobbyTimeout) {
        this.awaitingLobbyTimeout = awaitingLobbyTimeout;
    }

    public Boolean getOnce() {
        return once;
    }
    public void setOnce(Boolean once) {
        this.once = once;
    }

    public Boolean getAutomaticallyClose() {
        return automaticallyClose;
    }
    public void setAutomaticallyClose(Boolean automaticallyClose) {
        this.automaticallyClose = automaticallyClose;
    }

    public Integer getLobbySize() {
        return lobbySize;
    }
    public void setLobbySize(Integer lobbySize) {
        this.lobbySize = lobbySize;
    }

    public String getQueueCreatedAt() {
        return queueCreatedAt;
    }
    public void setQueueCreatedAt(String queueCreatedAt) {
        this.queueCreatedAt = queueCreatedAt;
    }



    public static MatchmakingQueueCondition.Builder builder() {
        return new MatchmakingQueueCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String name;
        private Boolean botOpponent;
        private Boolean privateLobby;
        private Boolean startsAutomatically;
        private String stillConnectedTimeout;
        private String emptyLobbyTimeout;
        private String awaitingLobbyTimeout;
        private Boolean once;
        private Boolean automaticallyClose;
        private Integer lobbySize;
        private String queueCreatedAt;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBotOpponent(Boolean botOpponent) {
            this.botOpponent = botOpponent;
            return this;
        }

        public Builder setPrivateLobby(Boolean privateLobby) {
            this.privateLobby = privateLobby;
            return this;
        }

        public Builder setStartsAutomatically(Boolean startsAutomatically) {
            this.startsAutomatically = startsAutomatically;
            return this;
        }

        public Builder setStillConnectedTimeout(String stillConnectedTimeout) {
            this.stillConnectedTimeout = stillConnectedTimeout;
            return this;
        }

        public Builder setEmptyLobbyTimeout(String emptyLobbyTimeout) {
            this.emptyLobbyTimeout = emptyLobbyTimeout;
            return this;
        }

        public Builder setAwaitingLobbyTimeout(String awaitingLobbyTimeout) {
            this.awaitingLobbyTimeout = awaitingLobbyTimeout;
            return this;
        }

        public Builder setOnce(Boolean once) {
            this.once = once;
            return this;
        }

        public Builder setAutomaticallyClose(Boolean automaticallyClose) {
            this.automaticallyClose = automaticallyClose;
            return this;
        }

        public Builder setLobbySize(Integer lobbySize) {
            this.lobbySize = lobbySize;
            return this;
        }

        public Builder setQueueCreatedAt(String queueCreatedAt) {
            this.queueCreatedAt = queueCreatedAt;
            return this;
        }


        public MatchmakingQueueCondition build() {
            return new MatchmakingQueueCondition(id, name, botOpponent, privateLobby, startsAutomatically, stillConnectedTimeout, emptyLobbyTimeout, awaitingLobbyTimeout, once, automaticallyClose, lobbySize, queueCreatedAt);
        }

    }
}
