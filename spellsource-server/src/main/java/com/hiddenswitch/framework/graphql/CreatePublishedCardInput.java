package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `PublishedCard` mutation.
 */
public class CreatePublishedCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private PublishedCardInput publishedCard;

    public CreatePublishedCardInput() {
    }

    public CreatePublishedCardInput(String clientMutationId, PublishedCardInput publishedCard) {
        this.clientMutationId = clientMutationId;
        this.publishedCard = publishedCard;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public PublishedCardInput getPublishedCard() {
        return publishedCard;
    }
    public void setPublishedCard(PublishedCardInput publishedCard) {
        this.publishedCard = publishedCard;
    }



    public static CreatePublishedCardInput.Builder builder() {
        return new CreatePublishedCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private PublishedCardInput publishedCard;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setPublishedCard(PublishedCardInput publishedCard) {
            this.publishedCard = publishedCard;
            return this;
        }


        public CreatePublishedCardInput build() {
            return new CreatePublishedCardInput(clientMutationId, publishedCard);
        }

    }
}
