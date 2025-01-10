package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `Deck` values.
 */
public class DecksConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<Deck> nodes;
    private java.util.List<DecksEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public DecksConnection() {
    }

    public DecksConnection(java.util.List<Deck> nodes, java.util.List<DecksEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `Deck` objects.
     */
    public java.util.List<Deck> getNodes() {
        return nodes;
    }
    /**
     * A list of `Deck` objects.
     */
    public void setNodes(java.util.List<Deck> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `Deck` and cursor to aid in pagination.
     */
    public java.util.List<DecksEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `Deck` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<DecksEdge> edges) {
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
     * The count of *all* `Deck` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `Deck` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static DecksConnection.Builder builder() {
        return new DecksConnection.Builder();
    }

    public static class Builder {

        private java.util.List<Deck> nodes;
        private java.util.List<DecksEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `Deck` objects.
         */
        public Builder setNodes(java.util.List<Deck> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `Deck` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<DecksEdge> edges) {
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
         * The count of *all* `Deck` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public DecksConnection build() {
            return new DecksConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
