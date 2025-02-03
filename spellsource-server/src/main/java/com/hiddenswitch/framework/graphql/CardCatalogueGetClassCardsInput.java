package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetClassCards` mutation.
 */
public class CardCatalogueGetClassCardsInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;

    public CardCatalogueGetClassCardsInput() {
    }

    public CardCatalogueGetClassCardsInput(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }



    public static CardCatalogueGetClassCardsInput.Builder builder() {
        return new CardCatalogueGetClassCardsInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }


        public CardCatalogueGetClassCardsInput build() {
            return new CardCatalogueGetClassCardsInput(clientMutationId);
        }

    }
}
