package com.hiddenswitch.framework.graphql;


/**
 * All input for the `getCollectionCards` mutation.
 */
public class GetCollectionCardsInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;

    public GetCollectionCardsInput() {
    }

    public GetCollectionCardsInput(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }



    public static GetCollectionCardsInput.Builder builder() {
        return new GetCollectionCardsInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }


        public GetCollectionCardsInput build() {
            return new GetCollectionCardsInput(clientMutationId);
        }

    }
}
