package com.hiddenswitch.framework.graphql;


public class AccessToken implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    public AccessToken() {
    }

    public AccessToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }



    public static AccessToken.Builder builder() {
        return new AccessToken.Builder();
    }

    public static class Builder {

        private String token;

        public Builder() {
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }


        public AccessToken build() {
            return new AccessToken(token);
        }

    }
}
