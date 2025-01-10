package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `PublishedCard` values.
 */
public class PublishedCardsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<PublishedCard> nodes;
    private java.util.List<PublishedCardsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public PublishedCardsConnection() {
    }

    public PublishedCardsConnection(java.util.List<PublishedCard> nodes, java.util.List<PublishedCardsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `PublishedCard` objects.
     */
    public java.util.List<PublishedCard> getNodes() {
        return nodes;
    }
    /**
     * A list of `PublishedCard` objects.
     */
    public void setNodes(java.util.List<PublishedCard> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `PublishedCard` and cursor to aid in pagination.
     */
    public java.util.List<PublishedCardsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `PublishedCard` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<PublishedCardsEdge> edges) {
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
     * The count of *all* `PublishedCard` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `PublishedCard` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static PublishedCardsConnection.Builder builder() {
        return new PublishedCardsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<PublishedCard> nodes;
        private java.util.List<PublishedCardsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `PublishedCard` objects.
         */
        public Builder setNodes(java.util.List<PublishedCard> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `PublishedCard` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<PublishedCardsEdge> edges) {
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
         * The count of *all* `PublishedCard` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public PublishedCardsConnection build() {
            return new PublishedCardsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
