package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `DeckShare` object types. All fields are combined with a logical ‘and.’
 */
public class DeckShareFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter deckId;
    private StringFilter shareRecipientId;
    private BooleanFilter trashedByRecipient;
    private DeckFilter deckByDeckId;
    private java.util.List<DeckShareFilter> and;
    private java.util.List<DeckShareFilter> or;
    private DeckShareFilter not;

    public DeckShareFilter() {
    }

    public DeckShareFilter(StringFilter deckId, StringFilter shareRecipientId, BooleanFilter trashedByRecipient, DeckFilter deckByDeckId, java.util.List<DeckShareFilter> and, java.util.List<DeckShareFilter> or, DeckShareFilter not) {
        this.deckId = deckId;
        this.shareRecipientId = shareRecipientId;
        this.trashedByRecipient = trashedByRecipient;
        this.deckByDeckId = deckByDeckId;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getDeckId() {
        return deckId;
    }
    public void setDeckId(StringFilter deckId) {
        this.deckId = deckId;
    }

    public StringFilter getShareRecipientId() {
        return shareRecipientId;
    }
    public void setShareRecipientId(StringFilter shareRecipientId) {
        this.shareRecipientId = shareRecipientId;
    }

    public BooleanFilter getTrashedByRecipient() {
        return trashedByRecipient;
    }
    public void setTrashedByRecipient(BooleanFilter trashedByRecipient) {
        this.trashedByRecipient = trashedByRecipient;
    }

    public DeckFilter getDeckByDeckId() {
        return deckByDeckId;
    }
    public void setDeckByDeckId(DeckFilter deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    public java.util.List<DeckShareFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<DeckShareFilter> and) {
        this.and = and;
    }

    public java.util.List<DeckShareFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<DeckShareFilter> or) {
        this.or = or;
    }

    public DeckShareFilter getNot() {
        return not;
    }
    public void setNot(DeckShareFilter not) {
        this.not = not;
    }



    public static DeckShareFilter.Builder builder() {
        return new DeckShareFilter.Builder();
    }

    public static class Builder {

        private StringFilter deckId;
        private StringFilter shareRecipientId;
        private BooleanFilter trashedByRecipient;
        private DeckFilter deckByDeckId;
        private java.util.List<DeckShareFilter> and;
        private java.util.List<DeckShareFilter> or;
        private DeckShareFilter not;

        public Builder() {
        }

        public Builder setDeckId(StringFilter deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setShareRecipientId(StringFilter shareRecipientId) {
            this.shareRecipientId = shareRecipientId;
            return this;
        }

        public Builder setTrashedByRecipient(BooleanFilter trashedByRecipient) {
            this.trashedByRecipient = trashedByRecipient;
            return this;
        }

        public Builder setDeckByDeckId(DeckFilter deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        public Builder setAnd(java.util.List<DeckShareFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<DeckShareFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(DeckShareFilter not) {
            this.not = not;
            return this;
        }


        public DeckShareFilter build() {
            return new DeckShareFilter(deckId, shareRecipientId, trashedByRecipient, deckByDeckId, and, or, not);
        }

    }
}
