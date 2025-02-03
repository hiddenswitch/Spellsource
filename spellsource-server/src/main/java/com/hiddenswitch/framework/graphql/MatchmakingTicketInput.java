package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `MatchmakingTicket`
 */
public class MatchmakingTicketInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String queueId;
    private String userId;
    private String deckId;
    private String botDeckId;
    private String createdAt;

    public MatchmakingTicketInput() {
    }

    public MatchmakingTicketInput(String queueId, String userId, String deckId, String botDeckId, String createdAt) {
        this.queueId = queueId;
        this.userId = userId;
        this.deckId = deckId;
        this.botDeckId = botDeckId;
        this.createdAt = createdAt;
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



    public static MatchmakingTicketInput.Builder builder() {
        return new MatchmakingTicketInput.Builder();
    }

    public static class Builder {

        private String queueId;
        private String userId;
        private String deckId;
        private String botDeckId;
        private String createdAt;

        public Builder() {
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


        public MatchmakingTicketInput build() {
            return new MatchmakingTicketInput(queueId, userId, deckId, botDeckId, createdAt);
        }

    }
}
