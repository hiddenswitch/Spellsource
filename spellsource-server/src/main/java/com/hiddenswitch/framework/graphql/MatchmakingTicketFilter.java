package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `MatchmakingTicket` object types. All fields are combined with a logical ‘and.’
 */
public class MatchmakingTicketFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private BigIntFilter ticketId;
    private StringFilter queueId;
    private StringFilter userId;
    private StringFilter deckId;
    private StringFilter botDeckId;
    private DatetimeFilter createdAt;
    private DeckFilter deckByBotDeckId;
    private Boolean deckByBotDeckIdExists;
    private DeckFilter deckByDeckId;
    private Boolean deckByDeckIdExists;
    private MatchmakingQueueFilter matchmakingQueueByQueueId;
    private Boolean matchmakingQueueByQueueIdExists;
    private java.util.List<MatchmakingTicketFilter> and;
    private java.util.List<MatchmakingTicketFilter> or;
    private MatchmakingTicketFilter not;

    public MatchmakingTicketFilter() {
    }

    public MatchmakingTicketFilter(BigIntFilter ticketId, StringFilter queueId, StringFilter userId, StringFilter deckId, StringFilter botDeckId, DatetimeFilter createdAt, DeckFilter deckByBotDeckId, Boolean deckByBotDeckIdExists, DeckFilter deckByDeckId, Boolean deckByDeckIdExists, MatchmakingQueueFilter matchmakingQueueByQueueId, Boolean matchmakingQueueByQueueIdExists, java.util.List<MatchmakingTicketFilter> and, java.util.List<MatchmakingTicketFilter> or, MatchmakingTicketFilter not) {
        this.ticketId = ticketId;
        this.queueId = queueId;
        this.userId = userId;
        this.deckId = deckId;
        this.botDeckId = botDeckId;
        this.createdAt = createdAt;
        this.deckByBotDeckId = deckByBotDeckId;
        this.deckByBotDeckIdExists = deckByBotDeckIdExists;
        this.deckByDeckId = deckByDeckId;
        this.deckByDeckIdExists = deckByDeckIdExists;
        this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
        this.matchmakingQueueByQueueIdExists = matchmakingQueueByQueueIdExists;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public BigIntFilter getTicketId() {
        return ticketId;
    }
    public void setTicketId(BigIntFilter ticketId) {
        this.ticketId = ticketId;
    }

    public StringFilter getQueueId() {
        return queueId;
    }
    public void setQueueId(StringFilter queueId) {
        this.queueId = queueId;
    }

    public StringFilter getUserId() {
        return userId;
    }
    public void setUserId(StringFilter userId) {
        this.userId = userId;
    }

    public StringFilter getDeckId() {
        return deckId;
    }
    public void setDeckId(StringFilter deckId) {
        this.deckId = deckId;
    }

    public StringFilter getBotDeckId() {
        return botDeckId;
    }
    public void setBotDeckId(StringFilter botDeckId) {
        this.botDeckId = botDeckId;
    }

    public DatetimeFilter getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(DatetimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public DeckFilter getDeckByBotDeckId() {
        return deckByBotDeckId;
    }
    public void setDeckByBotDeckId(DeckFilter deckByBotDeckId) {
        this.deckByBotDeckId = deckByBotDeckId;
    }

    public Boolean getDeckByBotDeckIdExists() {
        return deckByBotDeckIdExists;
    }
    public void setDeckByBotDeckIdExists(Boolean deckByBotDeckIdExists) {
        this.deckByBotDeckIdExists = deckByBotDeckIdExists;
    }

    public DeckFilter getDeckByDeckId() {
        return deckByDeckId;
    }
    public void setDeckByDeckId(DeckFilter deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    public Boolean getDeckByDeckIdExists() {
        return deckByDeckIdExists;
    }
    public void setDeckByDeckIdExists(Boolean deckByDeckIdExists) {
        this.deckByDeckIdExists = deckByDeckIdExists;
    }

    public MatchmakingQueueFilter getMatchmakingQueueByQueueId() {
        return matchmakingQueueByQueueId;
    }
    public void setMatchmakingQueueByQueueId(MatchmakingQueueFilter matchmakingQueueByQueueId) {
        this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
    }

    public Boolean getMatchmakingQueueByQueueIdExists() {
        return matchmakingQueueByQueueIdExists;
    }
    public void setMatchmakingQueueByQueueIdExists(Boolean matchmakingQueueByQueueIdExists) {
        this.matchmakingQueueByQueueIdExists = matchmakingQueueByQueueIdExists;
    }

    public java.util.List<MatchmakingTicketFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<MatchmakingTicketFilter> and) {
        this.and = and;
    }

    public java.util.List<MatchmakingTicketFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<MatchmakingTicketFilter> or) {
        this.or = or;
    }

    public MatchmakingTicketFilter getNot() {
        return not;
    }
    public void setNot(MatchmakingTicketFilter not) {
        this.not = not;
    }



    public static MatchmakingTicketFilter.Builder builder() {
        return new MatchmakingTicketFilter.Builder();
    }

    public static class Builder {

        private BigIntFilter ticketId;
        private StringFilter queueId;
        private StringFilter userId;
        private StringFilter deckId;
        private StringFilter botDeckId;
        private DatetimeFilter createdAt;
        private DeckFilter deckByBotDeckId;
        private Boolean deckByBotDeckIdExists;
        private DeckFilter deckByDeckId;
        private Boolean deckByDeckIdExists;
        private MatchmakingQueueFilter matchmakingQueueByQueueId;
        private Boolean matchmakingQueueByQueueIdExists;
        private java.util.List<MatchmakingTicketFilter> and;
        private java.util.List<MatchmakingTicketFilter> or;
        private MatchmakingTicketFilter not;

        public Builder() {
        }

        public Builder setTicketId(BigIntFilter ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public Builder setQueueId(StringFilter queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder setUserId(StringFilter userId) {
            this.userId = userId;
            return this;
        }

        public Builder setDeckId(StringFilter deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setBotDeckId(StringFilter botDeckId) {
            this.botDeckId = botDeckId;
            return this;
        }

        public Builder setCreatedAt(DatetimeFilter createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setDeckByBotDeckId(DeckFilter deckByBotDeckId) {
            this.deckByBotDeckId = deckByBotDeckId;
            return this;
        }

        public Builder setDeckByBotDeckIdExists(Boolean deckByBotDeckIdExists) {
            this.deckByBotDeckIdExists = deckByBotDeckIdExists;
            return this;
        }

        public Builder setDeckByDeckId(DeckFilter deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        public Builder setDeckByDeckIdExists(Boolean deckByDeckIdExists) {
            this.deckByDeckIdExists = deckByDeckIdExists;
            return this;
        }

        public Builder setMatchmakingQueueByQueueId(MatchmakingQueueFilter matchmakingQueueByQueueId) {
            this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
            return this;
        }

        public Builder setMatchmakingQueueByQueueIdExists(Boolean matchmakingQueueByQueueIdExists) {
            this.matchmakingQueueByQueueIdExists = matchmakingQueueByQueueIdExists;
            return this;
        }

        public Builder setAnd(java.util.List<MatchmakingTicketFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<MatchmakingTicketFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(MatchmakingTicketFilter not) {
            this.not = not;
            return this;
        }


        public MatchmakingTicketFilter build() {
            return new MatchmakingTicketFilter(ticketId, queueId, userId, deckId, botDeckId, createdAt, deckByBotDeckId, deckByBotDeckIdExists, deckByDeckId, deckByDeckIdExists, matchmakingQueueByQueueId, matchmakingQueueByQueueIdExists, and, or, not);
        }

    }
}
