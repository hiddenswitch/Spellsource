package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `Game` values.
 */
public class GamesConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<Game> nodes;
    private java.util.List<GamesEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public GamesConnection() {
    }

    public GamesConnection(java.util.List<Game> nodes, java.util.List<GamesEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `Game` objects.
     */
    public java.util.List<Game> getNodes() {
        return nodes;
    }
    /**
     * A list of `Game` objects.
     */
    public void setNodes(java.util.List<Game> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `Game` and cursor to aid in pagination.
     */
    public java.util.List<GamesEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `Game` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<GamesEdge> edges) {
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
     * The count of *all* `Game` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `Game` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static GamesConnection.Builder builder() {
        return new GamesConnection.Builder();
    }

    public static class Builder {

        private java.util.List<Game> nodes;
        private java.util.List<GamesEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `Game` objects.
         */
        public Builder setNodes(java.util.List<Game> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `Game` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<GamesEdge> edges) {
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
         * The count of *all* `Game` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public GamesConnection build() {
            return new GamesConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
