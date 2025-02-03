package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `Friend` object types. All fields are combined with a logical ‘and.’
 */
public class FriendFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private StringFilter friend;
    private DatetimeFilter createdAt;
    private java.util.List<FriendFilter> and;
    private java.util.List<FriendFilter> or;
    private FriendFilter not;

    public FriendFilter() {
    }

    public FriendFilter(StringFilter id, StringFilter friend, DatetimeFilter createdAt, java.util.List<FriendFilter> and, java.util.List<FriendFilter> or, FriendFilter not) {
        this.id = id;
        this.friend = friend;
        this.createdAt = createdAt;
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

    public StringFilter getFriend() {
        return friend;
    }
    public void setFriend(StringFilter friend) {
        this.friend = friend;
    }

    public DatetimeFilter getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(DatetimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.List<FriendFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<FriendFilter> and) {
        this.and = and;
    }

    public java.util.List<FriendFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<FriendFilter> or) {
        this.or = or;
    }

    public FriendFilter getNot() {
        return not;
    }
    public void setNot(FriendFilter not) {
        this.not = not;
    }



    public static FriendFilter.Builder builder() {
        return new FriendFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private StringFilter friend;
        private DatetimeFilter createdAt;
        private java.util.List<FriendFilter> and;
        private java.util.List<FriendFilter> or;
        private FriendFilter not;

        public Builder() {
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setFriend(StringFilter friend) {
            this.friend = friend;
            return this;
        }

        public Builder setCreatedAt(DatetimeFilter createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setAnd(java.util.List<FriendFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<FriendFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(FriendFilter not) {
            this.not = not;
            return this;
        }


        public FriendFilter build() {
            return new FriendFilter(id, friend, createdAt, and, or, not);
        }

    }
}
