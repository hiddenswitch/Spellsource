package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against many `DeckPlayerAttributeTuple` object types. All fields are combined with a logical ‘and.’
 */
public class DeckToManyDeckPlayerAttributeTupleFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private DeckPlayerAttributeTupleFilter every;
    private DeckPlayerAttributeTupleFilter some;
    private DeckPlayerAttributeTupleFilter none;

    public DeckToManyDeckPlayerAttributeTupleFilter() {
    }

    public DeckToManyDeckPlayerAttributeTupleFilter(DeckPlayerAttributeTupleFilter every, DeckPlayerAttributeTupleFilter some, DeckPlayerAttributeTupleFilter none) {
        this.every = every;
        this.some = some;
        this.none = none;
    }

    public DeckPlayerAttributeTupleFilter getEvery() {
        return every;
    }
    public void setEvery(DeckPlayerAttributeTupleFilter every) {
        this.every = every;
    }

    public DeckPlayerAttributeTupleFilter getSome() {
        return some;
    }
    public void setSome(DeckPlayerAttributeTupleFilter some) {
        this.some = some;
    }

    public DeckPlayerAttributeTupleFilter getNone() {
        return none;
    }
    public void setNone(DeckPlayerAttributeTupleFilter none) {
        this.none = none;
    }



    public static DeckToManyDeckPlayerAttributeTupleFilter.Builder builder() {
        return new DeckToManyDeckPlayerAttributeTupleFilter.Builder();
    }

    public static class Builder {

        private DeckPlayerAttributeTupleFilter every;
        private DeckPlayerAttributeTupleFilter some;
        private DeckPlayerAttributeTupleFilter none;

        public Builder() {
        }

        public Builder setEvery(DeckPlayerAttributeTupleFilter every) {
            this.every = every;
            return this;
        }

        public Builder setSome(DeckPlayerAttributeTupleFilter some) {
            this.some = some;
            return this;
        }

        public Builder setNone(DeckPlayerAttributeTupleFilter none) {
            this.none = none;
            return this;
        }


        public DeckToManyDeckPlayerAttributeTupleFilter build() {
            return new DeckToManyDeckPlayerAttributeTupleFilter(every, some, none);
        }

    }
}
