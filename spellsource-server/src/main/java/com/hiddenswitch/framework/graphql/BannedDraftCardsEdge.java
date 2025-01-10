package com.hiddenswitch.framework.graphql;


/**
 * A `BannedDraftCard` edge in the connection.
 */
public class BannedDraftCardsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private BannedDraftCard node;

    public BannedDraftCardsEdge() {
    }

    public BannedDraftCardsEdge(String cursor, BannedDraftCard node) {
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
     * The `BannedDraftCard` at the end of the edge.
     */
    public BannedDraftCard getNode() {
        return node;
    }
    /**
     * The `BannedDraftCard` at the end of the edge.
     */
    public void setNode(BannedDraftCard node) {
        this.node = node;
    }



    public static BannedDraftCardsEdge.Builder builder() {
        return new BannedDraftCardsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private BannedDraftCard node;

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
         * The `BannedDraftCard` at the end of the edge.
         */
        public Builder setNode(BannedDraftCard node) {
            this.node = node;
            return this;
        }


        public BannedDraftCardsEdge build() {
            return new BannedDraftCardsEdge(cursor, node);
        }

    }
}
