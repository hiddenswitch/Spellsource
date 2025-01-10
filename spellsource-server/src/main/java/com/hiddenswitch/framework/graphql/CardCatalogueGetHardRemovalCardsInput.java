package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetHardRemovalCards` mutation.
 */
public class CardCatalogueGetHardRemovalCardsInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;

    public CardCatalogueGetHardRemovalCardsInput() {
    }

    public CardCatalogueGetHardRemovalCardsInput(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }



    public static CardCatalogueGetHardRemovalCardsInput.Builder builder() {
        return new CardCatalogueGetHardRemovalCardsInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }


        public CardCatalogueGetHardRemovalCardsInput build() {
            return new CardCatalogueGetHardRemovalCardsInput(clientMutationId);
        }

    }
}
