package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against String List fields. All fields are combined with a logical ‘and.’
 */
public class StringListFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean isNull;
    private java.util.List<String> equalTo;
    private java.util.List<String> notEqualTo;
    private java.util.List<String> distinctFrom;
    private java.util.List<String> notDistinctFrom;
    private java.util.List<String> lessThan;
    private java.util.List<String> lessThanOrEqualTo;
    private java.util.List<String> greaterThan;
    private java.util.List<String> greaterThanOrEqualTo;
    private java.util.List<String> contains;
    private java.util.List<String> containedBy;
    private java.util.List<String> overlaps;
    private String anyEqualTo;
    private String anyNotEqualTo;
    private String anyLessThan;
    private String anyLessThanOrEqualTo;
    private String anyGreaterThan;
    private String anyGreaterThanOrEqualTo;

    public StringListFilter() {
    }

    public StringListFilter(Boolean isNull, java.util.List<String> equalTo, java.util.List<String> notEqualTo, java.util.List<String> distinctFrom, java.util.List<String> notDistinctFrom, java.util.List<String> lessThan, java.util.List<String> lessThanOrEqualTo, java.util.List<String> greaterThan, java.util.List<String> greaterThanOrEqualTo, java.util.List<String> contains, java.util.List<String> containedBy, java.util.List<String> overlaps, String anyEqualTo, String anyNotEqualTo, String anyLessThan, String anyLessThanOrEqualTo, String anyGreaterThan, String anyGreaterThanOrEqualTo) {
        this.isNull = isNull;
        this.equalTo = equalTo;
        this.notEqualTo = notEqualTo;
        this.distinctFrom = distinctFrom;
        this.notDistinctFrom = notDistinctFrom;
        this.lessThan = lessThan;
        this.lessThanOrEqualTo = lessThanOrEqualTo;
        this.greaterThan = greaterThan;
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
        this.contains = contains;
        this.containedBy = containedBy;
        this.overlaps = overlaps;
        this.anyEqualTo = anyEqualTo;
        this.anyNotEqualTo = anyNotEqualTo;
        this.anyLessThan = anyLessThan;
        this.anyLessThanOrEqualTo = anyLessThanOrEqualTo;
        this.anyGreaterThan = anyGreaterThan;
        this.anyGreaterThanOrEqualTo = anyGreaterThanOrEqualTo;
    }

    public Boolean getIsNull() {
        return isNull;
    }
    public void setIsNull(Boolean isNull) {
        this.isNull = isNull;
    }

    public java.util.List<String> getEqualTo() {
        return equalTo;
    }
    public void setEqualTo(java.util.List<String> equalTo) {
        this.equalTo = equalTo;
    }

    public java.util.List<String> getNotEqualTo() {
        return notEqualTo;
    }
    public void setNotEqualTo(java.util.List<String> notEqualTo) {
        this.notEqualTo = notEqualTo;
    }

    public java.util.List<String> getDistinctFrom() {
        return distinctFrom;
    }
    public void setDistinctFrom(java.util.List<String> distinctFrom) {
        this.distinctFrom = distinctFrom;
    }

    public java.util.List<String> getNotDistinctFrom() {
        return notDistinctFrom;
    }
    public void setNotDistinctFrom(java.util.List<String> notDistinctFrom) {
        this.notDistinctFrom = notDistinctFrom;
    }

    public java.util.List<String> getLessThan() {
        return lessThan;
    }
    public void setLessThan(java.util.List<String> lessThan) {
        this.lessThan = lessThan;
    }

    public java.util.List<String> getLessThanOrEqualTo() {
        return lessThanOrEqualTo;
    }
    public void setLessThanOrEqualTo(java.util.List<String> lessThanOrEqualTo) {
        this.lessThanOrEqualTo = lessThanOrEqualTo;
    }

    public java.util.List<String> getGreaterThan() {
        return greaterThan;
    }
    public void setGreaterThan(java.util.List<String> greaterThan) {
        this.greaterThan = greaterThan;
    }

    public java.util.List<String> getGreaterThanOrEqualTo() {
        return greaterThanOrEqualTo;
    }
    public void setGreaterThanOrEqualTo(java.util.List<String> greaterThanOrEqualTo) {
        this.greaterThanOrEqualTo = greaterThanOrEqualTo;
    }

    public java.util.List<String> getContains() {
        return contains;
    }
    public void setContains(java.util.List<String> contains) {
        this.contains = contains;
    }

    public java.util.List<String> getContainedBy() {
        return containedBy;
    }
    public void setContainedBy(java.util.List<String> containedBy) {
        this.containedBy = containedBy;
    }

    public java.util.List<String> getOverlaps() {
        return overlaps;
    }
    public void setOverlaps(java.util.List<String> overlaps) {
        this.overlaps = overlaps;
    }

    public String getAnyEqualTo() {
        return anyEqualTo;
    }
    public void setAnyEqualTo(String anyEqualTo) {
        this.anyEqualTo = anyEqualTo;
    }

    public String getAnyNotEqualTo() {
        return anyNotEqualTo;
    }
    public void setAnyNotEqualTo(String anyNotEqualTo) {
        this.anyNotEqualTo = anyNotEqualTo;
    }

    public String getAnyLessThan() {
        return anyLessThan;
    }
    public void setAnyLessThan(String anyLessThan) {
        this.anyLessThan = anyLessThan;
    }

    public String getAnyLessThanOrEqualTo() {
        return anyLessThanOrEqualTo;
    }
    public void setAnyLessThanOrEqualTo(String anyLessThanOrEqualTo) {
        this.anyLessThanOrEqualTo = anyLessThanOrEqualTo;
    }

    public String getAnyGreaterThan() {
        return anyGreaterThan;
    }
    public void setAnyGreaterThan(String anyGreaterThan) {
        this.anyGreaterThan = anyGreaterThan;
    }

    public String getAnyGreaterThanOrEqualTo() {
        return anyGreaterThanOrEqualTo;
    }
    public void setAnyGreaterThanOrEqualTo(String anyGreaterThanOrEqualTo) {
        this.anyGreaterThanOrEqualTo = anyGreaterThanOrEqualTo;
    }



    public static StringListFilter.Builder builder() {
        return new StringListFilter.Builder();
    }

    public static class Builder {

        private Boolean isNull;
        private java.util.List<String> equalTo;
        private java.util.List<String> notEqualTo;
        private java.util.List<String> distinctFrom;
        private java.util.List<String> notDistinctFrom;
        private java.util.List<String> lessThan;
        private java.util.List<String> lessThanOrEqualTo;
        private java.util.List<String> greaterThan;
        private java.util.List<String> greaterThanOrEqualTo;
        private java.util.List<String> contains;
        private java.util.List<String> containedBy;
        private java.util.List<String> overlaps;
        private String anyEqualTo;
        private String anyNotEqualTo;
        private String anyLessThan;
        private String anyLessThanOrEqualTo;
        private String anyGreaterThan;
        private String anyGreaterThanOrEqualTo;

        public Builder() {
        }

        public Builder setIsNull(Boolean isNull) {
            this.isNull = isNull;
            return this;
        }

        public Builder setEqualTo(java.util.List<String> equalTo) {
            this.equalTo = equalTo;
            return this;
        }

        public Builder setNotEqualTo(java.util.List<String> notEqualTo) {
            this.notEqualTo = notEqualTo;
            return this;
        }

        public Builder setDistinctFrom(java.util.List<String> distinctFrom) {
            this.distinctFrom = distinctFrom;
            return this;
        }

        public Builder setNotDistinctFrom(java.util.List<String> notDistinctFrom) {
            this.notDistinctFrom = notDistinctFrom;
            return this;
        }

        public Builder setLessThan(java.util.List<String> lessThan) {
            this.lessThan = lessThan;
            return this;
        }

        public Builder setLessThanOrEqualTo(java.util.List<String> lessThanOrEqualTo) {
            this.lessThanOrEqualTo = lessThanOrEqualTo;
            return this;
        }

        public Builder setGreaterThan(java.util.List<String> greaterThan) {
            this.greaterThan = greaterThan;
            return this;
        }

        public Builder setGreaterThanOrEqualTo(java.util.List<String> greaterThanOrEqualTo) {
            this.greaterThanOrEqualTo = greaterThanOrEqualTo;
            return this;
        }

        public Builder setContains(java.util.List<String> contains) {
            this.contains = contains;
            return this;
        }

        public Builder setContainedBy(java.util.List<String> containedBy) {
            this.containedBy = containedBy;
            return this;
        }

        public Builder setOverlaps(java.util.List<String> overlaps) {
            this.overlaps = overlaps;
            return this;
        }

        public Builder setAnyEqualTo(String anyEqualTo) {
            this.anyEqualTo = anyEqualTo;
            return this;
        }

        public Builder setAnyNotEqualTo(String anyNotEqualTo) {
            this.anyNotEqualTo = anyNotEqualTo;
            return this;
        }

        public Builder setAnyLessThan(String anyLessThan) {
            this.anyLessThan = anyLessThan;
            return this;
        }

        public Builder setAnyLessThanOrEqualTo(String anyLessThanOrEqualTo) {
            this.anyLessThanOrEqualTo = anyLessThanOrEqualTo;
            return this;
        }

        public Builder setAnyGreaterThan(String anyGreaterThan) {
            this.anyGreaterThan = anyGreaterThan;
            return this;
        }

        public Builder setAnyGreaterThanOrEqualTo(String anyGreaterThanOrEqualTo) {
            this.anyGreaterThanOrEqualTo = anyGreaterThanOrEqualTo;
            return this;
        }


        public StringListFilter build() {
            return new StringListFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo, contains, containedBy, overlaps, anyEqualTo, anyNotEqualTo, anyLessThan, anyLessThanOrEqualTo, anyGreaterThan, anyGreaterThanOrEqualTo);
        }

    }
}
