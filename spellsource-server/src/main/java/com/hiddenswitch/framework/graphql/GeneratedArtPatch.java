package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `GeneratedArt`. Fields that are set will be updated.
 */
public class GeneratedArtPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String hash;
    private String owner;
    private java.util.List<String> urls;
    private String info;
    private Boolean isArchived;

    public GeneratedArtPatch() {
    }

    public GeneratedArtPatch(String hash, String owner, java.util.List<String> urls, String info, Boolean isArchived) {
        this.hash = hash;
        this.owner = owner;
        this.urls = urls;
        this.info = info;
        this.isArchived = isArchived;
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

    public java.util.List<String> getUrls() {
        return urls;
    }
    public void setUrls(java.util.List<String> urls) {
        this.urls = urls;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }



    public static GeneratedArtPatch.Builder builder() {
        return new GeneratedArtPatch.Builder();
    }

    public static class Builder {

        private String hash;
        private String owner;
        private java.util.List<String> urls;
        private String info;
        private Boolean isArchived;

        public Builder() {
        }

        public Builder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder setUrls(java.util.List<String> urls) {
            this.urls = urls;
            return this;
        }

        public Builder setInfo(String info) {
            this.info = info;
            return this;
        }

        public Builder setIsArchived(Boolean isArchived) {
            this.isArchived = isArchived;
            return this;
        }


        public GeneratedArtPatch build() {
            return new GeneratedArtPatch(hash, owner, urls, info, isArchived);
        }

    }
}
