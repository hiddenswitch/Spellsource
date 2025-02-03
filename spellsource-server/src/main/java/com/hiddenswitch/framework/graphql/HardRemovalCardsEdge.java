package com.hiddenswitch.framework.graphql;


/**
 * A `HardRemovalCard` edge in the connection.
 */
public class HardRemovalCardsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private HardRemovalCard node;

    public HardRemovalCardsEdge() {
    }

    public HardRemovalCardsEdge(String cursor, HardRemovalCard node) {
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
     * The `HardRemovalCard` at the end of the edge.
     */
    public HardRemovalCard getNode() {
        return node;
    }
    /**
     * The `HardRemovalCard` at the end of the edge.
     */
    public void setNode(HardRemovalCard node) {
        this.node = node;
    }



    public static HardRemovalCardsEdge.Builder builder() {
        return new HardRemovalCardsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private HardRemovalCard node;

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
         * The `HardRemovalCard` at the end of the edge.
         */
        public Builder setNode(HardRemovalCard node) {
            this.node = node;
            return this;
        }


        public HardRemovalCardsEdge build() {
            return new HardRemovalCardsEdge(cursor, node);
        }

    }
}
