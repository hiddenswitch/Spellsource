package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `GeneratedArt` values.
 */
public class GeneratedArtsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<GeneratedArt> nodes;
    private java.util.List<GeneratedArtsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public GeneratedArtsConnection() {
    }

    public GeneratedArtsConnection(java.util.List<GeneratedArt> nodes, java.util.List<GeneratedArtsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `GeneratedArt` objects.
     */
    public java.util.List<GeneratedArt> getNodes() {
        return nodes;
    }
    /**
     * A list of `GeneratedArt` objects.
     */
    public void setNodes(java.util.List<GeneratedArt> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `GeneratedArt` and cursor to aid in pagination.
     */
    public java.util.List<GeneratedArtsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `GeneratedArt` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<GeneratedArtsEdge> edges) {
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
     * The count of *all* `GeneratedArt` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `GeneratedArt` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static GeneratedArtsConnection.Builder builder() {
        return new GeneratedArtsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<GeneratedArt> nodes;
        private java.util.List<GeneratedArtsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `GeneratedArt` objects.
         */
        public Builder setNodes(java.util.List<GeneratedArt> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `GeneratedArt` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<GeneratedArtsEdge> edges) {
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
         * The count of *all* `GeneratedArt` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public GeneratedArtsConnection build() {
            return new GeneratedArtsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
