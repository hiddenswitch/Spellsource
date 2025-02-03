package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetFormat` mutation.
 */
public class CardCatalogueGetFormatInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardName;

    public CardCatalogueGetFormatInput() {
    }

    public CardCatalogueGetFormatInput(String clientMutationId, String cardName) {
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



    public static CardCatalogueGetFormatInput.Builder builder() {
        return new CardCatalogueGetFormatInput.Builder();
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


        public CardCatalogueGetFormatInput build() {
            return new CardCatalogueGetFormatInput(clientMutationId, cardName);
        }

    }
}
