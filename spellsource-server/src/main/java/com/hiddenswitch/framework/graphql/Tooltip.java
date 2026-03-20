package com.hiddenswitch.framework.graphql;


public class Tooltip implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<String> keywords;
    private String text;

    public Tooltip() {
    }

    public Tooltip(java.util.List<String> keywords, String text) {
        this.keywords = keywords;
        this.text = text;
    }

    public java.util.List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(java.util.List<String> keywords) {
        this.keywords = keywords;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }



    public static Tooltip.Builder builder() {
        return new Tooltip.Builder();
    }

    public static class Builder {

        private java.util.List<String> keywords;
        private String text;

        public Builder() {
        }

        public Builder setKeywords(java.util.List<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }


        public Tooltip build() {
            return new Tooltip(keywords, text);
        }

    }
}
