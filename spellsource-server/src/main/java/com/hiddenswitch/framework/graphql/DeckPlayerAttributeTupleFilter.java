package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `DeckPlayerAttributeTuple` object types. All fields are combined with a logical ‘and.’
 */
public class DeckPlayerAttributeTupleFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private BigIntFilter id;
    private StringFilter deckId;
    private IntFilter attribute;
    private StringFilter stringValue;
    private DeckFilter deckByDeckId;
    private java.util.List<DeckPlayerAttributeTupleFilter> and;
    private java.util.List<DeckPlayerAttributeTupleFilter> or;
    private DeckPlayerAttributeTupleFilter not;

    public DeckPlayerAttributeTupleFilter() {
    }

    public DeckPlayerAttributeTupleFilter(BigIntFilter id, StringFilter deckId, IntFilter attribute, StringFilter stringValue, DeckFilter deckByDeckId, java.util.List<DeckPlayerAttributeTupleFilter> and, java.util.List<DeckPlayerAttributeTupleFilter> or, DeckPlayerAttributeTupleFilter not) {
        this.id = id;
        this.deckId = deckId;
        this.attribute = attribute;
        this.stringValue = stringValue;
        this.deckByDeckId = deckByDeckId;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public BigIntFilter getId() {
        return id;
    }
    public void setId(BigIntFilter id) {
        this.id = id;
    }

    public StringFilter getDeckId() {
        return deckId;
    }
    public void setDeckId(StringFilter deckId) {
        this.deckId = deckId;
    }

    public IntFilter getAttribute() {
        return attribute;
    }
    public void setAttribute(IntFilter attribute) {
        this.attribute = attribute;
    }

    public StringFilter getStringValue() {
        return stringValue;
    }
    public void setStringValue(StringFilter stringValue) {
        this.stringValue = stringValue;
    }

    public DeckFilter getDeckByDeckId() {
        return deckByDeckId;
    }
    public void setDeckByDeckId(DeckFilter deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    public java.util.List<DeckPlayerAttributeTupleFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<DeckPlayerAttributeTupleFilter> and) {
        this.and = and;
    }

    public java.util.List<DeckPlayerAttributeTupleFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<DeckPlayerAttributeTupleFilter> or) {
        this.or = or;
    }

    public DeckPlayerAttributeTupleFilter getNot() {
        return not;
    }
    public void setNot(DeckPlayerAttributeTupleFilter not) {
        this.not = not;
    }



    public static DeckPlayerAttributeTupleFilter.Builder builder() {
        return new DeckPlayerAttributeTupleFilter.Builder();
    }

    public static class Builder {

        private BigIntFilter id;
        private StringFilter deckId;
        private IntFilter attribute;
        private StringFilter stringValue;
        private DeckFilter deckByDeckId;
        private java.util.List<DeckPlayerAttributeTupleFilter> and;
        private java.util.List<DeckPlayerAttributeTupleFilter> or;
        private DeckPlayerAttributeTupleFilter not;

        public Builder() {
        }

        public Builder setId(BigIntFilter id) {
            this.id = id;
            return this;
        }

        public Builder setDeckId(StringFilter deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setAttribute(IntFilter attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setStringValue(StringFilter stringValue) {
            this.stringValue = stringValue;
            return this;
        }

        public Builder setDeckByDeckId(DeckFilter deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        public Builder setAnd(java.util.List<DeckPlayerAttributeTupleFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<DeckPlayerAttributeTupleFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(DeckPlayerAttributeTupleFilter not) {
            this.not = not;
            return this;
        }


        public DeckPlayerAttributeTupleFilter build() {
            return new DeckPlayerAttributeTupleFilter(id, deckId, attribute, stringValue, deckByDeckId, and, or, not);
        }

    }
}
