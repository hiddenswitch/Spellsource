package com.hiddenswitch.framework.graphql;


/**
 * A `Friend` edge in the connection.
 */
public class FriendsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private Friend node;

    public FriendsEdge() {
    }

    public FriendsEdge(String cursor, Friend node) {
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
     * The `Friend` at the end of the edge.
     */
    public Friend getNode() {
        return node;
    }
    /**
     * The `Friend` at the end of the edge.
     */
    public void setNode(Friend node) {
        this.node = node;
    }



    public static FriendsEdge.Builder builder() {
        return new FriendsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private Friend node;

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
         * The `Friend` at the end of the edge.
         */
        public Builder setNode(Friend node) {
            this.node = node;
            return this;
        }


        public FriendsEdge build() {
            return new FriendsEdge(cursor, node);
        }

    }
}
