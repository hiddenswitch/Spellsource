package com.hiddenswitch.framework.graphql;


/**
 * A `PublishedCard` edge in the connection.
 */
public class PublishedCardsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private PublishedCard node;

    public PublishedCardsEdge() {
    }

    public PublishedCardsEdge(String cursor, PublishedCard node) {
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
     * The `PublishedCard` at the end of the edge.
     */
    public PublishedCard getNode() {
        return node;
    }
    /**
     * The `PublishedCard` at the end of the edge.
     */
    public void setNode(PublishedCard node) {
        this.node = node;
    }



    public static PublishedCardsEdge.Builder builder() {
        return new PublishedCardsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private PublishedCard node;

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
         * The `PublishedCard` at the end of the edge.
         */
        public Builder setNode(PublishedCard node) {
            this.node = node;
            return this;
        }


        public PublishedCardsEdge build() {
            return new PublishedCardsEdge(cursor, node);
        }

    }
}
