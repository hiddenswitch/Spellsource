package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `CardsInDeck` object types. All fields are combined with a logical ‘and.’
 */
public class CardsInDeckFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private BigIntFilter id;
    private StringFilter deckId;
    private StringFilter cardId;
    private PublishedCardFilter publishedCardByCardId;
    private DeckFilter deckByDeckId;
    private java.util.List<CardsInDeckFilter> and;
    private java.util.List<CardsInDeckFilter> or;
    private CardsInDeckFilter not;

    public CardsInDeckFilter() {
    }

    public CardsInDeckFilter(BigIntFilter id, StringFilter deckId, StringFilter cardId, PublishedCardFilter publishedCardByCardId, DeckFilter deckByDeckId, java.util.List<CardsInDeckFilter> and, java.util.List<CardsInDeckFilter> or, CardsInDeckFilter not) {
        this.id = id;
        this.deckId = deckId;
        this.cardId = cardId;
        this.publishedCardByCardId = publishedCardByCardId;
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

    public StringFilter getCardId() {
        return cardId;
    }
    public void setCardId(StringFilter cardId) {
        this.cardId = cardId;
    }

    public PublishedCardFilter getPublishedCardByCardId() {
        return publishedCardByCardId;
    }
    public void setPublishedCardByCardId(PublishedCardFilter publishedCardByCardId) {
        this.publishedCardByCardId = publishedCardByCardId;
    }

    public DeckFilter getDeckByDeckId() {
        return deckByDeckId;
    }
    public void setDeckByDeckId(DeckFilter deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    public java.util.List<CardsInDeckFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<CardsInDeckFilter> and) {
        this.and = and;
    }

    public java.util.List<CardsInDeckFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<CardsInDeckFilter> or) {
        this.or = or;
    }

    public CardsInDeckFilter getNot() {
        return not;
    }
    public void setNot(CardsInDeckFilter not) {
        this.not = not;
    }



    public static CardsInDeckFilter.Builder builder() {
        return new CardsInDeckFilter.Builder();
    }

    public static class Builder {

        private BigIntFilter id;
        private StringFilter deckId;
        private StringFilter cardId;
        private PublishedCardFilter publishedCardByCardId;
        private DeckFilter deckByDeckId;
        private java.util.List<CardsInDeckFilter> and;
        private java.util.List<CardsInDeckFilter> or;
        private CardsInDeckFilter not;

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

        public Builder setCardId(StringFilter cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setPublishedCardByCardId(PublishedCardFilter publishedCardByCardId) {
            this.publishedCardByCardId = publishedCardByCardId;
            return this;
        }

        public Builder setDeckByDeckId(DeckFilter deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        public Builder setAnd(java.util.List<CardsInDeckFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<CardsInDeckFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(CardsInDeckFilter not) {
            this.not = not;
            return this;
        }


        public CardsInDeckFilter build() {
            return new CardsInDeckFilter(id, deckId, cardId, publishedCardByCardId, deckByDeckId, and, or, not);
        }

    }
}
