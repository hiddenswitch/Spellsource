package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `Game` object types. All fields are combined with a logical ‘and.’
 */
public class GameFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private BigIntFilter id;
    private GameStateEnumFilter status;
    private StringFilter gitHash;
    private JSONFilter trace;
    private DatetimeFilter createdAt;
    private GameToManyGameUserFilter gameUsersByGameId;
    private Boolean gameUsersByGameIdExist;
    private java.util.List<GameFilter> and;
    private java.util.List<GameFilter> or;
    private GameFilter not;

    public GameFilter() {
    }

    public GameFilter(BigIntFilter id, GameStateEnumFilter status, StringFilter gitHash, JSONFilter trace, DatetimeFilter createdAt, GameToManyGameUserFilter gameUsersByGameId, Boolean gameUsersByGameIdExist, java.util.List<GameFilter> and, java.util.List<GameFilter> or, GameFilter not) {
        this.id = id;
        this.status = status;
        this.gitHash = gitHash;
        this.trace = trace;
        this.createdAt = createdAt;
        this.gameUsersByGameId = gameUsersByGameId;
        this.gameUsersByGameIdExist = gameUsersByGameIdExist;
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

    public GameStateEnumFilter getStatus() {
        return status;
    }
    public void setStatus(GameStateEnumFilter status) {
        this.status = status;
    }

    public StringFilter getGitHash() {
        return gitHash;
    }
    public void setGitHash(StringFilter gitHash) {
        this.gitHash = gitHash;
    }

    public JSONFilter getTrace() {
        return trace;
    }
    public void setTrace(JSONFilter trace) {
        this.trace = trace;
    }

    public DatetimeFilter getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(DatetimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public GameToManyGameUserFilter getGameUsersByGameId() {
        return gameUsersByGameId;
    }
    public void setGameUsersByGameId(GameToManyGameUserFilter gameUsersByGameId) {
        this.gameUsersByGameId = gameUsersByGameId;
    }

    public Boolean getGameUsersByGameIdExist() {
        return gameUsersByGameIdExist;
    }
    public void setGameUsersByGameIdExist(Boolean gameUsersByGameIdExist) {
        this.gameUsersByGameIdExist = gameUsersByGameIdExist;
    }

    public java.util.List<GameFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<GameFilter> and) {
        this.and = and;
    }

    public java.util.List<GameFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<GameFilter> or) {
        this.or = or;
    }

    public GameFilter getNot() {
        return not;
    }
    public void setNot(GameFilter not) {
        this.not = not;
    }



    public static GameFilter.Builder builder() {
        return new GameFilter.Builder();
    }

    public static class Builder {

        private BigIntFilter id;
        private GameStateEnumFilter status;
        private StringFilter gitHash;
        private JSONFilter trace;
        private DatetimeFilter createdAt;
        private GameToManyGameUserFilter gameUsersByGameId;
        private Boolean gameUsersByGameIdExist;
        private java.util.List<GameFilter> and;
        private java.util.List<GameFilter> or;
        private GameFilter not;

        public Builder() {
        }

        public Builder setId(BigIntFilter id) {
            this.id = id;
            return this;
        }

        public Builder setStatus(GameStateEnumFilter status) {
            this.status = status;
            return this;
        }

        public Builder setGitHash(StringFilter gitHash) {
            this.gitHash = gitHash;
            return this;
        }

        public Builder setTrace(JSONFilter trace) {
            this.trace = trace;
            return this;
        }

        public Builder setCreatedAt(DatetimeFilter createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setGameUsersByGameId(GameToManyGameUserFilter gameUsersByGameId) {
            this.gameUsersByGameId = gameUsersByGameId;
            return this;
        }

        public Builder setGameUsersByGameIdExist(Boolean gameUsersByGameIdExist) {
            this.gameUsersByGameIdExist = gameUsersByGameIdExist;
            return this;
        }

        public Builder setAnd(java.util.List<GameFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<GameFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(GameFilter not) {
            this.not = not;
            return this;
        }


        public GameFilter build() {
            return new GameFilter(id, status, gitHash, trace, createdAt, gameUsersByGameId, gameUsersByGameIdExist, and, or, not);
        }

    }
}
