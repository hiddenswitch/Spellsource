package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `GameUser` values.
 */
public class GameUsersConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<GameUser> nodes;
    private java.util.List<GameUsersEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public GameUsersConnection() {
    }

    public GameUsersConnection(java.util.List<GameUser> nodes, java.util.List<GameUsersEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `GameUser` objects.
     */
    public java.util.List<GameUser> getNodes() {
        return nodes;
    }
    /**
     * A list of `GameUser` objects.
     */
    public void setNodes(java.util.List<GameUser> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `GameUser` and cursor to aid in pagination.
     */
    public java.util.List<GameUsersEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `GameUser` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<GameUsersEdge> edges) {
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
     * The count of *all* `GameUser` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `GameUser` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static GameUsersConnection.Builder builder() {
        return new GameUsersConnection.Builder();
    }

    public static class Builder {

        private java.util.List<GameUser> nodes;
        private java.util.List<GameUsersEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `GameUser` objects.
         */
        public Builder setNodes(java.util.List<GameUser> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `GameUser` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<GameUsersEdge> edges) {
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
         * The count of *all* `GameUser` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public GameUsersConnection build() {
            return new GameUsersConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
