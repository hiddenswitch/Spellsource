package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against GameUserVictoryEnum fields. All fields are combined with a logical ‘and.’
 */
public class GameUserVictoryEnumFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isNull;
    private GameUserVictoryEnum equalTo;
    private GameUserVictoryEnum notEqualTo;
    private GameUserVictoryEnum distinctFrom;
    private GameUserVictoryEnum notDistinctFrom;
    private java.util.List<GameUserVictoryEnum> in;
    private java.util.List<GameUserVictoryEnum> notIn;
    private GameUserVictoryEnum lessThan;
    private GameUserVictoryEnum lessThanOrEqualTo;
    private GameUserVictoryEnum greaterThan;
    private GameUserVictoryEnum greaterThanOrEqualTo;

    public GameUserVictoryEnumFilter() {
    }

    public GameUserVictoryEnumFilter(Boolean isNull, GameUserVictoryEnum equalTo, GameUserVictoryEnum notEqualTo, GameUserVictoryEnum distinctFrom, GameUserVictoryEnum notDistinctFrom, java.util.List<GameUserVictoryEnum> in, java.util.List<GameUserVictoryEnum> notIn, GameUserVictoryEnum lessThan, GameUserVictoryEnum lessThanOrEqualTo, GameUserVictoryEnum greaterThan, GameUserVictoryEnum greaterThanOrEqualTo) {
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

    public GameUserVictoryEnum getEqualTo() {
        return equalTo;
    }
    public void setEqualTo(GameUserVictoryEnum equalTo) {
        this.equalTo = equalTo;
    }

    public GameUserVictoryEnum getNotEqualTo() {
        return notEqualTo;
    }
    public void setNotEqualTo(GameUserVictoryEnum notEqualTo) {
        this.notEqualTo = notEqualTo;
    }

    public GameUserVictoryEnum getDistinctFrom() {
        return distinctFrom;
    }
    public void setDistinctFrom(GameUserVictoryEnum distinctFrom) {
        this.distinctFrom = distinctFrom;
    }

    public GameUserVictoryEnum getNotDistinctFrom() {
        return notDistinctFrom;
    }
    public void setNotDistinctFrom(GameUserVictoryEnum notDistinctFrom) {
        this.notDistinctFrom = notDistinctFrom;
    }

    public java.util.List<GameUserVictoryEnum> getIn() {
        return in;
    }
    public void setIn(java.util.List<GameUserVictoryEnum> in) {
        this.in = in;
    }

    public java.util.List<GameUserVictoryEnum> getNotIn() {
        return notIn;
    }
    public void setNotIn(java.util.List<GameUserVictoryEnum> notIn) {
        this.notIn = notIn;
    }

    public GameUserVictoryEnum getLessThan() {
        return lessThan;
    }
    public void setLessThan(GameUserVictoryEnum lessThan) {
        this.lessThan = lessThan;
    }

    public GameUserVictoryEnum getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }
    public void setLessThanOrEqualTo(GameUserVictoryEnum lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public GameUserVictoryEnum getGreaterThan() {
        return greaterThan;
    }
    public void setGreaterThan(GameUserVictoryEnum greaterThan) {
        this.greaterThan = greaterThan;
    }

    public GameUserVictoryEnum getGreaterThanOrEqualTo() {
        return greaterThanOrEqualTo;
    }
    public void setGreaterThanOrEqualTo(GameUserVictoryEnum greaterThanOrEqualTo) {
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }



    public static GameUserVictoryEnumFilter.Builder builder() {
        return new GameUserVictoryEnumFilter.Builder();
    }

    public static class Builder {

        private Boolean isNull;
        private GameUserVictoryEnum equalTo;
        private GameUserVictoryEnum notEqualTo;
        private GameUserVictoryEnum distinctFrom;
        private GameUserVictoryEnum notDistinctFrom;
        private java.util.List<GameUserVictoryEnum> in;
        private java.util.List<GameUserVictoryEnum> notIn;
        private GameUserVictoryEnum lessThan;
        private GameUserVictoryEnum lessThanOrEqualTo;
        private GameUserVictoryEnum greaterThan;
        private GameUserVictoryEnum greaterThanOrEqualTo;

        public Builder() {
        }

        public Builder setIsNull(Boolean isNull) {
            this.isNull = isNull;
            return this;
        }

        public Builder setEqualTo(GameUserVictoryEnum equalTo) {
            this.equalTo = equalTo;
            return this;
        }

        public Builder setNotEqualTo(GameUserVictoryEnum notEqualTo) {
            this.notEqualTo = notEqualTo;
            return this;
        }

        public Builder setDistinctFrom(GameUserVictoryEnum distinctFrom) {
            this.distinctFrom = distinctFrom;
            return this;
        }

        public Builder setNotDistinctFrom(GameUserVictoryEnum notDistinctFrom) {
            this.notDistinctFrom = notDistinctFrom;
            return this;
        }

        public Builder setIn(java.util.List<GameUserVictoryEnum> in) {
            this.in = in;
            return this;
        }

        public Builder setNotIn(java.util.List<GameUserVictoryEnum> notIn) {
            this.notIn = notIn;
            return this;
        }

        public Builder setLessThan(GameUserVictoryEnum lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public Builder setLessThanOrEqualTo(GameUserVictoryEnum lessThanOrEqualTo) {
            this.lessThanOrEqualTo = lessThanOrEqualTo;
            return this;
        }

        public Builder setGreaterThan(GameUserVictoryEnum greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public Builder setGreaterThanOrEqualTo(GameUserVictoryEnum greaterThanOrEqualTo) {
            this.greaterThanOrEqualTo = greaterThanOrEqualTo;
            return this;
        }


        public GameUserVictoryEnumFilter build() {
            return new GameUserVictoryEnumFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo);
        }

    }
}
