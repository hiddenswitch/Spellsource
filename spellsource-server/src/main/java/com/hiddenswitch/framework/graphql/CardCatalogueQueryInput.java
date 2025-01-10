package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueQuery` mutation.
 */
public class CardCatalogueQueryInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private java.util.List<String> sets;
    private String cardType;
    private String rarity;
    private String heroClass;
    private String attribute;

    public CardCatalogueQueryInput() {
    }

    public CardCatalogueQueryInput(String clientMutationId, java.util.List<String> sets, String cardType, String rarity, String heroClass, String attribute) {
        this.clientMutationId = clientMutationId;
        this.sets = sets;
        this.cardType = cardType;
        this.rarity = rarity;
        this.heroClass = heroClass;
        this.attribute = attribute;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public java.util.List<String> getSets() {
        return sets;
    }
    public void setSets(java.util.List<String> sets) {
        this.sets = sets;
    }

    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getRarity() {
        return rarity;
    }
    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(String heroClass) {
        this.heroClass = heroClass;
    }

    public String getAttribute() {
        return attribute;
    }
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }



    public static CardCatalogueQueryInput.Builder builder() {
        return new CardCatalogueQueryInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private java.util.List<String> sets;
        private String cardType;
        private String rarity;
        private String heroClass;
        private String attribute;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setSets(java.util.List<String> sets) {
            this.sets = sets;
            return this;
        }

        public Builder setCardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public Builder setRarity(String rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder setHeroClass(String heroClass) {
            this.heroClass = heroClass;
            return this;
        }

        public Builder setAttribute(String attribute) {
            this.attribute = attribute;
            return this;
        }


        public CardCatalogueQueryInput build() {
            return new CardCatalogueQueryInput(clientMutationId, sets, cardType, rarity, heroClass, attribute);
        }

    }
}
