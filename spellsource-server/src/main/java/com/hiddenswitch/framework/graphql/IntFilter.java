package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against Int fields. All fields are combined with a logical ‘and.’
 */
public class IntFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isNull;
    private Integer equalTo;
    private Integer notEqualTo;
    private Integer distinctFrom;
    private Integer notDistinctFrom;
    private java.util.List<Integer> in;
    private java.util.List<Integer> notIn;
    private Integer lessThan;
    private Integer lessThanOrEqualTo;
    private Integer greaterThan;
    private Integer greaterThanOrEqualTo;

    public IntFilter() {
    }

    public IntFilter(Boolean isNull, Integer equalTo, Integer notEqualTo, Integer distinctFrom, Integer notDistinctFrom, java.util.List<Integer> in, java.util.List<Integer> notIn, Integer lessThan, Integer lessThanOrEqualTo, Integer greaterThan, Integer greaterThanOrEqualTo) {
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

    public Integer getEqualTo() {
        return equalTo;
    }
    public void setEqualTo(Integer equalTo) {
        this.equalTo = equalTo;
    }

    public Integer getNotEqualTo() {
        return notEqualTo;
    }
    public void setNotEqualTo(Integer notEqualTo) {
        this.notEqualTo = notEqualTo;
    }

    public Integer getDistinctFrom() {
        return distinctFrom;
    }
    public void setDistinctFrom(Integer distinctFrom) {
        this.distinctFrom = distinctFrom;
    }

    public Integer getNotDistinctFrom() {
        return notDistinctFrom;
    }
    public void setNotDistinctFrom(Integer notDistinctFrom) {
        this.notDistinctFrom = notDistinctFrom;
    }

    public java.util.List<Integer> getIn() {
        return in;
    }
    public void setIn(java.util.List<Integer> in) {
        this.in = in;
    }

    public java.util.List<Integer> getNotIn() {
        return notIn;
    }
    public void setNotIn(java.util.List<Integer> notIn) {
        this.notIn = notIn;
    }

    public Integer getLessThan() {
        return lessThan;
    }
    public void setLessThan(Integer lessThan) {
        this.lessThan = lessThan;
    }

    public Integer getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }
    public void setLessThanOrEqualTo(Integer lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public Integer getGreaterThan() {
        return greaterThan;
    }
    public void setGreaterThan(Integer greaterThan) {
        this.greaterThan = greaterThan;
    }

    public Integer getGreaterThanOrEqualTo() {
        return greaterThanOrEqualTo;
    }
    public void setGreaterThanOrEqualTo(Integer greaterThanOrEqualTo) {
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }



    public static IntFilter.Builder builder() {
        return new IntFilter.Builder();
    }

    public static class Builder {

        private Boolean isNull;
        private Integer equalTo;
        private Integer notEqualTo;
        private Integer distinctFrom;
        private Integer notDistinctFrom;
        private java.util.List<Integer> in;
        private java.util.List<Integer> notIn;
        private Integer lessThan;
        private Integer lessThanOrEqualTo;
        private Integer greaterThan;
        private Integer greaterThanOrEqualTo;

        public Builder() {
        }

        public Builder setIsNull(Boolean isNull) {
            this.isNull = isNull;
            return this;
        }

        public Builder setEqualTo(Integer equalTo) {
            this.equalTo = equalTo;
            return this;
        }

        public Builder setNotEqualTo(Integer notEqualTo) {
            this.notEqualTo = notEqualTo;
            return this;
        }

        public Builder setDistinctFrom(Integer distinctFrom) {
            this.distinctFrom = distinctFrom;
            return this;
        }

        public Builder setNotDistinctFrom(Integer notDistinctFrom) {
            this.notDistinctFrom = notDistinctFrom;
            return this;
        }

        public Builder setIn(java.util.List<Integer> in) {
            this.in = in;
            return this;
        }

        public Builder setNotIn(java.util.List<Integer> notIn) {
            this.notIn = notIn;
            return this;
        }

        public Builder setLessThan(Integer lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public Builder setLessThanOrEqualTo(Integer lessThanOrEqualTo) {
            this.lessThanOrEqualTo = lessThanOrEqualTo;
            return this;
        }

        public Builder setGreaterThan(Integer greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public Builder setGreaterThanOrEqualTo(Integer greaterThanOrEqualTo) {
            this.greaterThanOrEqualTo = greaterThanOrEqualTo;
            return this;
        }


        public IntFilter build() {
            return new IntFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo);
        }

    }
}
