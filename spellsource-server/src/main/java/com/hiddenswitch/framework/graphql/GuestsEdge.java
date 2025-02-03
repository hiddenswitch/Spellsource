package com.hiddenswitch.framework.graphql;


/**
 * A `Guest` edge in the connection.
 */
public class GuestsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private Guest node;

    public GuestsEdge() {
    }

    public GuestsEdge(String cursor, Guest node) {
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
     * The `Guest` at the end of the edge.
     */
    public Guest getNode() {
        return node;
    }
    /**
     * The `Guest` at the end of the edge.
     */
    public void setNode(Guest node) {
        this.node = node;
    }



    public static GuestsEdge.Builder builder() {
        return new GuestsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private Guest node;

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
         * The `Guest` at the end of the edge.
         */
        public Builder setNode(Guest node) {
            this.node = node;
            return this;
        }


        public GuestsEdge build() {
            return new GuestsEdge(cursor, node);
        }

    }
}
