package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `Class` object types. All fields are combined with a logical ‘and.’
 */
public class ClassFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter createdBy;
    private StringFilter Class;
    private BooleanFilter isPublished;
    private BooleanFilter collectible;
    private JSONFilter cardScript;
    private StringFilter id;
    private StringFilter name;
    private java.util.List<ClassFilter> and;
    private java.util.List<ClassFilter> or;
    private ClassFilter not;

    public ClassFilter() {
    }

    public ClassFilter(StringFilter createdBy, StringFilter Class, BooleanFilter isPublished, BooleanFilter collectible, JSONFilter cardScript, StringFilter id, StringFilter name, java.util.List<ClassFilter> and, java.util.List<ClassFilter> or, ClassFilter not) {
        this.createdBy = createdBy;
        this.Class = Class;
        this.isPublished = isPublished;
        this.collectible = collectible;
        this.cardScript = cardScript;
        this.id = id;
        this.name = name;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    public StringFilter GetClass() {
        return Class;
    }
    public void setClass(StringFilter Class) {
        this.Class = Class;
    }

    public BooleanFilter getIsPublished() {
        return isPublished;
    }
    public void setIsPublished(BooleanFilter isPublished) {
        this.isPublished = isPublished;
    }

    public BooleanFilter getCollectible() {
        return collectible;
    }
    public void setCollectible(BooleanFilter collectible) {
        this.collectible = collectible;
    }

    public JSONFilter getCardScript() {
        return cardScript;
    }
    public void setCardScript(JSONFilter cardScript) {
        this.cardScript = cardScript;
    }

    public StringFilter getId() {
        return id;
    }
    public void setId(StringFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }
    public void setName(StringFilter name) {
        this.name = name;
    }

    public java.util.List<ClassFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<ClassFilter> and) {
        this.and = and;
    }

    public java.util.List<ClassFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<ClassFilter> or) {
        this.or = or;
    }

    public ClassFilter getNot() {
        return not;
    }
    public void setNot(ClassFilter not) {
        this.not = not;
    }



    public static ClassFilter.Builder builder() {
        return new ClassFilter.Builder();
    }

    public static class Builder {

        private StringFilter createdBy;
        private StringFilter Class;
        private BooleanFilter isPublished;
        private BooleanFilter collectible;
        private JSONFilter cardScript;
        private StringFilter id;
        private StringFilter name;
        private java.util.List<ClassFilter> and;
        private java.util.List<ClassFilter> or;
        private ClassFilter not;

        public Builder() {
        }

        public Builder setCreatedBy(StringFilter createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setClass(StringFilter Class) {
            this.Class = Class;
            return this;
        }

        public Builder setIsPublished(BooleanFilter isPublished) {
            this.isPublished = isPublished;
            return this;
        }

        public Builder setCollectible(BooleanFilter collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setCardScript(JSONFilter cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setName(StringFilter name) {
            this.name = name;
            return this;
        }

        public Builder setAnd(java.util.List<ClassFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<ClassFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(ClassFilter not) {
            this.not = not;
            return this;
        }


        public ClassFilter build() {
            return new ClassFilter(createdBy, Class, isPublished, collectible, cardScript, id, name, and, or, not);
        }

    }
}
