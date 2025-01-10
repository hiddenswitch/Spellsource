package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `Guest`. Fields that are set will be updated.
 */
public class GuestPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    public GuestPatch() {
    }

    public GuestPatch(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public static GuestPatch.Builder builder() {
        return new GuestPatch.Builder();
    }

    public static class Builder {

        private String userId;

        public Builder() {
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }


        public GuestPatch build() {
            return new GuestPatch(userId);
        }

    }
}
