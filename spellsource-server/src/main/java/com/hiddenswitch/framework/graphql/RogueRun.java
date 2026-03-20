package com.hiddenswitch.framework.graphql;


/**
 * ─── Rogue types (existing) ──────────────────────────────────
 */
public class RogueRun implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.lang.Long id;

    public RogueRun() {
    }

    public RogueRun(java.lang.Long id) {
        this.id = id;
    }

    public java.lang.Long getId() {
        return id;
    }
    public void setId(java.lang.Long id) {
        this.id = id;
    }



    public static RogueRun.Builder builder() {
        return new RogueRun.Builder();
    }

    public static class Builder {

        private java.lang.Long id;

        public Builder() {
        }

        public Builder setId(java.lang.Long id) {
            this.id = id;
            return this;
        }


        public RogueRun build() {
            return new RogueRun(id);
        }

    }
}
