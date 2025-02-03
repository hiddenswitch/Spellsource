package com.hiddenswitch.framework.graphql;


/**
 * All input for the `saveCard` mutation.
 */
public class SaveCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;
    private String workspace;
    private String json;

    public SaveCardInput() {
    }

    public SaveCardInput(String clientMutationId, String cardId, String workspace, String json) {
        this.clientMutationId = clientMutationId;
        this.cardId = cardId;
        this.workspace = workspace;
        this.json = json;
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

    public String getWorkspace() {
        return workspace;
    }
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getJson() {
        return json;
    }
    public void setJson(String json) {
        this.json = json;
    }



    public static SaveCardInput.Builder builder() {
        return new SaveCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardId;
        private String workspace;
        private String json;

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

        public Builder setWorkspace(String workspace) {
            this.workspace = workspace;
            return this;
        }

        public Builder setJson(String json) {
            this.json = json;
            return this;
        }


        public SaveCardInput build() {
            return new SaveCardInput(clientMutationId, cardId, workspace, json);
        }

    }
}
