package com.hiddenswitch.framework.graphql;


public class MatchmakingEnqueueInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private String queueId;
    private String botDeckId;

    public MatchmakingEnqueueInput() {
    }

    public MatchmakingEnqueueInput(String deckId, String queueId, String botDeckId) {
        this.deckId = deckId;
        this.queueId = queueId;
        this.botDeckId = botDeckId;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getQueueId() {
        return queueId;
    }
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getBotDeckId() {
        return botDeckId;
    }
    public void setBotDeckId(String botDeckId) {
        this.botDeckId = botDeckId;
    }



    public static MatchmakingEnqueueInput.Builder builder() {
        return new MatchmakingEnqueueInput.Builder();
    }

    public static class Builder {

        private String deckId;
        private String queueId;
        private String botDeckId;

        public Builder() {
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder setBotDeckId(String botDeckId) {
            this.botDeckId = botDeckId;
            return this;
        }


        public MatchmakingEnqueueInput build() {
            return new MatchmakingEnqueueInput(deckId, queueId, botDeckId);
        }

    }
}
