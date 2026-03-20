package com.hiddenswitch.framework.graphql;


/**
 * ─── Input types ──────────────────────────────────────────────
 */
public class CreateAccountInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String username;
    private String password;
    private Boolean decks;
    private Boolean guest;

    public CreateAccountInput() {
    }

    public CreateAccountInput(String email, String username, String password, Boolean decks, Boolean guest) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.decks = decks;
        this.guest = guest;
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

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getDecks() {
        return decks;
    }
    public void setDecks(Boolean decks) {
        this.decks = decks;
    }

    public Boolean getGuest() {
        return guest;
    }
    public void setGuest(Boolean guest) {
        this.guest = guest;
    }



    public static CreateAccountInput.Builder builder() {
        return new CreateAccountInput.Builder();
    }

    public static class Builder {

        private String email;
        private String username;
        private String password;
        private Boolean decks;
        private Boolean guest;

        public Builder() {
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setDecks(Boolean decks) {
            this.decks = decks;
            return this;
        }

        public Builder setGuest(Boolean guest) {
            this.guest = guest;
            return this;
        }


        public CreateAccountInput build() {
            return new CreateAccountInput(email, username, password, decks, guest);
        }

    }
}
