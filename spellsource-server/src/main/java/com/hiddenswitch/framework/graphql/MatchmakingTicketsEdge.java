package com.hiddenswitch.framework.graphql;


/**
 * A `MatchmakingTicket` edge in the connection.
 */
public class MatchmakingTicketsEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private MatchmakingTicket node;

    public MatchmakingTicketsEdge() {
    }

    public MatchmakingTicketsEdge(String cursor, MatchmakingTicket node) {
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
     * The `MatchmakingTicket` at the end of the edge.
     */
    public MatchmakingTicket getNode() {
        return node;
    }
    /**
     * The `MatchmakingTicket` at the end of the edge.
     */
    public void setNode(MatchmakingTicket node) {
        this.node = node;
    }



    public static MatchmakingTicketsEdge.Builder builder() {
        return new MatchmakingTicketsEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private MatchmakingTicket node;

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
         * The `MatchmakingTicket` at the end of the edge.
         */
        public Builder setNode(MatchmakingTicket node) {
            this.node = node;
            return this;
        }


        public MatchmakingTicketsEdge build() {
            return new MatchmakingTicketsEdge(cursor, node);
        }

    }
}
