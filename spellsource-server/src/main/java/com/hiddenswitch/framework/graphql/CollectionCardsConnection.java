package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `CollectionCard` values.
 */
public class CollectionCardsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<CollectionCard> nodes;
    private java.util.List<CollectionCardsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public CollectionCardsConnection() {
    }

    public CollectionCardsConnection(java.util.List<CollectionCard> nodes, java.util.List<CollectionCardsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `CollectionCard` objects.
     */
    public java.util.List<CollectionCard> getNodes() {
        return nodes;
    }
    /**
     * A list of `CollectionCard` objects.
     */
    public void setNodes(java.util.List<CollectionCard> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `CollectionCard` and cursor to aid in pagination.
     */
    public java.util.List<CollectionCardsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `CollectionCard` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<CollectionCardsEdge> edges) {
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
     * The count of *all* `CollectionCard` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `CollectionCard` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static CollectionCardsConnection.Builder builder() {
        return new CollectionCardsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<CollectionCard> nodes;
        private java.util.List<CollectionCardsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `CollectionCard` objects.
         */
        public Builder setNodes(java.util.List<CollectionCard> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `CollectionCard` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<CollectionCardsEdge> edges) {
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
         * The count of *all* `CollectionCard` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public CollectionCardsConnection build() {
            return new CollectionCardsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
