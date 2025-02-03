package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `MatchmakingTicket` values.
 */
public class MatchmakingTicketsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<MatchmakingTicket> nodes;
    private java.util.List<MatchmakingTicketsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public MatchmakingTicketsConnection() {
    }

    public MatchmakingTicketsConnection(java.util.List<MatchmakingTicket> nodes, java.util.List<MatchmakingTicketsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `MatchmakingTicket` objects.
     */
    public java.util.List<MatchmakingTicket> getNodes() {
        return nodes;
    }
    /**
     * A list of `MatchmakingTicket` objects.
     */
    public void setNodes(java.util.List<MatchmakingTicket> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `MatchmakingTicket` and cursor to aid in pagination.
     */
    public java.util.List<MatchmakingTicketsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `MatchmakingTicket` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<MatchmakingTicketsEdge> edges) {
        this.edges = edges;
    }

    /**
     * Information to aid in pagination.
     */
    public PageInfo getPageInfo() {
        return pageInfo;
    }
    /**
     * Information to aid in pagination.
     */
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    /**
     * The count of *all* `MatchmakingTicket` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `MatchmakingTicket` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static MatchmakingTicketsConnection.Builder builder() {
        return new MatchmakingTicketsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<MatchmakingTicket> nodes;
        private java.util.List<MatchmakingTicketsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `MatchmakingTicket` objects.
         */
        public Builder setNodes(java.util.List<MatchmakingTicket> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `MatchmakingTicket` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<MatchmakingTicketsEdge> edges) {
            this.edges = edges;
            return this;
        }

        /**
         * Information to aid in pagination.
         */
        public Builder setPageInfo(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
            return this;
        }

        /**
         * The count of *all* `MatchmakingTicket` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public MatchmakingTicketsConnection build() {
            return new MatchmakingTicketsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
