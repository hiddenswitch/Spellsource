package com.hiddenswitch.framework.graphql;


/**
 * A `DeckShare` edge in the connection.
 */
public class DeckSharesEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private DeckShare node;

    public DeckSharesEdge() {
    }

    public DeckSharesEdge(String cursor, DeckShare node) {
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
     * The `DeckShare` at the end of the edge.
     */
    public DeckShare getNode() {
        return node;
    }
    /**
     * The `DeckShare` at the end of the edge.
     */
    public void setNode(DeckShare node) {
        this.node = node;
    }



    public static DeckSharesEdge.Builder builder() {
        return new DeckSharesEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private DeckShare node;

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
         * The `DeckShare` at the end of the edge.
         */
        public Builder setNode(DeckShare node) {
            this.node = node;
            return this;
        }


        public DeckSharesEdge build() {
            return new DeckSharesEdge(cursor, node);
        }

    }
}
