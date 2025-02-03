package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `BannedDraftCard` values.
 */
public class BannedDraftCardsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<BannedDraftCard> nodes;
    private java.util.List<BannedDraftCardsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public BannedDraftCardsConnection() {
    }

    public BannedDraftCardsConnection(java.util.List<BannedDraftCard> nodes, java.util.List<BannedDraftCardsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `BannedDraftCard` objects.
     */
    public java.util.List<BannedDraftCard> getNodes() {
        return nodes;
    }
    /**
     * A list of `BannedDraftCard` objects.
     */
    public void setNodes(java.util.List<BannedDraftCard> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `BannedDraftCard` and cursor to aid in pagination.
     */
    public java.util.List<BannedDraftCardsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `BannedDraftCard` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<BannedDraftCardsEdge> edges) {
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
     * The count of *all* `BannedDraftCard` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `BannedDraftCard` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static BannedDraftCardsConnection.Builder builder() {
        return new BannedDraftCardsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<BannedDraftCard> nodes;
        private java.util.List<BannedDraftCardsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `BannedDraftCard` objects.
         */
        public Builder setNodes(java.util.List<BannedDraftCard> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `BannedDraftCard` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<BannedDraftCardsEdge> edges) {
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
         * The count of *all* `BannedDraftCard` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public BannedDraftCardsConnection build() {
            return new BannedDraftCardsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
