package com.hiddenswitch.framework.graphql;


/**
 * A `Card` edge in the connection.
 */
public class CardsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private Card node;

    public CardsEdge() {
    }

    public CardsEdge(String cursor, Card node) {
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
     * The `Card` at the end of the edge.
     */
    public Card getNode() {
        return node;
    }
    /**
     * The `Card` at the end of the edge.
     */
    public void setNode(Card node) {
        this.node = node;
    }



    public static CardsEdge.Builder builder() {
        return new CardsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private Card node;

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
         * The `Card` at the end of the edge.
         */
        public Builder setNode(Card node) {
            this.node = node;
            return this;
        }


        public CardsEdge build() {
            return new CardsEdge(cursor, node);
        }

    }
}
