package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against many `PublishedCard` object types. All fields are combined with a logical ‘and.’
 */
public class CardToManyPublishedCardFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private PublishedCardFilter every;
    private PublishedCardFilter some;
    private PublishedCardFilter none;

    public CardToManyPublishedCardFilter() {
    }

    public CardToManyPublishedCardFilter(PublishedCardFilter every, PublishedCardFilter some, PublishedCardFilter none) {
        this.every = every;
        this.some = some;
        this.none = none;
    }

    public PublishedCardFilter getEvery() {
        return every;
    }
    public void setEvery(PublishedCardFilter every) {
        this.every = every;
    }

    public PublishedCardFilter getSome() {
        return some;
    }
    public void setSome(PublishedCardFilter some) {
        this.some = some;
    }

    public PublishedCardFilter getNone() {
        return none;
    }
    public void setNone(PublishedCardFilter none) {
        this.none = none;
    }



    public static CardToManyPublishedCardFilter.Builder builder() {
        return new CardToManyPublishedCardFilter.Builder();
    }

    public static class Builder {

        private PublishedCardFilter every;
        private PublishedCardFilter some;
        private PublishedCardFilter none;

        public Builder() {
        }

        public Builder setEvery(PublishedCardFilter every) {
            this.every = every;
            return this;
        }

        public Builder setSome(PublishedCardFilter some) {
            this.some = some;
            return this;
        }

        public Builder setNone(PublishedCardFilter none) {
            this.none = none;
            return this;
        }


        public CardToManyPublishedCardFilter build() {
            return new CardToManyPublishedCardFilter(every, some, none);
        }

    }
}
