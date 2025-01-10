package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `Card` object types. All fields are tested for equality and combined with a logical ‘and.’
 */
public class CardCondition implements java.io.Serializable {

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
    private String succession;
    private Boolean collectible;
    private String type;
    private Integer cost;

    public CardCondition() {
    }

    public CardCondition(String id, String createdBy, String uri, String blocklyWorkspace, String cardScript, String createdAt, String lastModified, Boolean isArchived, Boolean isPublished, String succession, Boolean collectible, String type, Integer cost) {
        this.id = id;
        this.createdBy = createdBy;
        this.uri = uri;
        this.blocklyWorkspace = blocklyWorkspace;
        this.cardScript = cardScript;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.isArchived = isArchived;
        this.isPublished = isPublished;
        this.succession = succession;
        this.collectible = collectible;
        this.type = type;
        this.cost = cost;
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

    public String getSuccession() {
        return succession;
    }
    public void setSuccession(String succession) {
        this.succession = succession;
    }

    public Boolean getCollectible() {
        return collectible;
    }
    public void setCollectible(Boolean collectible) {
        this.collectible = collectible;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Integer getCost() {
        return cost;
    }
    public void setCost(Integer cost) {
        this.cost = cost;
    }



    public static CardCondition.Builder builder() {
        return new CardCondition.Builder();
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
        private String succession;
        private Boolean collectible;
        private String type;
        private Integer cost;

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

        public Builder setSuccession(String succession) {
            this.succession = succession;
            return this;
        }

        public Builder setCollectible(Boolean collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setCost(Integer cost) {
            this.cost = cost;
            return this;
        }


        public CardCondition build() {
            return new CardCondition(id, createdBy, uri, blocklyWorkspace, cardScript, createdAt, lastModified, isArchived, isPublished, succession, collectible, type, cost);
        }

    }
}
