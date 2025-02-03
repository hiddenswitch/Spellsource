package com.hiddenswitch.framework.graphql;


/**
 * A `CollectionCard` edge in the connection.
 */
public class CollectionCardsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private CollectionCard node;

    public CollectionCardsEdge() {
    }

    public CollectionCardsEdge(String cursor, CollectionCard node) {
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
     * The `CollectionCard` at the end of the edge.
     */
    public CollectionCard getNode() {
        return node;
    }
    /**
     * The `CollectionCard` at the end of the edge.
     */
    public void setNode(CollectionCard node) {
        this.node = node;
    }



    public static CollectionCardsEdge.Builder builder() {
        return new CollectionCardsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private CollectionCard node;

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
         * The `CollectionCard` at the end of the edge.
         */
        public Builder setNode(CollectionCard node) {
            this.node = node;
            return this;
        }


        public CollectionCardsEdge build() {
            return new CollectionCardsEdge(cursor, node);
        }

    }
}
