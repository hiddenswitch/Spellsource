package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `Card` object types. All fields are combined with a logical ‘and.’
 */
public class CardFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private StringFilter createdBy;
    private StringFilter uri;
    private JSONFilter blocklyWorkspace;
    private JSONFilter cardScript;
    private DatetimeFilter createdAt;
    private DatetimeFilter lastModified;
    private BooleanFilter isArchived;
    private BooleanFilter isPublished;
    private BigIntFilter succession;
    private BooleanFilter collectible;
    private StringFilter type;
    private IntFilter cost;
    private CardToManyPublishedCardFilter publishedCardsBySuccession;
    private Boolean publishedCardsBySuccessionExist;
    private java.util.List<CardFilter> and;
    private java.util.List<CardFilter> or;
    private CardFilter not;

    public CardFilter() {
    }

    public CardFilter(StringFilter id, StringFilter createdBy, StringFilter uri, JSONFilter blocklyWorkspace, JSONFilter cardScript, DatetimeFilter createdAt, DatetimeFilter lastModified, BooleanFilter isArchived, BooleanFilter isPublished, BigIntFilter succession, BooleanFilter collectible, StringFilter type, IntFilter cost, CardToManyPublishedCardFilter publishedCardsBySuccession, Boolean publishedCardsBySuccessionExist, java.util.List<CardFilter> and, java.util.List<CardFilter> or, CardFilter not) {
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
        this.publishedCardsBySuccession = publishedCardsBySuccession;
        this.publishedCardsBySuccessionExist = publishedCardsBySuccessionExist;
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

    public StringFilter getUri() {
        return uri;
    }
    public void setUri(StringFilter uri) {
        this.uri = uri;
    }

    public JSONFilter getBlocklyWorkspace() {
        return blocklyWorkspace;
    }
    public void setBlocklyWorkspace(JSONFilter blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
    }

    public JSONFilter getCardScript() {
        return cardScript;
    }
    public void setCardScript(JSONFilter cardScript) {
        this.cardScript = cardScript;
    }

    public DatetimeFilter getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(DatetimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public DatetimeFilter getLastModified() {
        return lastModified;
    }
    public void setLastModified(DatetimeFilter lastModified) {
        this.lastModified = lastModified;
    }

    public BooleanFilter getIsArchived() {
        return isArchived;
    }
    public void setIsArchived(BooleanFilter isArchived) {
        this.isArchived = isArchived;
    }

    public BooleanFilter getIsPublished() {
        return isPublished;
    }
    public void setIsPublished(BooleanFilter isPublished) {
        this.isPublished = isPublished;
    }

    public BigIntFilter getSuccession() {
        return succession;
    }
    public void setSuccession(BigIntFilter succession) {
        this.succession = succession;
    }

    public BooleanFilter getCollectible() {
        return collectible;
    }
    public void setCollectible(BooleanFilter collectible) {
        this.collectible = collectible;
    }

    public StringFilter getType() {
        return type;
    }
    public void setType(StringFilter type) {
        this.type = type;
    }

    public IntFilter getCost() {
        return cost;
    }
    public void setCost(IntFilter cost) {
        this.cost = cost;
    }

    public CardToManyPublishedCardFilter getPublishedCardsBySuccession() {
        return publishedCardsBySuccession;
    }
    public void setPublishedCardsBySuccession(CardToManyPublishedCardFilter publishedCardsBySuccession) {
        this.publishedCardsBySuccession = publishedCardsBySuccession;
    }

    public Boolean getPublishedCardsBySuccessionExist() {
        return publishedCardsBySuccessionExist;
    }
    public void setPublishedCardsBySuccessionExist(Boolean publishedCardsBySuccessionExist) {
        this.publishedCardsBySuccessionExist = publishedCardsBySuccessionExist;
    }

    public java.util.List<CardFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<CardFilter> and) {
        this.and = and;
    }

    public java.util.List<CardFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<CardFilter> or) {
        this.or = or;
    }

    public CardFilter getNot() {
        return not;
    }
    public void setNot(CardFilter not) {
        this.not = not;
    }



    public static CardFilter.Builder builder() {
        return new CardFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private StringFilter createdBy;
        private StringFilter uri;
        private JSONFilter blocklyWorkspace;
        private JSONFilter cardScript;
        private DatetimeFilter createdAt;
        private DatetimeFilter lastModified;
        private BooleanFilter isArchived;
        private BooleanFilter isPublished;
        private BigIntFilter succession;
        private BooleanFilter collectible;
        private StringFilter type;
        private IntFilter cost;
        private CardToManyPublishedCardFilter publishedCardsBySuccession;
        private Boolean publishedCardsBySuccessionExist;
        private java.util.List<CardFilter> and;
        private java.util.List<CardFilter> or;
        private CardFilter not;

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

        public Builder setUri(StringFilter uri) {
            this.uri = uri;
            return this;
        }

        public Builder setBlocklyWorkspace(JSONFilter blocklyWorkspace) {
            this.blocklyWorkspace = blocklyWorkspace;
            return this;
        }

        public Builder setCardScript(JSONFilter cardScript) {
            this.cardScript = cardScript;
            return this;
        }

        public Builder setCreatedAt(DatetimeFilter createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setLastModified(DatetimeFilter lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setIsArchived(BooleanFilter isArchived) {
            this.isArchived = isArchived;
            return this;
        }

        public Builder setIsPublished(BooleanFilter isPublished) {
            this.isPublished = isPublished;
            return this;
        }

        public Builder setSuccession(BigIntFilter succession) {
            this.succession = succession;
            return this;
        }

        public Builder setCollectible(BooleanFilter collectible) {
            this.collectible = collectible;
            return this;
        }

        public Builder setType(StringFilter type) {
            this.type = type;
            return this;
        }

        public Builder setCost(IntFilter cost) {
            this.cost = cost;
            return this;
        }

        public Builder setPublishedCardsBySuccession(CardToManyPublishedCardFilter publishedCardsBySuccession) {
            this.publishedCardsBySuccession = publishedCardsBySuccession;
            return this;
        }

        public Builder setPublishedCardsBySuccessionExist(Boolean publishedCardsBySuccessionExist) {
            this.publishedCardsBySuccessionExist = publishedCardsBySuccessionExist;
            return this;
        }

        public Builder setAnd(java.util.List<CardFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<CardFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(CardFilter not) {
            this.not = not;
            return this;
        }


        public CardFilter build() {
            return new CardFilter(id, createdBy, uri, blocklyWorkspace, cardScript, createdAt, lastModified, isArchived, isPublished, succession, collectible, type, cost, publishedCardsBySuccession, publishedCardsBySuccessionExist, and, or, not);
        }

    }
}
