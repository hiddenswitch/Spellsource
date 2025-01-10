package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against JSON fields. All fields are combined with a logical ‘and.’
 */
public class JSONFilter implements java.io.Serializable {

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
    private String contains;
    private String containsKey;
    private java.util.List<String> containsAllKeys;
    private java.util.List<String> containsAnyKeys;
    private String containedBy;

    public JSONFilter() {
    }

    public JSONFilter(Boolean isNull, String equalTo, String notEqualTo, String distinctFrom, String notDistinctFrom, java.util.List<String> in, java.util.List<String> notIn, String lessThan, String lessThanOrEqualTo, String greaterThan, String greaterThanOrEqualTo, String contains, String containsKey, java.util.List<String> containsAllKeys, java.util.List<String> containsAnyKeys, String containedBy) {
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
        this.contains = contains;
        this.containsKey = containsKey;
        this.containsAllKeys = containsAllKeys;
        this.containsAnyKeys = containsAnyKeys;
        this.containedBy = containedBy;
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

    public String getContains() {
        return contains;
    }
    public void setContains(String contains) {
        this.contains = contains;
    }

    public String getContainsKey() {
        return containsKey;
    }
    public void setContainsKey(String containsKey) {
        this.containsKey = containsKey;
    }

    public java.util.List<String> getContainsAllKeys() {
        return containsAllKeys;
    }
    public void setContainsAllKeys(java.util.List<String> containsAllKeys) {
        this.containsAllKeys = containsAllKeys;
    }

    public java.util.List<String> getContainsAnyKeys() {
        return containsAnyKeys;
    }
    public void setContainsAnyKeys(java.util.List<String> containsAnyKeys) {
        this.containsAnyKeys = containsAnyKeys;
    }

    public String getContainedBy() {
        return containedBy;
    }
    public void setContainedBy(String containedBy) {
        this.containedBy = containedBy;
    }



    public static JSONFilter.Builder builder() {
        return new JSONFilter.Builder();
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
        private String contains;
        private String containsKey;
        private java.util.List<String> containsAllKeys;
        private java.util.List<String> containsAnyKeys;
        private String containedBy;

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

        public Builder setContains(String contains) {
            this.contains = contains;
            return this;
        }

        public Builder setContainsKey(String containsKey) {
            this.containsKey = containsKey;
            return this;
        }

        public Builder setContainsAllKeys(java.util.List<String> containsAllKeys) {
            this.containsAllKeys = containsAllKeys;
            return this;
        }

        public Builder setContainsAnyKeys(java.util.List<String> containsAnyKeys) {
            this.containsAnyKeys = containsAnyKeys;
            return this;
        }

        public Builder setContainedBy(String containedBy) {
            this.containedBy = containedBy;
            return this;
        }


        public JSONFilter build() {
            return new JSONFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo, contains, containsKey, containsAllKeys, containsAnyKeys, containedBy);
        }

    }
}
