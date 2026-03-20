package com.hiddenswitch.framework.graphql;


public class AcceptInviteInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String inviteId;
    private String deckId;
    private String queueId;

    public AcceptInviteInput() {
    }

    public AcceptInviteInput(String inviteId, String deckId, String queueId) {
        this.inviteId = inviteId;
        this.deckId = deckId;
        this.queueId = queueId;
    }

    public String getInviteId() {
        return inviteId;
    }
    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
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



    public static AcceptInviteInput.Builder builder() {
        return new AcceptInviteInput.Builder();
    }

    public static class Builder {

        private String inviteId;
        private String deckId;
        private String queueId;

        public Builder() {
        }

        public Builder setInviteId(String inviteId) {
            this.inviteId = inviteId;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
            return this;
        }


        public AcceptInviteInput build() {
            return new AcceptInviteInput(inviteId, deckId, queueId);
        }

    }
}
