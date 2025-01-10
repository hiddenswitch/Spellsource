package com.hiddenswitch.framework.graphql;


/**
 * All input for the `cardCatalogueFormats` mutation.
 */
public class CardCatalogueFormatsInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;

    public CardCatalogueFormatsInput() {
    }

    public CardCatalogueFormatsInput(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }



    public static CardCatalogueFormatsInput.Builder builder() {
        return new CardCatalogueFormatsInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }


        public CardCatalogueFormatsInput build() {
            return new CardCatalogueFormatsInput(clientMutationId);
        }

    }
}
