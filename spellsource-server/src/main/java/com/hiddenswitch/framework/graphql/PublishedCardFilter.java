package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `PublishedCard` object types. All fields are combined with a logical ‘and.’
 */
public class PublishedCardFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private BigIntFilter succession;
    private PublishedCardToManyCardsInDeckFilter cardsInDecksByCardId;
    private Boolean cardsInDecksByCardIdExist;
    private CardFilter cardBySuccession;
    private java.util.List<PublishedCardFilter> and;
    private java.util.List<PublishedCardFilter> or;
    private PublishedCardFilter not;

    public PublishedCardFilter() {
    }

    public PublishedCardFilter(StringFilter id, BigIntFilter succession, PublishedCardToManyCardsInDeckFilter cardsInDecksByCardId, Boolean cardsInDecksByCardIdExist, CardFilter cardBySuccession, java.util.List<PublishedCardFilter> and, java.util.List<PublishedCardFilter> or, PublishedCardFilter not) {
        this.id = id;
        this.succession = succession;
        this.cardsInDecksByCardId = cardsInDecksByCardId;
        this.cardsInDecksByCardIdExist = cardsInDecksByCardIdExist;
        this.cardBySuccession = cardBySuccession;
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

    public BigIntFilter getSuccession() {
        return succession;
    }
    public void setSuccession(BigIntFilter succession) {
        this.succession = succession;
    }

    public PublishedCardToManyCardsInDeckFilter getCardsInDecksByCardId() {
        return cardsInDecksByCardId;
    }
    public void setCardsInDecksByCardId(PublishedCardToManyCardsInDeckFilter cardsInDecksByCardId) {
        this.cardsInDecksByCardId = cardsInDecksByCardId;
    }

    public Boolean getCardsInDecksByCardIdExist() {
        return cardsInDecksByCardIdExist;
    }
    public void setCardsInDecksByCardIdExist(Boolean cardsInDecksByCardIdExist) {
        this.cardsInDecksByCardIdExist = cardsInDecksByCardIdExist;
    }

    public CardFilter getCardBySuccession() {
        return cardBySuccession;
    }
    public void setCardBySuccession(CardFilter cardBySuccession) {
        this.cardBySuccession = cardBySuccession;
    }

    public java.util.List<PublishedCardFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<PublishedCardFilter> and) {
        this.and = and;
    }

    public java.util.List<PublishedCardFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<PublishedCardFilter> or) {
        this.or = or;
    }

    public PublishedCardFilter getNot() {
        return not;
    }
    public void setNot(PublishedCardFilter not) {
        this.not = not;
    }



    public static PublishedCardFilter.Builder builder() {
        return new PublishedCardFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private BigIntFilter succession;
        private PublishedCardToManyCardsInDeckFilter cardsInDecksByCardId;
        private Boolean cardsInDecksByCardIdExist;
        private CardFilter cardBySuccession;
        private java.util.List<PublishedCardFilter> and;
        private java.util.List<PublishedCardFilter> or;
        private PublishedCardFilter not;

        public Builder() {
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setSuccession(BigIntFilter succession) {
            this.succession = succession;
            return this;
        }

        public Builder setCardsInDecksByCardId(PublishedCardToManyCardsInDeckFilter cardsInDecksByCardId) {
            this.cardsInDecksByCardId = cardsInDecksByCardId;
            return this;
        }

        public Builder setCardsInDecksByCardIdExist(Boolean cardsInDecksByCardIdExist) {
            this.cardsInDecksByCardIdExist = cardsInDecksByCardIdExist;
            return this;
        }

        public Builder setCardBySuccession(CardFilter cardBySuccession) {
            this.cardBySuccession = cardBySuccession;
            return this;
        }

        public Builder setAnd(java.util.List<PublishedCardFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<PublishedCardFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(PublishedCardFilter not) {
            this.not = not;
            return this;
        }


        public PublishedCardFilter build() {
            return new PublishedCardFilter(id, succession, cardsInDecksByCardId, cardsInDecksByCardIdExist, cardBySuccession, and, or, not);
        }

    }
}
