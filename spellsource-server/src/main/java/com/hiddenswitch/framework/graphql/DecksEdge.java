package com.hiddenswitch.framework.graphql;


/**
 * A `Deck` edge in the connection.
 */
public class DecksEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private Deck node;

    public DecksEdge() {
    }

    public DecksEdge(String cursor, Deck node) {
        this.cursor = cursor;
        this.node = node;
    }

    /**
     * A cursor for use in pagination.
     */
    public String getCursor() {
        return cursor;
    }
    /**
     * A cursor for use in pagination.
     */
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    /**
     * The `Deck` at the end of the edge.
     */
    public Deck getNode() {
        return node;
    }
    /**
     * The `Deck` at the end of the edge.
     */
    public void setNode(Deck node) {
        this.node = node;
    }



    public static DecksEdge.Builder builder() {
        return new DecksEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private Deck node;

        public Builder() {
        }

        /**
         * A cursor for use in pagination.
         */
        public Builder setCursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        /**
         * The `Deck` at the end of the edge.
         */
        public Builder setNode(Deck node) {
            this.node = node;
            return this;
        }


        public DecksEdge build() {
            return new DecksEdge(cursor, node);
        }

    }
}
