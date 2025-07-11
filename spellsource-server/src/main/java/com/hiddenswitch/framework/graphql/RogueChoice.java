package com.hiddenswitch.framework.graphql;


public class RogueChoice implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.lang.Long id;

    public RogueChoice() {
    }

    public RogueChoice(java.lang.Long id) {
        this.id = id;
    }

    public java.lang.Long getId() {
        return id;
    }
    public void setId(java.lang.Long id) {
        this.id = id;
    }



    public static RogueChoice.Builder builder() {
        return new RogueChoice.Builder();
    }

    public static class Builder {

        private java.lang.Long id;

        public Builder() {
        }

        public Builder setId(java.lang.Long id) {
            this.id = id;
            return this;
        }


        public RogueChoice build() {
            return new RogueChoice(id);
        }

    }
}
