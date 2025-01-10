package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `BotUser`
 */
public class BotUserInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    public BotUserInput() {
    }

    public BotUserInput(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    public static BotUserInput.Builder builder() {
        return new BotUserInput.Builder();
    }

    public static class Builder {

        private String id;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }


        public BotUserInput build() {
            return new BotUserInput(id);
        }

    }
}
