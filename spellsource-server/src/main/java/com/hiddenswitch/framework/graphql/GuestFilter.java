package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `Guest` object types. All fields are combined with a logical ‘and.’
 */
public class GuestFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private BigIntFilter id;
    private StringFilter userId;
    private java.util.List<GuestFilter> and;
    private java.util.List<GuestFilter> or;
    private GuestFilter not;

    public GuestFilter() {
    }

    public GuestFilter(BigIntFilter id, StringFilter userId, java.util.List<GuestFilter> and, java.util.List<GuestFilter> or, GuestFilter not) {
        this.id = id;
        this.userId = userId;
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

    public StringFilter getUserId() {
        return userId;
    }
    public void setUserId(StringFilter userId) {
        this.userId = userId;
    }

    public java.util.List<GuestFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<GuestFilter> and) {
        this.and = and;
    }

    public java.util.List<GuestFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<GuestFilter> or) {
        this.or = or;
    }

    public GuestFilter getNot() {
        return not;
    }
    public void setNot(GuestFilter not) {
        this.not = not;
    }



    public static GuestFilter.Builder builder() {
        return new GuestFilter.Builder();
    }

    public static class Builder {

        private BigIntFilter id;
        private StringFilter userId;
        private java.util.List<GuestFilter> and;
        private java.util.List<GuestFilter> or;
        private GuestFilter not;

        public Builder() {
        }

        public Builder setId(BigIntFilter id) {
            this.id = id;
            return this;
        }

        public Builder setUserId(StringFilter userId) {
            this.userId = userId;
            return this;
        }

        public Builder setAnd(java.util.List<GuestFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<GuestFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(GuestFilter not) {
            this.not = not;
            return this;
        }


        public GuestFilter build() {
            return new GuestFilter(id, userId, and, or, not);
        }

    }
}
