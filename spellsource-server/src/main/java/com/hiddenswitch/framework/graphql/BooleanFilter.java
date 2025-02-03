package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against Boolean fields. All fields are combined with a logical ‘and.’
 */
public class BooleanFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isNull;
    private Boolean equalTo;
    private Boolean notEqualTo;
    private Boolean distinctFrom;
    private Boolean notDistinctFrom;
    private java.util.List<Boolean> in;
    private java.util.List<Boolean> notIn;
    private Boolean lessThan;
    private Boolean lessThanOrEqualTo;
    private Boolean greaterThan;
    private Boolean greaterThanOrEqualTo;

    public BooleanFilter() {
    }

    public BooleanFilter(Boolean isNull, Boolean equalTo, Boolean notEqualTo, Boolean distinctFrom, Boolean notDistinctFrom, java.util.List<Boolean> in, java.util.List<Boolean> notIn, Boolean lessThan, Boolean lessThanOrEqualTo, Boolean greaterThan, Boolean greaterThanOrEqualTo) {
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

    public Boolean getEqualTo() {
        return equalTo;
    }
    public void setEqualTo(Boolean equalTo) {
        this.equalTo = equalTo;
    }

    public Boolean getNotEqualTo() {
        return notEqualTo;
    }
    public void setNotEqualTo(Boolean notEqualTo) {
        this.notEqualTo = notEqualTo;
    }

    public Boolean getDistinctFrom() {
        return distinctFrom;
    }
    public void setDistinctFrom(Boolean distinctFrom) {
        this.distinctFrom = distinctFrom;
    }

    public Boolean getNotDistinctFrom() {
        return notDistinctFrom;
    }
    public void setNotDistinctFrom(Boolean notDistinctFrom) {
        this.notDistinctFrom = notDistinctFrom;
    }

    public java.util.List<Boolean> getIn() {
        return in;
    }
    public void setIn(java.util.List<Boolean> in) {
        this.in = in;
    }

    public java.util.List<Boolean> getNotIn() {
        return notIn;
    }
    public void setNotIn(java.util.List<Boolean> notIn) {
        this.notIn = notIn;
    }

    public Boolean getLessThan() {
        return lessThan;
    }
    public void setLessThan(Boolean lessThan) {
        this.lessThan = lessThan;
    }

    public Boolean getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }
    public void setLessThanOrEqualTo(Boolean lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public Boolean getGreaterThan() {
        return greaterThan;
    }
    public void setGreaterThan(Boolean greaterThan) {
        this.greaterThan = greaterThan;
    }

    public Boolean getGreaterThanOrEqualTo() {
        return greaterThanOrEqualTo;
    }
    public void setGreaterThanOrEqualTo(Boolean greaterThanOrEqualTo) {
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }



    public static BooleanFilter.Builder builder() {
        return new BooleanFilter.Builder();
    }

    public static class Builder {

        private Boolean isNull;
        private Boolean equalTo;
        private Boolean notEqualTo;
        private Boolean distinctFrom;
        private Boolean notDistinctFrom;
        private java.util.List<Boolean> in;
        private java.util.List<Boolean> notIn;
        private Boolean lessThan;
        private Boolean lessThanOrEqualTo;
        private Boolean greaterThan;
        private Boolean greaterThanOrEqualTo;

        public Builder() {
        }

        public Builder setIsNull(Boolean isNull) {
            this.isNull = isNull;
            return this;
        }

        public Builder setEqualTo(Boolean equalTo) {
            this.equalTo = equalTo;
            return this;
        }

        public Builder setNotEqualTo(Boolean notEqualTo) {
            this.notEqualTo = notEqualTo;
            return this;
        }

        public Builder setDistinctFrom(Boolean distinctFrom) {
            this.distinctFrom = distinctFrom;
            return this;
        }

        public Builder setNotDistinctFrom(Boolean notDistinctFrom) {
            this.notDistinctFrom = notDistinctFrom;
            return this;
        }

        public Builder setIn(java.util.List<Boolean> in) {
            this.in = in;
            return this;
        }

        public Builder setNotIn(java.util.List<Boolean> notIn) {
            this.notIn = notIn;
            return this;
        }

        public Builder setLessThan(Boolean lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public Builder setLessThanOrEqualTo(Boolean lessThanOrEqualTo) {
            this.lessThanOrEqualTo = lessThanOrEqualTo;
            return this;
        }

        public Builder setGreaterThan(Boolean greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public Builder setGreaterThanOrEqualTo(Boolean greaterThanOrEqualTo) {
            this.greaterThanOrEqualTo = greaterThanOrEqualTo;
            return this;
        }


        public BooleanFilter build() {
            return new BooleanFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo);
        }

    }
}
