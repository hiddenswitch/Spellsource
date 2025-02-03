package com.hiddenswitch.framework.graphql;


/**
 * A `MatchmakingQueue` edge in the connection.
 */
public class MatchmakingQueuesEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private MatchmakingQueue node;

    public MatchmakingQueuesEdge() {
    }

    public MatchmakingQueuesEdge(String cursor, MatchmakingQueue node) {
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
     * The `MatchmakingQueue` at the end of the edge.
     */
    public MatchmakingQueue getNode() {
        return node;
    }
    /**
     * The `MatchmakingQueue` at the end of the edge.
     */
    public void setNode(MatchmakingQueue node) {
        this.node = node;
    }



    public static MatchmakingQueuesEdge.Builder builder() {
        return new MatchmakingQueuesEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private MatchmakingQueue node;

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
         * The `MatchmakingQueue` at the end of the edge.
         */
        public Builder setNode(MatchmakingQueue node) {
            this.node = node;
            return this;
        }


        public MatchmakingQueuesEdge build() {
            return new MatchmakingQueuesEdge(cursor, node);
        }

    }
}
