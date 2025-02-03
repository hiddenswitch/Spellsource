package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteBannedDraftCardByCardId` mutation.
 */
public class DeleteBannedDraftCardByCardIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;

    public DeleteBannedDraftCardByCardIdInput() {
    }

    public DeleteBannedDraftCardByCardIdInput(String clientMutationId, String cardId) {
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



    public static DeleteBannedDraftCardByCardIdInput.Builder builder() {
        return new DeleteBannedDraftCardByCardIdInput.Builder();
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


        public DeleteBannedDraftCardByCardIdInput build() {
            return new DeleteBannedDraftCardByCardIdInput(clientMutationId, cardId);
        }

    }
}
