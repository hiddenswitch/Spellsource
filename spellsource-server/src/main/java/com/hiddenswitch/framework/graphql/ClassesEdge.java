package com.hiddenswitch.framework.graphql;


/**
 * A `Class` edge in the connection.
 */
public class ClassesEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private Class node;

    public ClassesEdge() {
    }

    public ClassesEdge(String cursor, Class node) {
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
     * The `Class` at the end of the edge.
     */
    public Class getNode() {
        return node;
    }
    /**
     * The `Class` at the end of the edge.
     */
    public void setNode(Class node) {
        this.node = node;
    }



    public static ClassesEdge.Builder builder() {
        return new ClassesEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private Class node;

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
         * The `Class` at the end of the edge.
         */
        public Builder setNode(Class node) {
            this.node = node;
            return this;
        }


        public ClassesEdge build() {
            return new ClassesEdge(cursor, node);
        }

    }
}
