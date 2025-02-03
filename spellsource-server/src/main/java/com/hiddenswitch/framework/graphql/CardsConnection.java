package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `Card` values.
 */
public class CardsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<Card> nodes;
    private java.util.List<CardsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public CardsConnection() {
    }

    public CardsConnection(java.util.List<Card> nodes, java.util.List<CardsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `Card` objects.
     */
    public java.util.List<Card> getNodes() {
        return nodes;
    }
    /**
     * A list of `Card` objects.
     */
    public void setNodes(java.util.List<Card> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `Card` and cursor to aid in pagination.
     */
    public java.util.List<CardsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `Card` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<CardsEdge> edges) {
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
     * The count of *all* `Card` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `Card` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static CardsConnection.Builder builder() {
        return new CardsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<Card> nodes;
        private java.util.List<CardsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `Card` objects.
         */
        public Builder setNodes(java.util.List<Card> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `Card` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<CardsEdge> edges) {
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
         * The count of *all* `Card` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public CardsConnection build() {
            return new CardsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
