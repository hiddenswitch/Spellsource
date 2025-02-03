package com.hiddenswitch.framework.graphql;


/**
 * A `DeckPlayerAttributeTuple` edge in the connection.
 */
public class DeckPlayerAttributeTuplesEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private DeckPlayerAttributeTuple node;

    public DeckPlayerAttributeTuplesEdge() {
    }

    public DeckPlayerAttributeTuplesEdge(String cursor, DeckPlayerAttributeTuple node) {
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
     * The `DeckPlayerAttributeTuple` at the end of the edge.
     */
    public DeckPlayerAttributeTuple getNode() {
        return node;
    }
    /**
     * The `DeckPlayerAttributeTuple` at the end of the edge.
     */
    public void setNode(DeckPlayerAttributeTuple node) {
        this.node = node;
    }



    public static DeckPlayerAttributeTuplesEdge.Builder builder() {
        return new DeckPlayerAttributeTuplesEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private DeckPlayerAttributeTuple node;

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
         * The `DeckPlayerAttributeTuple` at the end of the edge.
         */
        public Builder setNode(DeckPlayerAttributeTuple node) {
            this.node = node;
            return this;
        }


        public DeckPlayerAttributeTuplesEdge build() {
            return new DeckPlayerAttributeTuplesEdge(cursor, node);
        }

    }
}
