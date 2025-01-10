package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGeneratedArtByHashAndOwner` mutation.
 */
public class UpdateGeneratedArtByHashAndOwnerInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String hash;
    private String owner;
    private GeneratedArtPatch generatedArtPatch;

    public UpdateGeneratedArtByHashAndOwnerInput() {
    }

    public UpdateGeneratedArtByHashAndOwnerInput(String clientMutationId, String hash, String owner, GeneratedArtPatch generatedArtPatch) {
        this.clientMutationId = clientMutationId;
        this.hash = hash;
        this.owner = owner;
        this.generatedArtPatch = generatedArtPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public GeneratedArtPatch getGeneratedArtPatch() {
        return generatedArtPatch;
    }
    public void setGeneratedArtPatch(GeneratedArtPatch generatedArtPatch) {
        this.generatedArtPatch = generatedArtPatch;
    }



    public static UpdateGeneratedArtByHashAndOwnerInput.Builder builder() {
        return new UpdateGeneratedArtByHashAndOwnerInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String hash;
        private String owner;
        private GeneratedArtPatch generatedArtPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder setGeneratedArtPatch(GeneratedArtPatch generatedArtPatch) {
            this.generatedArtPatch = generatedArtPatch;
            return this;
        }


        public UpdateGeneratedArtByHashAndOwnerInput build() {
            return new UpdateGeneratedArtByHashAndOwnerInput(clientMutationId, hash, owner, generatedArtPatch);
        }

    }
}
