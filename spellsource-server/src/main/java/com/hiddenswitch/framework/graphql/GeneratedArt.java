package com.hiddenswitch.framework.graphql;


public class GeneratedArt implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String hash;
    private String owner;
    private java.util.List<String> urls;
    private String info;
    private boolean isArchived;

    public GeneratedArt() {
    }

    public GeneratedArt(String hash, String owner, java.util.List<String> urls, String info, boolean isArchived) {
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

    public boolean getIsArchived() {
        return isArchived;
    }
    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }



    public static GeneratedArt.Builder builder() {
        return new GeneratedArt.Builder();
    }

    public static class Builder {

        private String hash;
        private String owner;
        private java.util.List<String> urls;
        private String info;
        private boolean isArchived;

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

        public Builder setIsArchived(boolean isArchived) {
            this.isArchived = isArchived;
            return this;
        }


        public GeneratedArt build() {
            return new GeneratedArt(hash, owner, urls, info, isArchived);
        }

    }
}
