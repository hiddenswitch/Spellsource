package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `GeneratedArt` object types. All fields are combined with a logical ‘and.’
 */
public class GeneratedArtFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter hash;
    private StringFilter owner;
    private StringListFilter urls;
    private JSONFilter info;
    private BooleanFilter isArchived;
    private java.util.List<GeneratedArtFilter> and;
    private java.util.List<GeneratedArtFilter> or;
    private GeneratedArtFilter not;

    public GeneratedArtFilter() {
    }

    public GeneratedArtFilter(StringFilter hash, StringFilter owner, StringListFilter urls, JSONFilter info, BooleanFilter isArchived, java.util.List<GeneratedArtFilter> and, java.util.List<GeneratedArtFilter> or, GeneratedArtFilter not) {
        this.hash = hash;
        this.owner = owner;
        this.urls = urls;
        this.info = info;
        this.isArchived = isArchived;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getHash() {
        return hash;
    }
    public void setHash(StringFilter hash) {
        this.hash = hash;
    }

    public StringFilter getOwner() {
        return owner;
    }
    public void setOwner(StringFilter owner) {
        this.owner = owner;
    }

    public StringListFilter getUrls() {
        return urls;
    }
    public void setUrls(StringListFilter urls) {
        this.urls = urls;
    }

    public JSONFilter getInfo() {
        return info;
    }
    public void setInfo(JSONFilter info) {
        this.info = info;
    }

    public BooleanFilter getIsArchived() {
        return isArchived;
    }
    public void setIsArchived(BooleanFilter isArchived) {
        this.isArchived = isArchived;
    }

    public java.util.List<GeneratedArtFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<GeneratedArtFilter> and) {
        this.and = and;
    }

    public java.util.List<GeneratedArtFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<GeneratedArtFilter> or) {
        this.or = or;
    }

    public GeneratedArtFilter getNot() {
        return not;
    }
    public void setNot(GeneratedArtFilter not) {
        this.not = not;
    }



    public static GeneratedArtFilter.Builder builder() {
        return new GeneratedArtFilter.Builder();
    }

    public static class Builder {

        private StringFilter hash;
        private StringFilter owner;
        private StringListFilter urls;
        private JSONFilter info;
        private BooleanFilter isArchived;
        private java.util.List<GeneratedArtFilter> and;
        private java.util.List<GeneratedArtFilter> or;
        private GeneratedArtFilter not;

        public Builder() {
        }

        public Builder setHash(StringFilter hash) {
            this.hash = hash;
            return this;
        }

        public Builder setOwner(StringFilter owner) {
            this.owner = owner;
            return this;
        }

        public Builder setUrls(StringListFilter urls) {
            this.urls = urls;
            return this;
        }

        public Builder setInfo(JSONFilter info) {
            this.info = info;
            return this;
        }

        public Builder setIsArchived(BooleanFilter isArchived) {
            this.isArchived = isArchived;
            return this;
        }

        public Builder setAnd(java.util.List<GeneratedArtFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<GeneratedArtFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(GeneratedArtFilter not) {
            this.not = not;
            return this;
        }


        public GeneratedArtFilter build() {
            return new GeneratedArtFilter(hash, owner, urls, info, isArchived, and, or, not);
        }

    }
}
