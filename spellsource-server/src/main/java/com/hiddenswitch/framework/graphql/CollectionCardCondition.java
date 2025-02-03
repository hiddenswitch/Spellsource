package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `CollectionCard` object types. All fields are
tested for equality and combined with a logical ‘and.’
 */
public class CollectionCardCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private String cardScript;
    private String blocklyWorkspace;
    private String name;
    private String type;
    private String Class;
    private Integer cost;
    private Boolean collectible;
    private String searchMessage;
    private String lastModified;
    private String createdAt;

    public CollectionCardCondition() {
    }

    public CollectionCardCondition(String id, String createdBy, String cardScript, String blocklyWorkspace, String name, String type, String Class, Integer cost, Boolean collectible, String searchMessage, String lastModified, String createdAt) {
        this.id = id;
        this.createdBy = createdBy;
        this.cardScript = cardScript;
        this.blocklyWorkspace = blocklyWorkspace;
        this.name = name;
        this.type = type;
        this.Class = Class;
        this.cost = cost;
        this.collectible = collectible;
        this.searchMessage = searchMessage;
        this.lastModified = lastModified;
        this.createdAt = createdAt;
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

    public String getCardScript() {
        return cardScript;
    }
    public void setCardScript(String cardScript) {
        this.cardScript = cardScript;
    }

    public String getBlocklyWorkspace() {
        return blocklyWorkspace;
    }
    public void setBlocklyWorkspace(String blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String GetClass() {
        return Class;
    }
    public void setClass(String Class) {
        this.Class = Class;
    }

    public Integer getCost() {
        return cost;
    }
    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Boolean getCollectible() {
        return collectible;
    }
    public void setCollectible(Boolean collectible) {
        this.collectible = collectible;
    }

    public String getSearchMessage() {
        return searchMessage;
    }
    public void setSearchMessage(String searchMessage) {
        this.searchMessage = searchMessage;
    }

    public String getLastModified() {
        return lastModified;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }



    public static CollectionCardCondition.Builder builder() {
        return new CollectionCardCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String createdBy;
        private String cardScript;
        private String blocklyWorkspace;
        private String name;
        private String type;
        private String Class;
        private Integer cost;
        private Boolean collectible;
        private String searchMessage;
        private String lastModified;
        private String createdAt;

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

        public Builder setCardScript(String cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setBlocklyWorkspace(String blocklyWorkspace) {
            this.blocklyWorkspace = blocklyWorkspace;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setClass(String Class) {
            this.Class = Class;
            return this;
        }

        public Builder setCost(Integer cost) {
            this.cost = cost;
            return this;
        }

        public Builder setCollectible(Boolean collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setSearchMessage(String searchMessage) {
            this.searchMessage = searchMessage;
            return this;
        }

        public Builder setLastModified(String lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }


        public CollectionCardCondition build() {
            return new CollectionCardCondition(id, createdBy, cardScript, blocklyWorkspace, name, type, Class, cost, collectible, searchMessage, lastModified, createdAt);
        }

    }
}
