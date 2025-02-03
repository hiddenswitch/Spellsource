package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteCardBySuccession` mutation.
 */
public class DeleteCardBySuccessionInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String succession;

    public DeleteCardBySuccessionInput() {
    }

    public DeleteCardBySuccessionInput(String clientMutationId, String succession) {
        this.clientMutationId = clientMutationId;
        this.succession = succession;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getSuccession() {
        return succession;
    }
    public void setSuccession(String succession) {
        this.succession = succession;
    }



    public static DeleteCardBySuccessionInput.Builder builder() {
        return new DeleteCardBySuccessionInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String succession;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setSuccession(String succession) {
            this.succession = succession;
            return this;
        }


        public DeleteCardBySuccessionInput build() {
            return new DeleteCardBySuccessionInput(clientMutationId, succession);
        }

    }
}
