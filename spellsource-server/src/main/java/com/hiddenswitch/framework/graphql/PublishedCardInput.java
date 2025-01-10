package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `PublishedCard`
 */
public class PublishedCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String succession;

    public PublishedCardInput() {
    }

    public PublishedCardInput(String id, String succession) {
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



    public static PublishedCardInput.Builder builder() {
        return new PublishedCardInput.Builder();
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


        public PublishedCardInput build() {
            return new PublishedCardInput(id, succession);
        }

    }
}
