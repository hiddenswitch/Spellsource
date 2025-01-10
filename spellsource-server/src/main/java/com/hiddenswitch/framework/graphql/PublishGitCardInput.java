package com.hiddenswitch.framework.graphql;


/**
 * All input for the `publishGitCard` mutation.
 */
public class PublishGitCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;
    private String json;
    private String creator;

    public PublishGitCardInput() {
    }

    public PublishGitCardInput(String clientMutationId, String cardId, String json, String creator) {
        this.clientMutationId = clientMutationId;
        this.cardId = cardId;
        this.json = json;
        this.creator = creator;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getJson() {
        return json;
    }
    public void setJson(String json) {
        this.json = json;
    }

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }



    public static PublishGitCardInput.Builder builder() {
        return new PublishGitCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardId;
        private String json;
        private String creator;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setJson(String json) {
            this.json = json;
            return this;
        }

        public Builder setCreator(String creator) {
            this.creator = creator;
            return this;
        }


        public PublishGitCardInput build() {
            return new PublishGitCardInput(clientMutationId, cardId, json, creator);
        }

    }
}
