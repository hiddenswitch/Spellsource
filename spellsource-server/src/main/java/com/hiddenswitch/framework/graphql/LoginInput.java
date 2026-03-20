package com.hiddenswitch.framework.graphql;


public class LoginInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String usernameOrEmail;
    private String password;

    public LoginInput() {
    }

    public LoginInput(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }



    public static LoginInput.Builder builder() {
        return new LoginInput.Builder();
    }

    public static class Builder {

        private String usernameOrEmail;
        private String password;

        public Builder() {
        }

        public Builder setUsernameOrEmail(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }


        public LoginInput build() {
            return new LoginInput(usernameOrEmail, password);
        }

    }
}
