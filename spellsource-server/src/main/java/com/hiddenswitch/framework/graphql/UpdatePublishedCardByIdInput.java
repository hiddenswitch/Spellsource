package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updatePublishedCardById` mutation.
 */
public class UpdatePublishedCardByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private PublishedCardPatch publishedCardPatch;

    public UpdatePublishedCardByIdInput() {
    }

    public UpdatePublishedCardByIdInput(String clientMutationId, String id, PublishedCardPatch publishedCardPatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.publishedCardPatch = publishedCardPatch;
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

    public PublishedCardPatch getPublishedCardPatch() {
        return publishedCardPatch;
    }
    public void setPublishedCardPatch(PublishedCardPatch publishedCardPatch) {
        this.publishedCardPatch = publishedCardPatch;
    }



    public static UpdatePublishedCardByIdInput.Builder builder() {
        return new UpdatePublishedCardByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private PublishedCardPatch publishedCardPatch;

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

        public Builder setPublishedCardPatch(PublishedCardPatch publishedCardPatch) {
            this.publishedCardPatch = publishedCardPatch;
            return this;
        }


        public UpdatePublishedCardByIdInput build() {
            return new UpdatePublishedCardByIdInput(clientMutationId, id, publishedCardPatch);
        }

    }
}
