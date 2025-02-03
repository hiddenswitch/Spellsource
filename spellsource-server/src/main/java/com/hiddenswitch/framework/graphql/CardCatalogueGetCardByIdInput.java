package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetCardById` mutation.
 */
public class CardCatalogueGetCardByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;

    public CardCatalogueGetCardByIdInput() {
    }

    public CardCatalogueGetCardByIdInput(String clientMutationId, String cardId) {
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



    public static CardCatalogueGetCardByIdInput.Builder builder() {
        return new CardCatalogueGetCardByIdInput.Builder();
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


        public CardCatalogueGetCardByIdInput build() {
            return new CardCatalogueGetCardByIdInput(clientMutationId, cardId);
        }

    }
}
