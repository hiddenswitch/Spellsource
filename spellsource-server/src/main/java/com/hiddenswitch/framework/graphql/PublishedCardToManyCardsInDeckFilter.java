package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against many `CardsInDeck` object types. All fields are combined with a logical ‘and.’
 */
public class PublishedCardToManyCardsInDeckFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private CardsInDeckFilter every;
    private CardsInDeckFilter some;
    private CardsInDeckFilter none;

    public PublishedCardToManyCardsInDeckFilter() {
    }

    public PublishedCardToManyCardsInDeckFilter(CardsInDeckFilter every, CardsInDeckFilter some, CardsInDeckFilter none) {
        this.every = every;
        this.some = some;
        this.none = none;
    }

    public CardsInDeckFilter getEvery() {
        return every;
    }
    public void setEvery(CardsInDeckFilter every) {
        this.every = every;
    }

    public CardsInDeckFilter getSome() {
        return some;
    }
    public void setSome(CardsInDeckFilter some) {
        this.some = some;
    }

    public CardsInDeckFilter getNone() {
        return none;
    }
    public void setNone(CardsInDeckFilter none) {
        this.none = none;
    }



    public static PublishedCardToManyCardsInDeckFilter.Builder builder() {
        return new PublishedCardToManyCardsInDeckFilter.Builder();
    }

    public static class Builder {

        private CardsInDeckFilter every;
        private CardsInDeckFilter some;
        private CardsInDeckFilter none;

        public Builder() {
        }

        public Builder setEvery(CardsInDeckFilter every) {
            this.every = every;
            return this;
        }

        public Builder setSome(CardsInDeckFilter some) {
            this.some = some;
            return this;
        }

        public Builder setNone(CardsInDeckFilter none) {
            this.none = none;
            return this;
        }


        public PublishedCardToManyCardsInDeckFilter build() {
            return new PublishedCardToManyCardsInDeckFilter(every, some, none);
        }

    }
}
