package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `Guest`
 */
public class GuestInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    public GuestInput() {
    }

    public GuestInput(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public static GuestInput.Builder builder() {
        return new GuestInput.Builder();
    }

    public static class Builder {

        private String userId;

        public Builder() {
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }


        public GuestInput build() {
            return new GuestInput(userId);
        }

    }
}
