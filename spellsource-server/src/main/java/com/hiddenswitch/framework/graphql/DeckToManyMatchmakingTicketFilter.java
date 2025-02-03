package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against many `MatchmakingTicket` object types. All fields are combined with a logical ‘and.’
 */
public class DeckToManyMatchmakingTicketFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private MatchmakingTicketFilter every;
    private MatchmakingTicketFilter some;
    private MatchmakingTicketFilter none;

    public DeckToManyMatchmakingTicketFilter() {
    }

    public DeckToManyMatchmakingTicketFilter(MatchmakingTicketFilter every, MatchmakingTicketFilter some, MatchmakingTicketFilter none) {
        this.every = every;
        this.some = some;
        this.none = none;
    }

    public MatchmakingTicketFilter getEvery() {
        return every;
    }
    public void setEvery(MatchmakingTicketFilter every) {
        this.every = every;
    }

    public MatchmakingTicketFilter getSome() {
        return some;
    }
    public void setSome(MatchmakingTicketFilter some) {
        this.some = some;
    }

    public MatchmakingTicketFilter getNone() {
        return none;
    }
    public void setNone(MatchmakingTicketFilter none) {
        this.none = none;
    }



    public static DeckToManyMatchmakingTicketFilter.Builder builder() {
        return new DeckToManyMatchmakingTicketFilter.Builder();
    }

    public static class Builder {

        private MatchmakingTicketFilter every;
        private MatchmakingTicketFilter some;
        private MatchmakingTicketFilter none;

        public Builder() {
        }

        public Builder setEvery(MatchmakingTicketFilter every) {
            this.every = every;
            return this;
        }

        public Builder setSome(MatchmakingTicketFilter some) {
            this.some = some;
            return this;
        }

        public Builder setNone(MatchmakingTicketFilter none) {
            this.none = none;
            return this;
        }


        public DeckToManyMatchmakingTicketFilter build() {
            return new DeckToManyMatchmakingTicketFilter(every, some, none);
        }

    }
}
