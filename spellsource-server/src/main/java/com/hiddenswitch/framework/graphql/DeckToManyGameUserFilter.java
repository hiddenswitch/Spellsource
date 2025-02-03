package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against many `GameUser` object types. All fields are combined with a logical ‘and.’
 */
public class DeckToManyGameUserFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private GameUserFilter every;
    private GameUserFilter some;
    private GameUserFilter none;

    public DeckToManyGameUserFilter() {
    }

    public DeckToManyGameUserFilter(GameUserFilter every, GameUserFilter some, GameUserFilter none) {
        this.every = every;
        this.some = some;
        this.none = none;
    }

    public GameUserFilter getEvery() {
        return every;
    }
    public void setEvery(GameUserFilter every) {
        this.every = every;
    }

    public GameUserFilter getSome() {
        return some;
    }
    public void setSome(GameUserFilter some) {
        this.some = some;
    }

    public GameUserFilter getNone() {
        return none;
    }
    public void setNone(GameUserFilter none) {
        this.none = none;
    }



    public static DeckToManyGameUserFilter.Builder builder() {
        return new DeckToManyGameUserFilter.Builder();
    }

    public static class Builder {

        private GameUserFilter every;
        private GameUserFilter some;
        private GameUserFilter none;

        public Builder() {
        }

        public Builder setEvery(GameUserFilter every) {
            this.every = every;
            return this;
        }

        public Builder setSome(GameUserFilter some) {
            this.some = some;
            return this;
        }

        public Builder setNone(GameUserFilter none) {
            this.none = none;
            return this;
        }


        public DeckToManyGameUserFilter build() {
            return new DeckToManyGameUserFilter(every, some, none);
        }

    }
}
