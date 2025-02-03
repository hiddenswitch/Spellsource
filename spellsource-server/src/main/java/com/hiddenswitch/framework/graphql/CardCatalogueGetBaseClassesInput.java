package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueGetBaseClasses` mutation.
 */
public class CardCatalogueGetBaseClassesInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private java.util.List<String> sets;

    public CardCatalogueGetBaseClassesInput() {
    }

    public CardCatalogueGetBaseClassesInput(String clientMutationId, java.util.List<String> sets) {
        this.clientMutationId = clientMutationId;
        this.sets = sets;
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



    public static CardCatalogueGetBaseClassesInput.Builder builder() {
        return new CardCatalogueGetBaseClassesInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private java.util.List<String> sets;

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


        public CardCatalogueGetBaseClassesInput build() {
            return new CardCatalogueGetBaseClassesInput(clientMutationId, sets);
        }

    }
}
