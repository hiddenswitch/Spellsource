package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetHeroCard` mutation.
 */
public class CardCatalogueGetHeroCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String heroClass;

    public CardCatalogueGetHeroCardInput() {
    }

    public CardCatalogueGetHeroCardInput(String clientMutationId, String heroClass) {
        this.clientMutationId = clientMutationId;
        this.heroClass = heroClass;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(String heroClass) {
        this.heroClass = heroClass;
    }



    public static CardCatalogueGetHeroCardInput.Builder builder() {
        return new CardCatalogueGetHeroCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String heroClass;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setHeroClass(String heroClass) {
            this.heroClass = heroClass;
            return this;
        }


        public CardCatalogueGetHeroCardInput build() {
            return new CardCatalogueGetHeroCardInput(clientMutationId, heroClass);
        }

    }
}
