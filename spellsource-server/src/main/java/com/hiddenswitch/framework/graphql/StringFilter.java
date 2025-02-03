package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against String fields. All fields are combined with a logical ‘and.’
 */
public class StringFilter implements java.io.Serializable {

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
    private String includes;
    private String notIncludes;
    private String includesInsensitive;
    private String notIncludesInsensitive;
    private String startsWith;
    private String notStartsWith;
    private String startsWithInsensitive;
    private String notStartsWithInsensitive;
    private String endsWith;
    private String notEndsWith;
    private String endsWithInsensitive;
    private String notEndsWithInsensitive;
    private String like;
    private String notLike;
    private String likeInsensitive;
    private String notLikeInsensitive;
    private String equalToInsensitive;
    private String notEqualToInsensitive;
    private String distinctFromInsensitive;
    private String notDistinctFromInsensitive;
    private java.util.List<String> inInsensitive;
    private java.util.List<String> notInInsensitive;
    private String lessThanInsensitive;
    private String lessThanOrEqualToInsensitive;
    private String greaterThanInsensitive;
    private String greaterThanOrEqualToInsensitive;

    public StringFilter() {
    }

    public StringFilter(Boolean isNull, String equalTo, String notEqualTo, String distinctFrom, String notDistinctFrom, java.util.List<String> in, java.util.List<String> notIn, String lessThan, String lessThanOrEqualTo, String greaterThan, String greaterThanOrEqualTo, String includes, String notIncludes, String includesInsensitive, String notIncludesInsensitive, String startsWith, String notStartsWith, String startsWithInsensitive, String notStartsWithInsensitive, String endsWith, String notEndsWith, String endsWithInsensitive, String notEndsWithInsensitive, String like, String notLike, String likeInsensitive, String notLikeInsensitive, String equalToInsensitive, String notEqualToInsensitive, String distinctFromInsensitive, String notDistinctFromInsensitive, java.util.List<String> inInsensitive, java.util.List<String> notInInsensitive, String lessThanInsensitive, String lessThanOrEqualToInsensitive, String greaterThanInsensitive, String greaterThanOrEqualToInsensitive) {
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
        this.includes = includes;
        this.notIncludes = notIncludes;
        this.includesInsensitive = includesInsensitive;
        this.notIncludesInsensitive = notIncludesInsensitive;
        this.startsWith = startsWith;
        this.notStartsWith = notStartsWith;
        this.startsWithInsensitive = startsWithInsensitive;
        this.notStartsWithInsensitive = notStartsWithInsensitive;
        this.endsWith = endsWith;
        this.notEndsWith = notEndsWith;
        this.endsWithInsensitive = endsWithInsensitive;
        this.notEndsWithInsensitive = notEndsWithInsensitive;
        this.like = like;
        this.notLike = notLike;
        this.likeInsensitive = likeInsensitive;
        this.notLikeInsensitive = notLikeInsensitive;
        this.equalToInsensitive = equalToInsensitive;
        this.notEqualToInsensitive = notEqualToInsensitive;
        this.distinctFromInsensitive = distinctFromInsensitive;
        this.notDistinctFromInsensitive = notDistinctFromInsensitive;
        this.inInsensitive = inInsensitive;
        this.notInInsensitive = notInInsensitive;
        this.lessThanInsensitive = lessThanInsensitive;
        this.lessThanOrEqualToInsensitive = lessThanOrEqualToInsensitive;
        this.greaterThanInsensitive = greaterThanInsensitive;
        this.greaterThanOrEqualToInsensitive = greaterThanOrEqualToInsensitive;
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

    public String getIncludes() {
        return includes;
    }
    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getNotIncludes() {
        return notIncludes;
    }
    public void setNotIncludes(String notIncludes) {
        this.notIncludes = notIncludes;
    }

    public String getIncludesInsensitive() {
        return includesInsensitive;
    }
    public void setIncludesInsensitive(String includesInsensitive) {
        this.includesInsensitive = includesInsensitive;
    }

    public String getNotIncludesInsensitive() {
        return notIncludesInsensitive;
    }
    public void setNotIncludesInsensitive(String notIncludesInsensitive) {
        this.notIncludesInsensitive = notIncludesInsensitive;
    }

    public String getStartsWith() {
        return startsWith;
    }
    public void setStartsWith(String startsWith) {
        this.startsWith = startsWith;
    }

    public String getNotStartsWith() {
        return notStartsWith;
    }
    public void setNotStartsWith(String notStartsWith) {
        this.notStartsWith = notStartsWith;
    }

    public String getStartsWithInsensitive() {
        return startsWithInsensitive;
    }
    public void setStartsWithInsensitive(String startsWithInsensitive) {
        this.startsWithInsensitive = startsWithInsensitive;
    }

    public String getNotStartsWithInsensitive() {
        return notStartsWithInsensitive;
    }
    public void setNotStartsWithInsensitive(String notStartsWithInsensitive) {
        this.notStartsWithInsensitive = notStartsWithInsensitive;
    }

    public String getEndsWith() {
        return endsWith;
    }
    public void setEndsWith(String endsWith) {
        this.endsWith = endsWith;
    }

    public String getNotEndsWith() {
        return notEndsWith;
    }
    public void setNotEndsWith(String notEndsWith) {
        this.notEndsWith = notEndsWith;
    }

    public String getEndsWithInsensitive() {
        return endsWithInsensitive;
    }
    public void setEndsWithInsensitive(String endsWithInsensitive) {
        this.endsWithInsensitive = endsWithInsensitive;
    }

    public String getNotEndsWithInsensitive() {
        return notEndsWithInsensitive;
    }
    public void setNotEndsWithInsensitive(String notEndsWithInsensitive) {
        this.notEndsWithInsensitive = notEndsWithInsensitive;
    }

    public String getLike() {
        return like;
    }
    public void setLike(String like) {
        this.like = like;
    }

    public String getNotLike() {
        return notLike;
    }
    public void setNotLike(String notLike) {
        this.notLike = notLike;
    }

    public String getLikeInsensitive() {
        return likeInsensitive;
    }
    public void setLikeInsensitive(String likeInsensitive) {
        this.likeInsensitive = likeInsensitive;
    }

    public String getNotLikeInsensitive() {
        return notLikeInsensitive;
    }
    public void setNotLikeInsensitive(String notLikeInsensitive) {
        this.notLikeInsensitive = notLikeInsensitive;
    }

    public String getEqualToInsensitive() {
        return equalToInsensitive;
    }
    public void setEqualToInsensitive(String equalToInsensitive) {
        this.equalToInsensitive = equalToInsensitive;
    }

    public String getNotEqualToInsensitive() {
        return notEqualToInsensitive;
    }
    public void setNotEqualToInsensitive(String notEqualToInsensitive) {
        this.notEqualToInsensitive = notEqualToInsensitive;
    }

    public String getDistinctFromInsensitive() {
        return distinctFromInsensitive;
    }
    public void setDistinctFromInsensitive(String distinctFromInsensitive) {
        this.distinctFromInsensitive = distinctFromInsensitive;
    }

    public String getNotDistinctFromInsensitive() {
        return notDistinctFromInsensitive;
    }
    public void setNotDistinctFromInsensitive(String notDistinctFromInsensitive) {
        this.notDistinctFromInsensitive = notDistinctFromInsensitive;
    }

    public java.util.List<String> getInInsensitive() {
        return inInsensitive;
    }
    public void setInInsensitive(java.util.List<String> inInsensitive) {
        this.inInsensitive = inInsensitive;
    }

    public java.util.List<String> getNotInInsensitive() {
        return notInInsensitive;
    }
    public void setNotInInsensitive(java.util.List<String> notInInsensitive) {
        this.notInInsensitive = notInInsensitive;
    }

    public String getLessThanInsensitive() {
        return lessThanInsensitive;
    }
    public void setLessThanInsensitive(String lessThanInsensitive) {
        this.lessThanInsensitive = lessThanInsensitive;
    }

    public String getLessThanOrEqualToInsensitive() {
        return lessThanOrEqualToInsensitive;
    }
    public void setLessThanOrEqualToInsensitive(String lessThanOrEqualToInsensitive) {
        this.lessThanOrEqualToInsensitive = lessThanOrEqualToInsensitive;
    }

    public String getGreaterThanInsensitive() {
        return greaterThanInsensitive;
    }
    public void setGreaterThanInsensitive(String greaterThanInsensitive) {
        this.greaterThanInsensitive = greaterThanInsensitive;
    }

    public String getGreaterThanOrEqualToInsensitive() {
        return greaterThanOrEqualToInsensitive;
    }
    public void setGreaterThanOrEqualToInsensitive(String greaterThanOrEqualToInsensitive) {
        this.greaterThanOrEqualToInsensitive = greaterThanOrEqualToInsensitive;
    }



    public static StringFilter.Builder builder() {
        return new StringFilter.Builder();
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
        private String includes;
        private String notIncludes;
        private String includesInsensitive;
        private String notIncludesInsensitive;
        private String startsWith;
        private String notStartsWith;
        private String startsWithInsensitive;
        private String notStartsWithInsensitive;
        private String endsWith;
        private String notEndsWith;
        private String endsWithInsensitive;
        private String notEndsWithInsensitive;
        private String like;
        private String notLike;
        private String likeInsensitive;
        private String notLikeInsensitive;
        private String equalToInsensitive;
        private String notEqualToInsensitive;
        private String distinctFromInsensitive;
        private String notDistinctFromInsensitive;
        private java.util.List<String> inInsensitive;
        private java.util.List<String> notInInsensitive;
        private String lessThanInsensitive;
        private String lessThanOrEqualToInsensitive;
        private String greaterThanInsensitive;
        private String greaterThanOrEqualToInsensitive;

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

        public Builder setIncludes(String includes) {
            this.includes = includes;
            return this;
        }

        public Builder setNotIncludes(String notIncludes) {
            this.notIncludes = notIncludes;
            return this;
        }

        public Builder setIncludesInsensitive(String includesInsensitive) {
            this.includesInsensitive = includesInsensitive;
            return this;
        }

        public Builder setNotIncludesInsensitive(String notIncludesInsensitive) {
            this.notIncludesInsensitive = notIncludesInsensitive;
            return this;
        }

        public Builder setStartsWith(String startsWith) {
            this.startsWith = startsWith;
            return this;
        }

        public Builder setNotStartsWith(String notStartsWith) {
            this.notStartsWith = notStartsWith;
            return this;
        }

        public Builder setStartsWithInsensitive(String startsWithInsensitive) {
            this.startsWithInsensitive = startsWithInsensitive;
            return this;
        }

        public Builder setNotStartsWithInsensitive(String notStartsWithInsensitive) {
            this.notStartsWithInsensitive = notStartsWithInsensitive;
            return this;
        }

        public Builder setEndsWith(String endsWith) {
            this.endsWith = endsWith;
            return this;
        }

        public Builder setNotEndsWith(String notEndsWith) {
            this.notEndsWith = notEndsWith;
            return this;
        }

        public Builder setEndsWithInsensitive(String endsWithInsensitive) {
            this.endsWithInsensitive = endsWithInsensitive;
            return this;
        }

        public Builder setNotEndsWithInsensitive(String notEndsWithInsensitive) {
            this.notEndsWithInsensitive = notEndsWithInsensitive;
            return this;
        }

        public Builder setLike(String like) {
            this.like = like;
            return this;
        }

        public Builder setNotLike(String notLike) {
            this.notLike = notLike;
            return this;
        }

        public Builder setLikeInsensitive(String likeInsensitive) {
            this.likeInsensitive = likeInsensitive;
            return this;
        }

        public Builder setNotLikeInsensitive(String notLikeInsensitive) {
            this.notLikeInsensitive = notLikeInsensitive;
            return this;
        }

        public Builder setEqualToInsensitive(String equalToInsensitive) {
            this.equalToInsensitive = equalToInsensitive;
            return this;
        }

        public Builder setNotEqualToInsensitive(String notEqualToInsensitive) {
            this.notEqualToInsensitive = notEqualToInsensitive;
            return this;
        }

        public Builder setDistinctFromInsensitive(String distinctFromInsensitive) {
            this.distinctFromInsensitive = distinctFromInsensitive;
            return this;
        }

        public Builder setNotDistinctFromInsensitive(String notDistinctFromInsensitive) {
            this.notDistinctFromInsensitive = notDistinctFromInsensitive;
            return this;
        }

        public Builder setInInsensitive(java.util.List<String> inInsensitive) {
            this.inInsensitive = inInsensitive;
            return this;
        }

        public Builder setNotInInsensitive(java.util.List<String> notInInsensitive) {
            this.notInInsensitive = notInInsensitive;
            return this;
        }

        public Builder setLessThanInsensitive(String lessThanInsensitive) {
            this.lessThanInsensitive = lessThanInsensitive;
            return this;
        }

        public Builder setLessThanOrEqualToInsensitive(String lessThanOrEqualToInsensitive) {
            this.lessThanOrEqualToInsensitive = lessThanOrEqualToInsensitive;
            return this;
        }

        public Builder setGreaterThanInsensitive(String greaterThanInsensitive) {
            this.greaterThanInsensitive = greaterThanInsensitive;
            return this;
        }

        public Builder setGreaterThanOrEqualToInsensitive(String greaterThanOrEqualToInsensitive) {
            this.greaterThanOrEqualToInsensitive = greaterThanOrEqualToInsensitive;
            return this;
        }


        public StringFilter build() {
            return new StringFilter(isNull, equalTo, notEqualTo, distinctFrom, notDistinctFrom, in, notIn, lessThan, lessThanOrEqualTo, greaterThan, greaterThanOrEqualTo, includes, notIncludes, includesInsensitive, notIncludesInsensitive, startsWith, notStartsWith, startsWithInsensitive, notStartsWithInsensitive, endsWith, notEndsWith, endsWithInsensitive, notEndsWithInsensitive, like, notLike, likeInsensitive, notLikeInsensitive, equalToInsensitive, notEqualToInsensitive, distinctFromInsensitive, notDistinctFromInsensitive, inInsensitive, notInInsensitive, lessThanInsensitive, lessThanOrEqualToInsensitive, greaterThanInsensitive, greaterThanOrEqualToInsensitive);
        }

    }
}
