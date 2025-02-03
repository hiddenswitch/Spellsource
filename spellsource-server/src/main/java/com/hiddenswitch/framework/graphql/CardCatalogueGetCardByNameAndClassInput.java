package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetCardByNameAndClass` mutation.
 */
public class CardCatalogueGetCardByNameAndClassInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardName;
    private String heroClass;

    public CardCatalogueGetCardByNameAndClassInput() {
    }

    public CardCatalogueGetCardByNameAndClassInput(String clientMutationId, String cardName, String heroClass) {
        this.clientMutationId = clientMutationId;
        this.cardName = cardName;
        this.heroClass = heroClass;
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

    public String getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(String heroClass) {
        this.heroClass = heroClass;
    }



    public static CardCatalogueGetCardByNameAndClassInput.Builder builder() {
        return new CardCatalogueGetCardByNameAndClassInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardName;
        private String heroClass;

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

        public Builder setHeroClass(String heroClass) {
            this.heroClass = heroClass;
            return this;
        }


        public CardCatalogueGetCardByNameAndClassInput build() {
            return new CardCatalogueGetCardByNameAndClassInput(clientMutationId, cardName, heroClass);
        }

    }
}
