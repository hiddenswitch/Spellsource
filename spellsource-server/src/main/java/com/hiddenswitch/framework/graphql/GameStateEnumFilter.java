package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against GameStateEnum fields. All fields are combined with a logical ‘and.’
 */
public class GameStateEnumFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isNull;
    private GameStateEnum equalTo;
    private GameStateEnum notEqualTo;
    private GameStateEnum distinctFrom;
    private GameStateEnum notDistinctFrom;
    private java.util.List<GameStateEnum> in;
    private java.util.List<GameStateEnum> notIn;
    private GameStateEnum lessThan;
    private GameStateEnum lessThanOrEqualTo;
    private GameStateEnum greaterThan;
    private GameStateEnum greaterThanOrEqualTo;

    public GameStateEnumFilter() {
    }

    public GameStateEnumFilter(Boolean isNull, GameStateEnum equalTo, GameStateEnum notEqualTo, GameStateEnum distinctFrom, GameStateEnum notDistinctFrom, java.util.List<GameStateEnum> in, java.util.List<GameStateEnum> notIn, GameStateEnum lessThan, GameStateEnum lessThanOrEqualTo, GameStateEnum greaterThan, GameStateEnum greaterThanOrEqualTo) {
        this.isNull = isNull;
        this.equalTo = equalTo;
        this.notEqualTo = notEqualTo;
        this.distinctFrom = distinctFrom;
        this.notDistinctFrom = notDistinctFrom;
        this.in = in;
        this.notIn = notIn;
        this.lessThan = lessThan;
        this.lessThanOrEqualTo = lessThanOrEqualTo;
        this.greaterThan = greaterThan;
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }

    public Boolean getIsNull() {
        return isNull;
    }
    public void setIsNull(Boolean isNull) {
        this.isNull = isNull;
    }

    public GameStateEnum getEqualTo() {
        return equalTo;
    }
    public void setEqualTo(GameStateEnum equalTo) {
        this.equalTo = equalTo;
    }

    public GameStateEnum getNotEqualTo() {
        return notEqualTo;
    }
    public void setNotEqualTo(GameStateEnum notEqualTo) {
        this.notEqualTo = notEqualTo;
    }

    public GameStateEnum getDistinctFrom() {
        return distinctFrom;
    }
    public void setDistinctFrom(GameStateEnum distinctFrom) {
        this.distinctFrom = distinctFrom;
    }

    public GameStateEnum getNotDistinctFrom() {
        return notDistinctFrom;
    }
    public void setNotDistinctFrom(GameStateEnum notDistinctFrom) {
        this.notDistinctFrom = notDistinctFrom;
    }

    public java.util.List<GameStateEnum> getIn() {
        return in;
    }
    public void setIn(java.util.List<GameStateEnum> in) {
        this.in = in;
    }

    public java.util.List<GameStateEnum> getNotIn() {
        return notIn;
    }
    public void setNotIn(java.util.List<GameStateEnum> notIn) {
        this.notIn = notIn;
    }

    public GameStateEnum getLessThan() {
        return lessThan;
    }
    public void setLessThan(GameStateEnum lessThan) {
        this.lessThan = lessThan;
    }

    public GameStateEnum getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }
    public void setLessThanOrEqualTo(GameStateEnum lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public GameStateEnum getGreaterThan() {
        return greaterThan;
    }
    public void setGreaterThan(GameStateEnum greaterThan) {
        this.greaterThan = greaterThan;
    }

    public GameStateEnum getGreaterThanOrEqualTo() {
        return greaterThanOrEqualTo;
    }
    public void setGreaterThanOrEqualTo(GameStateEnum greaterThanOrEqualTo) {
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }



    public static GameStateEnumFilter.Builder builder() {
        return new GameStateEnumFilter.Builder();
    }

    public static class Builder {

        private Boolean isNull;
        private GameStateEnum equalTo;
        private GameStateEnum notEqualTo;
        private GameStateEnum distinctFrom;
        private GameStateEnum notDistinctFrom;
        private java.util.List<GameStateEnum> in;
        private java.util.List<GameStateEnum> notIn;
        private GameStateEnum lessThan;
        private GameStateEnum lessThanOrEqualTo;
        private GameStateEnum greaterThan;
        private GameStateEnum greaterThanOrEqualTo;

        public Builder() {
        }

        public Builder setIsNull(Boolean isNull) {
            this.isNull = isNull;
            return this;
        }

        public Builder setEqualTo(GameStateEnum equalTo) {
            this.equalTo = equalTo;
            return this;
        }

        public Builder setNotEqualTo(GameStateEnum notEqualTo) {
            this.notEqualTo = notEqualTo;
            return this;
        }

        public Builder setDistinctFrom(GameStateEnum distinctFrom) {
            this.distinctFrom = distinctFrom;
            return this;
        }

        public Builder setNotDistinctFrom(GameStateEnum notDistinctFrom) {
            this.notDistinctFrom = notDistinctFrom;
            return this;
        }

        public Builder setIn(java.util.List<GameStateEnum> in) {
            this.in = in;
            return this;
        }

        public Builder setNotIn(java.util.List<GameStateEnum> notIn) {
            this.notIn = notIn;
            return this;
        }

        public Builder setLessThan(GameStateEnum lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public Builder setLessThanOrEqualTo(GameStateEnum lessThanOrEqualTo) {
            this.lessThanOrEqualTo = lessThanOrEqualTo;
            return this;
        }

        public Builder setGreaterThan(GameStateEnum greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public Builder setGreaterThanOrEqualTo(GameStateEnum greaterThanOrEqualTo) {
            this.greaterThanOrEqualTo = greaterThanOrEqualTo;
            return this;
        }


        public GameStateEnumFilter build() {
            return new GameStateEnumFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo);
        }

    }
}
