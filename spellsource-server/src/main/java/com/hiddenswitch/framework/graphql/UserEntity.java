package com.hiddenswitch.framework.graphql;


/**
 * ─── Account / Auth types ─────────────────────────────────────
 */
public class UserEntity implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String email;
    private String username;
    private String privacyToken;

    public UserEntity() {
    }

    public UserEntity(String id, String email, String username, String privacyToken) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.privacyToken = privacyToken;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrivacyToken() {
        return privacyToken;
    }
    public void setPrivacyToken(String privacyToken) {
        this.privacyToken = privacyToken;
    }



    public static UserEntity.Builder builder() {
        return new UserEntity.Builder();
    }

    public static class Builder {

        private String id;
        private String email;
        private String username;
        private String privacyToken;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPrivacyToken(String privacyToken) {
            this.privacyToken = privacyToken;
            return this;
        }


        public UserEntity build() {
            return new UserEntity(id, email, username, privacyToken);
        }

    }
}
