package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `PublishedCard`. Fields that are set will be updated.
 */
public class PublishedCardPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String succession;

    public PublishedCardPatch() {
    }

    public PublishedCardPatch(String id, String succession) {
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



    public static PublishedCardPatch.Builder builder() {
        return new PublishedCardPatch.Builder();
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


        public PublishedCardPatch build() {
            return new PublishedCardPatch(id, succession);
        }

    }
}
