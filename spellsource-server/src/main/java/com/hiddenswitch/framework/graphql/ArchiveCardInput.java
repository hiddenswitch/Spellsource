package com.hiddenswitch.framework.graphql;


/**
 * All input for the `archiveCard` mutation.
 */
public class ArchiveCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;

    public ArchiveCardInput() {
    }

    public ArchiveCardInput(String clientMutationId, String cardId) {
        this.clientMutationId = clientMutationId;
        this.cardId = cardId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static ArchiveCardInput.Builder builder() {
        return new ArchiveCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public ArchiveCardInput build() {
            return new ArchiveCardInput(clientMutationId, cardId);
        }

    }
}
