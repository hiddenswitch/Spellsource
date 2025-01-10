package com.hiddenswitch.framework.graphql;


/**
 * All input for the `getClasses` mutation.
 */
public class GetClassesInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;

    public GetClassesInput() {
    }

    public GetClassesInput(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }



    public static GetClassesInput.Builder builder() {
        return new GetClassesInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }


        public GetClassesInput build() {
            return new GetClassesInput(clientMutationId);
        }

    }
}
