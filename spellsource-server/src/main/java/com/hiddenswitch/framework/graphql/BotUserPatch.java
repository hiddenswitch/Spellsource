package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `BotUser`. Fields that are set will be updated.
 */
public class BotUserPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    public BotUserPatch() {
    }

    public BotUserPatch(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    public static BotUserPatch.Builder builder() {
        return new BotUserPatch.Builder();
    }

    public static class Builder {

        private String id;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }


        public BotUserPatch build() {
            return new BotUserPatch(id);
        }

    }
}
