package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `HardRemovalCard` object types. All fields are combined with a logical ‘and.’
 */
public class HardRemovalCardFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter cardId;
    private java.util.List<HardRemovalCardFilter> and;
    private java.util.List<HardRemovalCardFilter> or;
    private HardRemovalCardFilter not;

    public HardRemovalCardFilter() {
    }

    public HardRemovalCardFilter(StringFilter cardId, java.util.List<HardRemovalCardFilter> and, java.util.List<HardRemovalCardFilter> or, HardRemovalCardFilter not) {
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

    public java.util.List<HardRemovalCardFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<HardRemovalCardFilter> and) {
        this.and = and;
    }

    public java.util.List<HardRemovalCardFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<HardRemovalCardFilter> or) {
        this.or = or;
    }

    public HardRemovalCardFilter getNot() {
        return not;
    }
    public void setNot(HardRemovalCardFilter not) {
        this.not = not;
    }



    public static HardRemovalCardFilter.Builder builder() {
        return new HardRemovalCardFilter.Builder();
    }

    public static class Builder {

        private StringFilter cardId;
        private java.util.List<HardRemovalCardFilter> and;
        private java.util.List<HardRemovalCardFilter> or;
        private HardRemovalCardFilter not;

        public Builder() {
        }

        public Builder setCardId(StringFilter cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setAnd(java.util.List<HardRemovalCardFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<HardRemovalCardFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(HardRemovalCardFilter not) {
            this.not = not;
            return this;
        }


        public HardRemovalCardFilter build() {
            return new HardRemovalCardFilter(cardId, and, or, not);
        }

    }
}
