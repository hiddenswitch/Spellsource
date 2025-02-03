package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateBannedDraftCardByCardId` mutation.
 */
public class UpdateBannedDraftCardByCardIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;
    private BannedDraftCardPatch bannedDraftCardPatch;

    public UpdateBannedDraftCardByCardIdInput() {
    }

    public UpdateBannedDraftCardByCardIdInput(String clientMutationId, String cardId, BannedDraftCardPatch bannedDraftCardPatch) {
        this.clientMutationId = clientMutationId;
        this.cardId = cardId;
        this.bannedDraftCardPatch = bannedDraftCardPatch;
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

    public BannedDraftCardPatch getBannedDraftCardPatch() {
        return bannedDraftCardPatch;
    }
    public void setBannedDraftCardPatch(BannedDraftCardPatch bannedDraftCardPatch) {
        this.bannedDraftCardPatch = bannedDraftCardPatch;
    }



    public static UpdateBannedDraftCardByCardIdInput.Builder builder() {
        return new UpdateBannedDraftCardByCardIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardId;
        private BannedDraftCardPatch bannedDraftCardPatch;

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

        public Builder setBannedDraftCardPatch(BannedDraftCardPatch bannedDraftCardPatch) {
            this.bannedDraftCardPatch = bannedDraftCardPatch;
            return this;
        }


        public UpdateBannedDraftCardByCardIdInput build() {
            return new UpdateBannedDraftCardByCardIdInput(clientMutationId, cardId, bannedDraftCardPatch);
        }

    }
}
