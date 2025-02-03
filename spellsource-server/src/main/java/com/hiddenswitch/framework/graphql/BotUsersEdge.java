package com.hiddenswitch.framework.graphql;


/**
 * A `BotUser` edge in the connection.
 */
public class BotUsersEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private BotUser node;

    public BotUsersEdge() {
    }

    public BotUsersEdge(String cursor, BotUser node) {
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
     * The `BotUser` at the end of the edge.
     */
    public BotUser getNode() {
        return node;
    }
    /**
     * The `BotUser` at the end of the edge.
     */
    public void setNode(BotUser node) {
        this.node = node;
    }



    public static BotUsersEdge.Builder builder() {
        return new BotUsersEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private BotUser node;

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
         * The `BotUser` at the end of the edge.
         */
        public Builder setNode(BotUser node) {
            this.node = node;
            return this;
        }


        public BotUsersEdge build() {
            return new BotUsersEdge(cursor, node);
        }

    }
}
