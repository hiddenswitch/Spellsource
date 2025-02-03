package com.hiddenswitch.framework.graphql;


public class MatchmakingTicket implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String ticketId;
    private String queueId;
    private String userId;
    private String deckId;
    private String botDeckId;
    private String createdAt;
    private Deck deckByBotDeckId;
    private Deck deckByDeckId;
    private MatchmakingQueue matchmakingQueueByQueueId;

    public MatchmakingTicket() {
    }

    public MatchmakingTicket(String nodeId, String ticketId, String queueId, String userId, String deckId, String botDeckId, String createdAt, Deck deckByBotDeckId, Deck deckByDeckId, MatchmakingQueue matchmakingQueueByQueueId) {
        this.nodeId = nodeId;
        this.ticketId = ticketId;
        this.queueId = queueId;
        this.userId = userId;
        this.deckId = deckId;
        this.botDeckId = botDeckId;
        this.createdAt = createdAt;
        this.deckByBotDeckId = deckByBotDeckId;
        this.deckByDeckId = deckByDeckId;
        this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
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

    public String getTicketId() {
        return ticketId;
    }
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getQueueId() {
        return queueId;
    }
    public void setQueueId(String queueId) {
        this.queueId = queueId;
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

    public String getBotDeckId() {
        return botDeckId;
    }
    public void setBotDeckId(String botDeckId) {
        this.botDeckId = botDeckId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public Deck getDeckByBotDeckId() {
        return deckByBotDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public void setDeckByBotDeckId(Deck deckByBotDeckId) {
        this.deckByBotDeckId = deckByBotDeckId;
    }

    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    /**
     * Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`.
     */
    public MatchmakingQueue getMatchmakingQueueByQueueId() {
        return matchmakingQueueByQueueId;
    }
    /**
     * Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`.
     */
    public void setMatchmakingQueueByQueueId(MatchmakingQueue matchmakingQueueByQueueId) {
        this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
    }



    public static MatchmakingTicket.Builder builder() {
        return new MatchmakingTicket.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String ticketId;
        private String queueId;
        private String userId;
        private String deckId;
        private String botDeckId;
        private String createdAt;
        private Deck deckByBotDeckId;
        private Deck deckByDeckId;
        private MatchmakingQueue matchmakingQueueByQueueId;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setTicketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
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

        public Builder setBotDeckId(String botDeckId) {
            this.botDeckId = botDeckId;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `MatchmakingTicket`.
         */
        public Builder setDeckByBotDeckId(Deck deckByBotDeckId) {
            this.deckByBotDeckId = deckByBotDeckId;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `MatchmakingTicket`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        /**
         * Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`.
         */
        public Builder setMatchmakingQueueByQueueId(MatchmakingQueue matchmakingQueueByQueueId) {
            this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
            return this;
        }


        public MatchmakingTicket build() {
            return new MatchmakingTicket(nodeId, ticketId, queueId, userId, deckId, botDeckId, createdAt, deckByBotDeckId, deckByDeckId, matchmakingQueueByQueueId);
        }

    }
}
