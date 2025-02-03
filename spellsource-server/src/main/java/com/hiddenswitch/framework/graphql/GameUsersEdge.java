package com.hiddenswitch.framework.graphql;


/**
 * A `GameUser` edge in the connection.
 */
public class GameUsersEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private GameUser node;

    public GameUsersEdge() {
    }

    public GameUsersEdge(String cursor, GameUser node) {
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
     * The `GameUser` at the end of the edge.
     */
    public GameUser getNode() {
        return node;
    }
    /**
     * The `GameUser` at the end of the edge.
     */
    public void setNode(GameUser node) {
        this.node = node;
    }



    public static GameUsersEdge.Builder builder() {
        return new GameUsersEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private GameUser node;

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
         * The `GameUser` at the end of the edge.
         */
        public Builder setNode(GameUser node) {
            this.node = node;
            return this;
        }


        public GameUsersEdge build() {
            return new GameUsersEdge(cursor, node);
        }

    }
}
