package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `Card`
 */
public class CardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private String uri;
    private String blocklyWorkspace;
    private String cardScript;
    private String createdAt;
    private String lastModified;
    private Boolean isArchived;
    private Boolean isPublished;

    public CardInput() {
    }

    public CardInput(String id, String createdBy, String uri, String blocklyWorkspace, String cardScript, String createdAt, String lastModified, Boolean isArchived, Boolean isPublished) {
        this.id = id;
        this.createdBy = createdBy;
        this.uri = uri;
        this.blocklyWorkspace = blocklyWorkspace;
        this.cardScript = cardScript;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.isArchived = isArchived;
        this.isPublished = isPublished;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getBlocklyWorkspace() {
        return blocklyWorkspace;
    }
    public void setBlocklyWorkspace(String blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
    }

    public String getCardScript() {
        return cardScript;
    }
    public void setCardScript(String cardScript) {
        this.cardScript = cardScript;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModified() {
        return lastModified;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }



    public static CardInput.Builder builder() {
        return new CardInput.Builder();
    }

    public static class Builder {

        private String id;
        private String createdBy;
        private String uri;
        private String blocklyWorkspace;
        private String cardScript;
        private String createdAt;
        private String lastModified;
        private Boolean isArchived;
        private Boolean isPublished;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setBlocklyWorkspace(String blocklyWorkspace) {
            this.blocklyWorkspace = blocklyWorkspace;
            return this;
        }

        public Builder setCardScript(String cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setLastModified(String lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setIsArchived(Boolean isArchived) {
            this.isArchived = isArchived;
            return this;
        }

        public Builder setIsPublished(Boolean isPublished) {
            this.isPublished = isPublished;
            return this;
        }


        public CardInput build() {
            return new CardInput(id, createdBy, uri, blocklyWorkspace, cardScript, createdAt, lastModified, isArchived, isPublished);
        }

    }
}
