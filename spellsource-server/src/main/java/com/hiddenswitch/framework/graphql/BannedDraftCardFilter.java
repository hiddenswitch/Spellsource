package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `BannedDraftCard` object types. All fields are combined with a logical ‘and.’
 */
public class BannedDraftCardFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter cardId;
    private java.util.List<BannedDraftCardFilter> and;
    private java.util.List<BannedDraftCardFilter> or;
    private BannedDraftCardFilter not;

    public BannedDraftCardFilter() {
    }

    public BannedDraftCardFilter(StringFilter cardId, java.util.List<BannedDraftCardFilter> and, java.util.List<BannedDraftCardFilter> or, BannedDraftCardFilter not) {
        this.cardId = cardId;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getCardId() {
        return cardId;
    }
    public void setCardId(StringFilter cardId) {
        this.cardId = cardId;
    }

    public java.util.List<BannedDraftCardFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<BannedDraftCardFilter> and) {
        this.and = and;
    }

    public java.util.List<BannedDraftCardFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<BannedDraftCardFilter> or) {
        this.or = or;
    }

    public BannedDraftCardFilter getNot() {
        return not;
    }
    public void setNot(BannedDraftCardFilter not) {
        this.not = not;
    }



    public static BannedDraftCardFilter.Builder builder() {
        return new BannedDraftCardFilter.Builder();
    }

    public static class Builder {

        private StringFilter cardId;
        private java.util.List<BannedDraftCardFilter> and;
        private java.util.List<BannedDraftCardFilter> or;
        private BannedDraftCardFilter not;

        public Builder() {
        }

        public Builder setCardId(StringFilter cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setAnd(java.util.List<BannedDraftCardFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<BannedDraftCardFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(BannedDraftCardFilter not) {
            this.not = not;
            return this;
        }


        public BannedDraftCardFilter build() {
            return new BannedDraftCardFilter(cardId, and, or, not);
        }

    }
}
