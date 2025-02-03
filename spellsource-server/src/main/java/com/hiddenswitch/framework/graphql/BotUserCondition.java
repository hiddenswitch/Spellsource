package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `BotUser` object types. All fields are tested for equality and combined with a logical ‘and.’
 */
public class BotUserCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    public BotUserCondition() {
    }

    public BotUserCondition(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    public static BotUserCondition.Builder builder() {
        return new BotUserCondition.Builder();
    }

    public static class Builder {

        private String id;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }


        public BotUserCondition build() {
            return new BotUserCondition(id);
        }

    }
}
