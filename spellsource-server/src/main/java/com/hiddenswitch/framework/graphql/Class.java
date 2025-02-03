package com.hiddenswitch.framework.graphql;


public class Class implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String createdBy;
    private String Class;
    private Boolean isPublished;
    private Boolean collectible;
    private String cardScript;
    private String id;
    private String name;

    public Class() {
    }

    public Class(String createdBy, String Class, Boolean isPublished, Boolean collectible, String cardScript, String id, String name) {
        this.createdBy = createdBy;
        this.Class = Class;
        this.isPublished = isPublished;
        this.collectible = collectible;
        this.cardScript = cardScript;
        this.id = id;
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String GetClass() {
        return Class;
    }
    public void setClass(String Class) {
        this.Class = Class;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Boolean getCollectible() {
        return collectible;
    }
    public void setCollectible(Boolean collectible) {
        this.collectible = collectible;
    }

    public String getCardScript() {
        return cardScript;
    }
    public void setCardScript(String cardScript) {
        this.cardScript = cardScript;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }



    public static Class.Builder builder() {
        return new Class.Builder();
    }

    public static class Builder {

        private String createdBy;
        private String Class;
        private Boolean isPublished;
        private Boolean collectible;
        private String cardScript;
        private String id;
        private String name;

        public Builder() {
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setClass(String Class) {
            this.Class = Class;
            return this;
        }

        public Builder setIsPublished(Boolean isPublished) {
            this.isPublished = isPublished;
            return this;
        }

        public Builder setCollectible(Boolean collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setCardScript(String cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }


        public Class build() {
            return new Class(createdBy, Class, isPublished, collectible, cardScript, id, name);
        }

    }
}
