package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetCardByName` mutation.
 */
public class CardCatalogueGetCardByNameInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardName;

    public CardCatalogueGetCardByNameInput() {
    }

    public CardCatalogueGetCardByNameInput(String clientMutationId, String cardName) {
        this.clientMutationId = clientMutationId;
        this.cardName = cardName;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getCardName() {
        return cardName;
    }
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }



    public static CardCatalogueGetCardByNameInput.Builder builder() {
        return new CardCatalogueGetCardByNameInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardName;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setCardName(String cardName) {
            this.cardName = cardName;
            return this;
        }


        public CardCatalogueGetCardByNameInput build() {
            return new CardCatalogueGetCardByNameInput(clientMutationId, cardName);
        }

    }
}
