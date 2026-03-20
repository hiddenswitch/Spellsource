package com.hiddenswitch.framework.graphql;


public class LoginOrCreateReply implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private AccessToken accessToken;
    private UserEntity userEntity;

    public LoginOrCreateReply() {
    }

    public LoginOrCreateReply(AccessToken accessToken, UserEntity userEntity) {
        this.accessToken = accessToken;
        this.userEntity = userEntity;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }
    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }



    public static LoginOrCreateReply.Builder builder() {
        return new LoginOrCreateReply.Builder();
    }

    public static class Builder {

        private AccessToken accessToken;
        private UserEntity userEntity;

        public Builder() {
        }

        public Builder setAccessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder setUserEntity(UserEntity userEntity) {
            this.userEntity = userEntity;
            return this;
        }


        public LoginOrCreateReply build() {
            return new LoginOrCreateReply(accessToken, userEntity);
        }

    }
}
