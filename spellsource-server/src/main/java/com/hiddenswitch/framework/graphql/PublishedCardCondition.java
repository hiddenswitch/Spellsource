package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `PublishedCard` object types. All fields are
tested for equality and combined with a logical ‘and.’
 */
public class PublishedCardCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String succession;

    public PublishedCardCondition() {
    }

    public PublishedCardCondition(String id, String succession) {
        this.id = id;
        this.succession = succession;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getSuccession() {
        return succession;
    }
    public void setSuccession(String succession) {
        this.succession = succession;
    }



    public static PublishedCardCondition.Builder builder() {
        return new PublishedCardCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String succession;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setSuccession(String succession) {
            this.succession = succession;
            return this;
        }


        public PublishedCardCondition build() {
            return new PublishedCardCondition(id, succession);
        }

    }
}
