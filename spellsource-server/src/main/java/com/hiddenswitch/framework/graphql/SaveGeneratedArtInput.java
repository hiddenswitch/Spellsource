package com.hiddenswitch.framework.graphql;


/**
 * All input for the `saveGeneratedArt` mutation.
 */
public class SaveGeneratedArtInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String digest;
    private java.util.List<String> links;
    private String extraInfo;

    public SaveGeneratedArtInput() {
    }

    public SaveGeneratedArtInput(String clientMutationId, String digest, java.util.List<String> links, String extraInfo) {
        this.clientMutationId = clientMutationId;
        this.digest = digest;
        this.links = links;
        this.extraInfo = extraInfo;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getDigest() {
        return digest;
    }
    public void setDigest(String digest) {
        this.digest = digest;
    }

    public java.util.List<String> getLinks() {
        return links;
    }
    public void setLinks(java.util.List<String> links) {
        this.links = links;
    }

    public String getExtraInfo() {
        return extraInfo;
    }
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }



    public static SaveGeneratedArtInput.Builder builder() {
        return new SaveGeneratedArtInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String digest;
        private java.util.List<String> links;
        private String extraInfo;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDigest(String digest) {
            this.digest = digest;
            return this;
        }

        public Builder setLinks(java.util.List<String> links) {
            this.links = links;
            return this;
        }

        public Builder setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }


        public SaveGeneratedArtInput build() {
            return new SaveGeneratedArtInput(clientMutationId, digest, links, extraInfo);
        }

    }
}
