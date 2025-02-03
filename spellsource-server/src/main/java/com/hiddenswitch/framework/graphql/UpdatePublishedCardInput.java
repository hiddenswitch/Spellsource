package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updatePublishedCard` mutation.
 */
public class UpdatePublishedCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private PublishedCardPatch publishedCardPatch;

    public UpdatePublishedCardInput() {
    }

    public UpdatePublishedCardInput(String clientMutationId, String nodeId, PublishedCardPatch publishedCardPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.publishedCardPatch = publishedCardPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public PublishedCardPatch getPublishedCardPatch() {
        return publishedCardPatch;
    }
    public void setPublishedCardPatch(PublishedCardPatch publishedCardPatch) {
        this.publishedCardPatch = publishedCardPatch;
    }



    public static UpdatePublishedCardInput.Builder builder() {
        return new UpdatePublishedCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private PublishedCardPatch publishedCardPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setPublishedCardPatch(PublishedCardPatch publishedCardPatch) {
            this.publishedCardPatch = publishedCardPatch;
            return this;
        }


        public UpdatePublishedCardInput build() {
            return new UpdatePublishedCardInput(clientMutationId, nodeId, publishedCardPatch);
        }

    }
}
