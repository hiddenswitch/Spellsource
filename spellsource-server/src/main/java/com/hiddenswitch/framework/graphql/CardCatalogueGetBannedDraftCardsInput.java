package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetBannedDraftCards` mutation.
 */
public class CardCatalogueGetBannedDraftCardsInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;

    public CardCatalogueGetBannedDraftCardsInput() {
    }

    public CardCatalogueGetBannedDraftCardsInput(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }



    public static CardCatalogueGetBannedDraftCardsInput.Builder builder() {
        return new CardCatalogueGetBannedDraftCardsInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }


        public CardCatalogueGetBannedDraftCardsInput build() {
            return new CardCatalogueGetBannedDraftCardsInput(clientMutationId);
        }

    }
}
