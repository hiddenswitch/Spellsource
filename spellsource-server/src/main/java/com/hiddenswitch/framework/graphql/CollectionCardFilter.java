package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `CollectionCard` object types. All fields are combined with a logical ‘and.’
 */
public class CollectionCardFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private StringFilter createdBy;
    private JSONFilter cardScript;
    private JSONFilter blocklyWorkspace;
    private StringFilter name;
    private StringFilter type;
    private StringFilter Class;
    private IntFilter cost;
    private BooleanFilter collectible;
    private StringFilter searchMessage;
    private DatetimeFilter lastModified;
    private DatetimeFilter createdAt;
    private java.util.List<CollectionCardFilter> and;
    private java.util.List<CollectionCardFilter> or;
    private CollectionCardFilter not;

    public CollectionCardFilter() {
    }

    public CollectionCardFilter(StringFilter id, StringFilter createdBy, JSONFilter cardScript, JSONFilter blocklyWorkspace, StringFilter name, StringFilter type, StringFilter Class, IntFilter cost, BooleanFilter collectible, StringFilter searchMessage, DatetimeFilter lastModified, DatetimeFilter createdAt, java.util.List<CollectionCardFilter> and, java.util.List<CollectionCardFilter> or, CollectionCardFilter not) {
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
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getId() {
        return id;
    }
    public void setId(StringFilter id) {
        this.id = id;
    }

    public StringFilter getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    public JSONFilter getCardScript() {
        return cardScript;
    }
    public void setCardScript(JSONFilter cardScript) {
        this.cardScript = cardScript;
    }

    public JSONFilter getBlocklyWorkspace() {
        return blocklyWorkspace;
    }
    public void setBlocklyWorkspace(JSONFilter blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
    }

    public StringFilter getName() {
        return name;
    }
    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getType() {
        return type;
    }
    public void setType(StringFilter type) {
        this.type = type;
    }

    public StringFilter GetClass() {
        return Class;
    }
    public void setClass(StringFilter Class) {
        this.Class = Class;
    }

    public IntFilter getCost() {
        return cost;
    }
    public void setCost(IntFilter cost) {
        this.cost = cost;
    }

    public BooleanFilter getCollectible() {
        return collectible;
    }
    public void setCollectible(BooleanFilter collectible) {
        this.collectible = collectible;
    }

    public StringFilter getSearchMessage() {
        return searchMessage;
    }
    public void setSearchMessage(StringFilter searchMessage) {
        this.searchMessage = searchMessage;
    }

    public DatetimeFilter getLastModified() {
        return lastModified;
    }
    public void setLastModified(DatetimeFilter lastModified) {
        this.lastModified = lastModified;
    }

    public DatetimeFilter getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(DatetimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.List<CollectionCardFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<CollectionCardFilter> and) {
        this.and = and;
    }

    public java.util.List<CollectionCardFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<CollectionCardFilter> or) {
        this.or = or;
    }

    public CollectionCardFilter getNot() {
        return not;
    }
    public void setNot(CollectionCardFilter not) {
        this.not = not;
    }



    public static CollectionCardFilter.Builder builder() {
        return new CollectionCardFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private StringFilter createdBy;
        private JSONFilter cardScript;
        private JSONFilter blocklyWorkspace;
        private StringFilter name;
        private StringFilter type;
        private StringFilter Class;
        private IntFilter cost;
        private BooleanFilter collectible;
        private StringFilter searchMessage;
        private DatetimeFilter lastModified;
        private DatetimeFilter createdAt;
        private java.util.List<CollectionCardFilter> and;
        private java.util.List<CollectionCardFilter> or;
        private CollectionCardFilter not;

        public Builder() {
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setCreatedBy(StringFilter createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setCardScript(JSONFilter cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setBlocklyWorkspace(JSONFilter blocklyWorkspace) {
            this.blocklyWorkspace = blocklyWorkspace;
            return this;
        }

        public Builder setName(StringFilter name) {
            this.name = name;
            return this;
        }

        public Builder setType(StringFilter type) {
            this.type = type;
            return this;
        }

        public Builder setClass(StringFilter Class) {
            this.Class = Class;
            return this;
        }

        public Builder setCost(IntFilter cost) {
            this.cost = cost;
            return this;
        }

        public Builder setCollectible(BooleanFilter collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setSearchMessage(StringFilter searchMessage) {
            this.searchMessage = searchMessage;
            return this;
        }

        public Builder setLastModified(DatetimeFilter lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setCreatedAt(DatetimeFilter createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setAnd(java.util.List<CollectionCardFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<CollectionCardFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(CollectionCardFilter not) {
            this.not = not;
            return this;
        }


        public CollectionCardFilter build() {
            return new CollectionCardFilter(id, createdBy, cardScript, blocklyWorkspace, name, type, Class, cost, collectible, searchMessage, lastModified, createdAt, and, or, not);
        }

    }
}
