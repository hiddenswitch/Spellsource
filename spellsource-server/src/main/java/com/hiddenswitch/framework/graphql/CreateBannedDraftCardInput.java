package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `BannedDraftCard` mutation.
 */
public class CreateBannedDraftCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private BannedDraftCardInput bannedDraftCard;

    public CreateBannedDraftCardInput() {
    }

    public CreateBannedDraftCardInput(String clientMutationId, BannedDraftCardInput bannedDraftCard) {
        this.clientMutationId = clientMutationId;
        this.bannedDraftCard = bannedDraftCard;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public BannedDraftCardInput getBannedDraftCard() {
        return bannedDraftCard;
    }
    public void setBannedDraftCard(BannedDraftCardInput bannedDraftCard) {
        this.bannedDraftCard = bannedDraftCard;
    }



    public static CreateBannedDraftCardInput.Builder builder() {
        return new CreateBannedDraftCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private BannedDraftCardInput bannedDraftCard;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setBannedDraftCard(BannedDraftCardInput bannedDraftCard) {
            this.bannedDraftCard = bannedDraftCard;
            return this;
        }


        public CreateBannedDraftCardInput build() {
            return new CreateBannedDraftCardInput(clientMutationId, bannedDraftCard);
        }

    }
}
