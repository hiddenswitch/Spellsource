package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `BotUser` values.
 */
public class BotUsersConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<BotUser> nodes;
    private java.util.List<BotUsersEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public BotUsersConnection() {
    }

    public BotUsersConnection(java.util.List<BotUser> nodes, java.util.List<BotUsersEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `BotUser` objects.
     */
    public java.util.List<BotUser> getNodes() {
        return nodes;
    }
    /**
     * A list of `BotUser` objects.
     */
    public void setNodes(java.util.List<BotUser> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `BotUser` and cursor to aid in pagination.
     */
    public java.util.List<BotUsersEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `BotUser` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<BotUsersEdge> edges) {
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
     * The count of *all* `BotUser` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `BotUser` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static BotUsersConnection.Builder builder() {
        return new BotUsersConnection.Builder();
    }

    public static class Builder {

        private java.util.List<BotUser> nodes;
        private java.util.List<BotUsersEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `BotUser` objects.
         */
        public Builder setNodes(java.util.List<BotUser> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `BotUser` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<BotUsersEdge> edges) {
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
         * The count of *all* `BotUser` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public BotUsersConnection build() {
            return new BotUsersConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
