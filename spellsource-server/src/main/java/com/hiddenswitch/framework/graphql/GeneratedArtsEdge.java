package com.hiddenswitch.framework.graphql;


/**
 * A `GeneratedArt` edge in the connection.
 */
public class GeneratedArtsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private GeneratedArt node;

    public GeneratedArtsEdge() {
    }

    public GeneratedArtsEdge(String cursor, GeneratedArt node) {
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
     * The `GeneratedArt` at the end of the edge.
     */
    public GeneratedArt getNode() {
        return node;
    }
    /**
     * The `GeneratedArt` at the end of the edge.
     */
    public void setNode(GeneratedArt node) {
        this.node = node;
    }



    public static GeneratedArtsEdge.Builder builder() {
        return new GeneratedArtsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private GeneratedArt node;

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
         * The `GeneratedArt` at the end of the edge.
         */
        public Builder setNode(GeneratedArt node) {
            this.node = node;
            return this;
        }


        public GeneratedArtsEdge build() {
            return new GeneratedArtsEdge(cursor, node);
        }

    }
}
