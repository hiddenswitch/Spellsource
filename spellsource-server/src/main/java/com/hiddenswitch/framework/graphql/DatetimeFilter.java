package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against Datetime fields. All fields are combined with a logical ‘and.’
 */
public class DatetimeFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isNull;
    private String equalTo;
    private String notEqualTo;
    private String distinctFrom;
    private String notDistinctFrom;
    private java.util.List<String> in;
    private java.util.List<String> notIn;
    private String lessThan;
    private String lessThanOrEqualTo;
    private String greaterThan;
    private String greaterThanOrEqualTo;

    public DatetimeFilter() {
    }

    public DatetimeFilter(Boolean isNull, String equalTo, String notEqualTo, String distinctFrom, String notDistinctFrom, java.util.List<String> in, java.util.List<String> notIn, String lessThan, String lessThanOrEqualTo, String greaterThan, String greaterThanOrEqualTo) {
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

    public String getEqualTo() {
        return equalTo;
    }
    public void setEqualTo(String equalTo) {
        this.equalTo = equalTo;
    }

    public String getNotEqualTo() {
        return notEqualTo;
    }
    public void setNotEqualTo(String notEqualTo) {
        this.notEqualTo = notEqualTo;
    }

    public String getDistinctFrom() {
        return distinctFrom;
    }
    public void setDistinctFrom(String distinctFrom) {
        this.distinctFrom = distinctFrom;
    }

    public String getNotDistinctFrom() {
        return notDistinctFrom;
    }
    public void setNotDistinctFrom(String notDistinctFrom) {
        this.notDistinctFrom = notDistinctFrom;
    }

    public java.util.List<String> getIn() {
        return in;
    }
    public void setIn(java.util.List<String> in) {
        this.in = in;
    }

    public java.util.List<String> getNotIn() {
        return notIn;
    }
    public void setNotIn(java.util.List<String> notIn) {
        this.notIn = notIn;
    }

    public String getLessThan() {
        return lessThan;
    }
    public void setLessThan(String lessThan) {
        this.lessThan = lessThan;
    }

    public String getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }
    public void setLessThanOrEqualTo(String lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public String getGreaterThan() {
        return greaterThan;
    }
    public void setGreaterThan(String greaterThan) {
        this.greaterThan = greaterThan;
    }

    public String getGreaterThanOrEqualTo() {
        return greaterThanOrEqualTo;
    }
    public void setGreaterThanOrEqualTo(String greaterThanOrEqualTo) {
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }



    public static DatetimeFilter.Builder builder() {
        return new DatetimeFilter.Builder();
    }

    public static class Builder {

        private Boolean isNull;
        private String equalTo;
        private String notEqualTo;
        private String distinctFrom;
        private String notDistinctFrom;
        private java.util.List<String> in;
        private java.util.List<String> notIn;
        private String lessThan;
        private String lessThanOrEqualTo;
        private String greaterThan;
        private String greaterThanOrEqualTo;

        public Builder() {
        }

        public Builder setIsNull(Boolean isNull) {
            this.isNull = isNull;
            return this;
        }

        public Builder setEqualTo(String equalTo) {
            this.equalTo = equalTo;
            return this;
        }

        public Builder setNotEqualTo(String notEqualTo) {
            this.notEqualTo = notEqualTo;
            return this;
        }

        public Builder setDistinctFrom(String distinctFrom) {
            this.distinctFrom = distinctFrom;
            return this;
        }

        public Builder setNotDistinctFrom(String notDistinctFrom) {
            this.notDistinctFrom = notDistinctFrom;
            return this;
        }

        public Builder setIn(java.util.List<String> in) {
            this.in = in;
            return this;
        }

        public Builder setNotIn(java.util.List<String> notIn) {
            this.notIn = notIn;
            return this;
        }

        public Builder setLessThan(String lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public Builder setLessThanOrEqualTo(String lessThanOrEqualTo) {
            this.lessThanOrEqualTo = lessThanOrEqualTo;
            return this;
        }

        public Builder setGreaterThan(String greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public Builder setGreaterThanOrEqualTo(String greaterThanOrEqualTo) {
            this.greaterThanOrEqualTo = greaterThanOrEqualTo;
            return this;
        }


        public DatetimeFilter build() {
            return new DatetimeFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo);
        }

    }
}
