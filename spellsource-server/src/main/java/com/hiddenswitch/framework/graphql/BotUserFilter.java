package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `BotUser` object types. All fields are combined with a logical ‘and.’
 */
public class BotUserFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private java.util.List<BotUserFilter> and;
    private java.util.List<BotUserFilter> or;
    private BotUserFilter not;

    public BotUserFilter() {
    }

    public BotUserFilter(StringFilter id, java.util.List<BotUserFilter> and, java.util.List<BotUserFilter> or, BotUserFilter not) {
        this.id = id;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getId() {
        return id;
    }
    public void setId(StringFilter id) {
        this.id = id;
    }

    public java.util.List<BotUserFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<BotUserFilter> and) {
        this.and = and;
    }

    public java.util.List<BotUserFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<BotUserFilter> or) {
        this.or = or;
    }

    public BotUserFilter getNot() {
        return not;
    }
    public void setNot(BotUserFilter not) {
        this.not = not;
    }



    public static BotUserFilter.Builder builder() {
        return new BotUserFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private java.util.List<BotUserFilter> and;
        private java.util.List<BotUserFilter> or;
        private BotUserFilter not;

        public Builder() {
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setAnd(java.util.List<BotUserFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<BotUserFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(BotUserFilter not) {
            this.not = not;
            return this;
        }


        public BotUserFilter build() {
            return new BotUserFilter(id, and, or, not);
        }

    }
}
