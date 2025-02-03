package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `MatchmakingTicket` object types. All fields are
tested for equality and combined with a logical ‘and.’
 */
public class MatchmakingTicketCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String ticketId;
    private String queueId;
    private String userId;
    private String deckId;
    private String botDeckId;
    private String createdAt;

    public MatchmakingTicketCondition() {
    }

    public MatchmakingTicketCondition(String ticketId, String queueId, String userId, String deckId, String botDeckId, String createdAt) {
        this.ticketId = ticketId;
        this.queueId = queueId;
        this.userId = userId;
        this.deckId = deckId;
        this.botDeckId = botDeckId;
        this.createdAt = createdAt;
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



    public static MatchmakingTicketCondition.Builder builder() {
        return new MatchmakingTicketCondition.Builder();
    }

    public static class Builder {

        private String ticketId;
        private String queueId;
        private String userId;
        private String deckId;
        private String botDeckId;
        private String createdAt;

        public Builder() {
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


        public MatchmakingTicketCondition build() {
            return new MatchmakingTicketCondition(ticketId, queueId, userId, deckId, botDeckId, createdAt);
        }

    }
}
