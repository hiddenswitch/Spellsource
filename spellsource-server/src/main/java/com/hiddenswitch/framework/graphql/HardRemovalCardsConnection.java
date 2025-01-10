package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `HardRemovalCard` values.
 */
public class HardRemovalCardsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<HardRemovalCard> nodes;
    private java.util.List<HardRemovalCardsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public HardRemovalCardsConnection() {
    }

    public HardRemovalCardsConnection(java.util.List<HardRemovalCard> nodes, java.util.List<HardRemovalCardsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `HardRemovalCard` objects.
     */
    public java.util.List<HardRemovalCard> getNodes() {
        return nodes;
    }
    /**
     * A list of `HardRemovalCard` objects.
     */
    public void setNodes(java.util.List<HardRemovalCard> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `HardRemovalCard` and cursor to aid in pagination.
     */
    public java.util.List<HardRemovalCardsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `HardRemovalCard` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<HardRemovalCardsEdge> edges) {
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
     * The count of *all* `HardRemovalCard` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `HardRemovalCard` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static HardRemovalCardsConnection.Builder builder() {
        return new HardRemovalCardsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<HardRemovalCard> nodes;
        private java.util.List<HardRemovalCardsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `HardRemovalCard` objects.
         */
        public Builder setNodes(java.util.List<HardRemovalCard> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `HardRemovalCard` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<HardRemovalCardsEdge> edges) {
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
         * The count of *all* `HardRemovalCard` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public HardRemovalCardsConnection build() {
            return new HardRemovalCardsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
