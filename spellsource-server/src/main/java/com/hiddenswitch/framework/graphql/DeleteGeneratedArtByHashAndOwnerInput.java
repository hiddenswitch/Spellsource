package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteGeneratedArtByHashAndOwner` mutation.
 */
public class DeleteGeneratedArtByHashAndOwnerInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String hash;
    private String owner;

    public DeleteGeneratedArtByHashAndOwnerInput() {
    }

    public DeleteGeneratedArtByHashAndOwnerInput(String clientMutationId, String hash, String owner) {
        this.clientMutationId = clientMutationId;
        this.hash = hash;
        this.owner = owner;
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



    public static DeleteGeneratedArtByHashAndOwnerInput.Builder builder() {
        return new DeleteGeneratedArtByHashAndOwnerInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String hash;
        private String owner;

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


        public DeleteGeneratedArtByHashAndOwnerInput build() {
            return new DeleteGeneratedArtByHashAndOwnerInput(clientMutationId, hash, owner);
        }

    }
}
