package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `MatchmakingQueue` values.
 */
public class MatchmakingQueuesConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<MatchmakingQueue> nodes;
    private java.util.List<MatchmakingQueuesEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public MatchmakingQueuesConnection() {
    }

    public MatchmakingQueuesConnection(java.util.List<MatchmakingQueue> nodes, java.util.List<MatchmakingQueuesEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `MatchmakingQueue` objects.
     */
    public java.util.List<MatchmakingQueue> getNodes() {
        return nodes;
    }
    /**
     * A list of `MatchmakingQueue` objects.
     */
    public void setNodes(java.util.List<MatchmakingQueue> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `MatchmakingQueue` and cursor to aid in pagination.
     */
    public java.util.List<MatchmakingQueuesEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `MatchmakingQueue` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<MatchmakingQueuesEdge> edges) {
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
     * The count of *all* `MatchmakingQueue` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `MatchmakingQueue` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static MatchmakingQueuesConnection.Builder builder() {
        return new MatchmakingQueuesConnection.Builder();
    }

    public static class Builder {

        private java.util.List<MatchmakingQueue> nodes;
        private java.util.List<MatchmakingQueuesEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `MatchmakingQueue` objects.
         */
        public Builder setNodes(java.util.List<MatchmakingQueue> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `MatchmakingQueue` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<MatchmakingQueuesEdge> edges) {
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
         * The count of *all* `MatchmakingQueue` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public MatchmakingQueuesConnection build() {
            return new MatchmakingQueuesConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
