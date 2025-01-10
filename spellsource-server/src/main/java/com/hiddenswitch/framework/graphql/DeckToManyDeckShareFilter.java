package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against many `DeckShare` object types. All fields are combined with a logical ‘and.’
 */
public class DeckToManyDeckShareFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private DeckShareFilter every;
    private DeckShareFilter some;
    private DeckShareFilter none;

    public DeckToManyDeckShareFilter() {
    }

    public DeckToManyDeckShareFilter(DeckShareFilter every, DeckShareFilter some, DeckShareFilter none) {
        this.every = every;
        this.some = some;
        this.none = none;
    }

    public DeckShareFilter getEvery() {
        return every;
    }
    public void setEvery(DeckShareFilter every) {
        this.every = every;
    }

    public DeckShareFilter getSome() {
        return some;
    }
    public void setSome(DeckShareFilter some) {
        this.some = some;
    }

    public DeckShareFilter getNone() {
        return none;
    }
    public void setNone(DeckShareFilter none) {
        this.none = none;
    }



    public static DeckToManyDeckShareFilter.Builder builder() {
        return new DeckToManyDeckShareFilter.Builder();
    }

    public static class Builder {

        private DeckShareFilter every;
        private DeckShareFilter some;
        private DeckShareFilter none;

        public Builder() {
        }

        public Builder setEvery(DeckShareFilter every) {
            this.every = every;
            return this;
        }

        public Builder setSome(DeckShareFilter some) {
            this.some = some;
            return this;
        }

        public Builder setNone(DeckShareFilter none) {
            this.none = none;
            return this;
        }


        public DeckToManyDeckShareFilter build() {
            return new DeckToManyDeckShareFilter(every, some, none);
        }

    }
}
