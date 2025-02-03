package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `Guest` object types. All fields are tested for equality and combined with a logical ‘and.’
 */
public class GuestCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;

    public GuestCondition() {
    }

    public GuestCondition(String id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public static GuestCondition.Builder builder() {
        return new GuestCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String userId;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }


        public GuestCondition build() {
            return new GuestCondition(id, userId);
        }

    }
}
