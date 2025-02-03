package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deletePublishedCardById` mutation.
 */
public class DeletePublishedCardByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;

    public DeletePublishedCardByIdInput() {
    }

    public DeletePublishedCardByIdInput(String clientMutationId, String id) {
        this.clientMutationId = clientMutationId;
        this.id = id;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    public static DeletePublishedCardByIdInput.Builder builder() {
        return new DeletePublishedCardByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }


        public DeletePublishedCardByIdInput build() {
            return new DeletePublishedCardByIdInput(clientMutationId, id);
        }

    }
}
