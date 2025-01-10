package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `DeckShare` values.
 */
public class DeckSharesConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<DeckShare> nodes;
    private java.util.List<DeckSharesEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public DeckSharesConnection() {
    }

    public DeckSharesConnection(java.util.List<DeckShare> nodes, java.util.List<DeckSharesEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `DeckShare` objects.
     */
    public java.util.List<DeckShare> getNodes() {
        return nodes;
    }
    /**
     * A list of `DeckShare` objects.
     */
    public void setNodes(java.util.List<DeckShare> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `DeckShare` and cursor to aid in pagination.
     */
    public java.util.List<DeckSharesEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `DeckShare` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<DeckSharesEdge> edges) {
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
     * The count of *all* `DeckShare` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `DeckShare` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static DeckSharesConnection.Builder builder() {
        return new DeckSharesConnection.Builder();
    }

    public static class Builder {

        private java.util.List<DeckShare> nodes;
        private java.util.List<DeckSharesEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `DeckShare` objects.
         */
        public Builder setNodes(java.util.List<DeckShare> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `DeckShare` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<DeckSharesEdge> edges) {
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
         * The count of *all* `DeckShare` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public DeckSharesConnection build() {
            return new DeckSharesConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
