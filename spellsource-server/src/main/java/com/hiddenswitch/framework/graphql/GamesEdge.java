package com.hiddenswitch.framework.graphql;


/**
 * A `Game` edge in the connection.
 */
public class GamesEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private Game node;

    public GamesEdge() {
    }

    public GamesEdge(String cursor, Game node) {
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
     * The `Game` at the end of the edge.
     */
    public Game getNode() {
        return node;
    }
    /**
     * The `Game` at the end of the edge.
     */
    public void setNode(Game node) {
        this.node = node;
    }



    public static GamesEdge.Builder builder() {
        return new GamesEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private Game node;

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
         * The `Game` at the end of the edge.
         */
        public Builder setNode(Game node) {
            this.node = node;
            return this;
        }


        public GamesEdge build() {
            return new GamesEdge(cursor, node);
        }

    }
}
