package com.hiddenswitch.framework.graphql;


public class MatchmakingQueue implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;
    private String name;
    private boolean botOpponent;
    private boolean privateLobby;
    private boolean startsAutomatically;
    private String stillConnectedTimeout;
    private String emptyLobbyTimeout;
    private String awaitingLobbyTimeout;
    private boolean once;
    private boolean automaticallyClose;
    private int lobbySize;
    private String queueCreatedAt;
    private MatchmakingTicketsConnection matchmakingTicketsByQueueId;

    public MatchmakingQueue() {
    }

    public MatchmakingQueue(String nodeId, String id, String name, boolean botOpponent, boolean privateLobby, boolean startsAutomatically, String stillConnectedTimeout, String emptyLobbyTimeout, String awaitingLobbyTimeout, boolean once, boolean automaticallyClose, int lobbySize, String queueCreatedAt, MatchmakingTicketsConnection matchmakingTicketsByQueueId) {
        this.nodeId = nodeId;
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
        this.matchmakingTicketsByQueueId = matchmakingTicketsByQueueId;
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

    public boolean getBotOpponent() {
        return botOpponent;
    }
    public void setBotOpponent(boolean botOpponent) {
        this.botOpponent = botOpponent;
    }

    public boolean getPrivateLobby() {
        return privateLobby;
    }
    public void setPrivateLobby(boolean privateLobby) {
        this.privateLobby = privateLobby;
    }

    public boolean getStartsAutomatically() {
        return startsAutomatically;
    }
    public void setStartsAutomatically(boolean startsAutomatically) {
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

    public boolean getOnce() {
        return once;
    }
    public void setOnce(boolean once) {
        this.once = once;
    }

    public boolean getAutomaticallyClose() {
        return automaticallyClose;
    }
    public void setAutomaticallyClose(boolean automaticallyClose) {
        this.automaticallyClose = automaticallyClose;
    }

    public int getLobbySize() {
        return lobbySize;
    }
    public void setLobbySize(int lobbySize) {
        this.lobbySize = lobbySize;
    }

    public String getQueueCreatedAt() {
        return queueCreatedAt;
    }
    public void setQueueCreatedAt(String queueCreatedAt) {
        this.queueCreatedAt = queueCreatedAt;
    }

    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public MatchmakingTicketsConnection getMatchmakingTicketsByQueueId() {
        return matchmakingTicketsByQueueId;
    }
    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public void setMatchmakingTicketsByQueueId(MatchmakingTicketsConnection matchmakingTicketsByQueueId) {
        this.matchmakingTicketsByQueueId = matchmakingTicketsByQueueId;
    }



    public static MatchmakingQueue.Builder builder() {
        return new MatchmakingQueue.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;
        private String name;
        private boolean botOpponent;
        private boolean privateLobby;
        private boolean startsAutomatically;
        private String stillConnectedTimeout;
        private String emptyLobbyTimeout;
        private String awaitingLobbyTimeout;
        private boolean once;
        private boolean automaticallyClose;
        private int lobbySize;
        private String queueCreatedAt;
        private MatchmakingTicketsConnection matchmakingTicketsByQueueId;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBotOpponent(boolean botOpponent) {
            this.botOpponent = botOpponent;
            return this;
        }

        public Builder setPrivateLobby(boolean privateLobby) {
            this.privateLobby = privateLobby;
            return this;
        }

        public Builder setStartsAutomatically(boolean startsAutomatically) {
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

        public Builder setOnce(boolean once) {
            this.once = once;
            return this;
        }

        public Builder setAutomaticallyClose(boolean automaticallyClose) {
            this.automaticallyClose = automaticallyClose;
            return this;
        }

        public Builder setLobbySize(int lobbySize) {
            this.lobbySize = lobbySize;
            return this;
        }

        public Builder setQueueCreatedAt(String queueCreatedAt) {
            this.queueCreatedAt = queueCreatedAt;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `MatchmakingTicket`.
         */
        public Builder setMatchmakingTicketsByQueueId(MatchmakingTicketsConnection matchmakingTicketsByQueueId) {
            this.matchmakingTicketsByQueueId = matchmakingTicketsByQueueId;
            return this;
        }


        public MatchmakingQueue build() {
            return new MatchmakingQueue(nodeId, id, name, botOpponent, privateLobby, startsAutomatically, stillConnectedTimeout, emptyLobbyTimeout, awaitingLobbyTimeout, once, automaticallyClose, lobbySize, queueCreatedAt, matchmakingTicketsByQueueId);
        }

    }
}
