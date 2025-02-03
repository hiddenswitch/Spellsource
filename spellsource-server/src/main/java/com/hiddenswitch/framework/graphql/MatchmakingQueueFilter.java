package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `MatchmakingQueue` object types. All fields are combined with a logical ‘and.’
 */
public class MatchmakingQueueFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private StringFilter name;
    private BooleanFilter botOpponent;
    private BooleanFilter privateLobby;
    private BooleanFilter startsAutomatically;
    private BigIntFilter stillConnectedTimeout;
    private BigIntFilter emptyLobbyTimeout;
    private BigIntFilter awaitingLobbyTimeout;
    private BooleanFilter once;
    private BooleanFilter automaticallyClose;
    private IntFilter lobbySize;
    private DatetimeFilter queueCreatedAt;
    private MatchmakingQueueToManyMatchmakingTicketFilter matchmakingTicketsByQueueId;
    private Boolean matchmakingTicketsByQueueIdExist;
    private java.util.List<MatchmakingQueueFilter> and;
    private java.util.List<MatchmakingQueueFilter> or;
    private MatchmakingQueueFilter not;

    public MatchmakingQueueFilter() {
    }

    public MatchmakingQueueFilter(StringFilter id, StringFilter name, BooleanFilter botOpponent, BooleanFilter privateLobby, BooleanFilter startsAutomatically, BigIntFilter stillConnectedTimeout, BigIntFilter emptyLobbyTimeout, BigIntFilter awaitingLobbyTimeout, BooleanFilter once, BooleanFilter automaticallyClose, IntFilter lobbySize, DatetimeFilter queueCreatedAt, MatchmakingQueueToManyMatchmakingTicketFilter matchmakingTicketsByQueueId, Boolean matchmakingTicketsByQueueIdExist, java.util.List<MatchmakingQueueFilter> and, java.util.List<MatchmakingQueueFilter> or, MatchmakingQueueFilter not) {
        this.id = id;
        this.name = name;
        this.botOpponent = botOpponent;
        this.privateLobby = privateLobby;
        this.startsAutomatically = startsAutomatically;
        this.stillConnectedTimeout = stillConnectedTimeout;
        this.emptyLobbyTimeout = emptyLobbyTimeout;
        this.awaitingLobbyTimeout = awaitingLobbyTimeout;
        this.once = once;
        this.automaticallyClose = automaticallyClose;
        this.lobbySize = lobbySize;
        this.queueCreatedAt = queueCreatedAt;
        this.matchmakingTicketsByQueueId = matchmakingTicketsByQueueId;
        this.matchmakingTicketsByQueueIdExist = matchmakingTicketsByQueueIdExist;
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

    public StringFilter getName() {
        return name;
    }
    public void setName(StringFilter name) {
        this.name = name;
    }

    public BooleanFilter getBotOpponent() {
        return botOpponent;
    }
    public void setBotOpponent(BooleanFilter botOpponent) {
        this.botOpponent = botOpponent;
    }

    public BooleanFilter getPrivateLobby() {
        return privateLobby;
    }
    public void setPrivateLobby(BooleanFilter privateLobby) {
        this.privateLobby = privateLobby;
    }

    public BooleanFilter getStartsAutomatically() {
        return startsAutomatically;
    }
    public void setStartsAutomatically(BooleanFilter startsAutomatically) {
        this.startsAutomatically = startsAutomatically;
    }

    public BigIntFilter getStillConnectedTimeout() {
        return stillConnectedTimeout;
    }
    public void setStillConnectedTimeout(BigIntFilter stillConnectedTimeout) {
        this.stillConnectedTimeout = stillConnectedTimeout;
    }

    public BigIntFilter getEmptyLobbyTimeout() {
        return emptyLobbyTimeout;
    }
    public void setEmptyLobbyTimeout(BigIntFilter emptyLobbyTimeout) {
        this.emptyLobbyTimeout = emptyLobbyTimeout;
    }

    public BigIntFilter getAwaitingLobbyTimeout() {
        return awaitingLobbyTimeout;
    }
    public void setAwaitingLobbyTimeout(BigIntFilter awaitingLobbyTimeout) {
        this.awaitingLobbyTimeout = awaitingLobbyTimeout;
    }

    public BooleanFilter getOnce() {
        return once;
    }
    public void setOnce(BooleanFilter once) {
        this.once = once;
    }

    public BooleanFilter getAutomaticallyClose() {
        return automaticallyClose;
    }
    public void setAutomaticallyClose(BooleanFilter automaticallyClose) {
        this.automaticallyClose = automaticallyClose;
    }

    public IntFilter getLobbySize() {
        return lobbySize;
    }
    public void setLobbySize(IntFilter lobbySize) {
        this.lobbySize = lobbySize;
    }

    public DatetimeFilter getQueueCreatedAt() {
        return queueCreatedAt;
    }
    public void setQueueCreatedAt(DatetimeFilter queueCreatedAt) {
        this.queueCreatedAt = queueCreatedAt;
    }

    public MatchmakingQueueToManyMatchmakingTicketFilter getMatchmakingTicketsByQueueId() {
        return matchmakingTicketsByQueueId;
    }
    public void setMatchmakingTicketsByQueueId(MatchmakingQueueToManyMatchmakingTicketFilter matchmakingTicketsByQueueId) {
        this.matchmakingTicketsByQueueId = matchmakingTicketsByQueueId;
    }

    public Boolean getMatchmakingTicketsByQueueIdExist() {
        return matchmakingTicketsByQueueIdExist;
    }
    public void setMatchmakingTicketsByQueueIdExist(Boolean matchmakingTicketsByQueueIdExist) {
        this.matchmakingTicketsByQueueIdExist = matchmakingTicketsByQueueIdExist;
    }

    public java.util.List<MatchmakingQueueFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<MatchmakingQueueFilter> and) {
        this.and = and;
    }

    public java.util.List<MatchmakingQueueFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<MatchmakingQueueFilter> or) {
        this.or = or;
    }

    public MatchmakingQueueFilter getNot() {
        return not;
    }
    public void setNot(MatchmakingQueueFilter not) {
        this.not = not;
    }



    public static MatchmakingQueueFilter.Builder builder() {
        return new MatchmakingQueueFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private StringFilter name;
        private BooleanFilter botOpponent;
        private BooleanFilter privateLobby;
        private BooleanFilter startsAutomatically;
        private BigIntFilter stillConnectedTimeout;
        private BigIntFilter emptyLobbyTimeout;
        private BigIntFilter awaitingLobbyTimeout;
        private BooleanFilter once;
        private BooleanFilter automaticallyClose;
        private IntFilter lobbySize;
        private DatetimeFilter queueCreatedAt;
        private MatchmakingQueueToManyMatchmakingTicketFilter matchmakingTicketsByQueueId;
        private Boolean matchmakingTicketsByQueueIdExist;
        private java.util.List<MatchmakingQueueFilter> and;
        private java.util.List<MatchmakingQueueFilter> or;
        private MatchmakingQueueFilter not;

        public Builder() {
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setName(StringFilter name) {
            this.name = name;
            return this;
        }

        public Builder setBotOpponent(BooleanFilter botOpponent) {
            this.botOpponent = botOpponent;
            return this;
        }

        public Builder setPrivateLobby(BooleanFilter privateLobby) {
            this.privateLobby = privateLobby;
            return this;
        }

        public Builder setStartsAutomatically(BooleanFilter startsAutomatically) {
            this.startsAutomatically = startsAutomatically;
            return this;
        }

        public Builder setStillConnectedTimeout(BigIntFilter stillConnectedTimeout) {
            this.stillConnectedTimeout = stillConnectedTimeout;
            return this;
        }

        public Builder setEmptyLobbyTimeout(BigIntFilter emptyLobbyTimeout) {
            this.emptyLobbyTimeout = emptyLobbyTimeout;
            return this;
        }

        public Builder setAwaitingLobbyTimeout(BigIntFilter awaitingLobbyTimeout) {
            this.awaitingLobbyTimeout = awaitingLobbyTimeout;
            return this;
        }

        public Builder setOnce(BooleanFilter once) {
            this.once = once;
            return this;
        }

        public Builder setAutomaticallyClose(BooleanFilter automaticallyClose) {
            this.automaticallyClose = automaticallyClose;
            return this;
        }

        public Builder setLobbySize(IntFilter lobbySize) {
            this.lobbySize = lobbySize;
            return this;
        }

        public Builder setQueueCreatedAt(DatetimeFilter queueCreatedAt) {
            this.queueCreatedAt = queueCreatedAt;
            return this;
        }

        public Builder setMatchmakingTicketsByQueueId(MatchmakingQueueToManyMatchmakingTicketFilter matchmakingTicketsByQueueId) {
            this.matchmakingTicketsByQueueId = matchmakingTicketsByQueueId;
            return this;
        }

        public Builder setMatchmakingTicketsByQueueIdExist(Boolean matchmakingTicketsByQueueIdExist) {
            this.matchmakingTicketsByQueueIdExist = matchmakingTicketsByQueueIdExist;
            return this;
        }

        public Builder setAnd(java.util.List<MatchmakingQueueFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<MatchmakingQueueFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(MatchmakingQueueFilter not) {
            this.not = not;
            return this;
        }


        public MatchmakingQueueFilter build() {
            return new MatchmakingQueueFilter(id, name, botOpponent, privateLobby, startsAutomatically, stillConnectedTimeout, emptyLobbyTimeout, awaitingLobbyTimeout, once, automaticallyClose, lobbySize, queueCreatedAt, matchmakingTicketsByQueueId, matchmakingTicketsByQueueIdExist, and, or, not);
        }

    }
}
