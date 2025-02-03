package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `GeneratedArt` mutation.
 */
public class CreateGeneratedArtInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GeneratedArtInput generatedArt;

    public CreateGeneratedArtInput() {
    }

    public CreateGeneratedArtInput(String clientMutationId, GeneratedArtInput generatedArt) {
        this.clientMutationId = clientMutationId;
        this.generatedArt = generatedArt;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public GeneratedArtInput getGeneratedArt() {
        return generatedArt;
    }
    public void setGeneratedArt(GeneratedArtInput generatedArt) {
        this.generatedArt = generatedArt;
    }



    public static CreateGeneratedArtInput.Builder builder() {
        return new CreateGeneratedArtInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private GeneratedArtInput generatedArt;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setGeneratedArt(GeneratedArtInput generatedArt) {
            this.generatedArt = generatedArt;
            return this;
        }


        public CreateGeneratedArtInput build() {
            return new CreateGeneratedArtInput(clientMutationId, generatedArt);
        }

    }
}
