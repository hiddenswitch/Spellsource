package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `DeckPlayerAttributeTuple` values.
 */
public class DeckPlayerAttributeTuplesConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<DeckPlayerAttributeTuple> nodes;
    private java.util.List<DeckPlayerAttributeTuplesEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public DeckPlayerAttributeTuplesConnection() {
    }

    public DeckPlayerAttributeTuplesConnection(java.util.List<DeckPlayerAttributeTuple> nodes, java.util.List<DeckPlayerAttributeTuplesEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `DeckPlayerAttributeTuple` objects.
     */
    public java.util.List<DeckPlayerAttributeTuple> getNodes() {
        return nodes;
    }
    /**
     * A list of `DeckPlayerAttributeTuple` objects.
     */
    public void setNodes(java.util.List<DeckPlayerAttributeTuple> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `DeckPlayerAttributeTuple` and cursor to aid in pagination.
     */
    public java.util.List<DeckPlayerAttributeTuplesEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `DeckPlayerAttributeTuple` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<DeckPlayerAttributeTuplesEdge> edges) {
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
     * The count of *all* `DeckPlayerAttributeTuple` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `DeckPlayerAttributeTuple` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static DeckPlayerAttributeTuplesConnection.Builder builder() {
        return new DeckPlayerAttributeTuplesConnection.Builder();
    }

    public static class Builder {

        private java.util.List<DeckPlayerAttributeTuple> nodes;
        private java.util.List<DeckPlayerAttributeTuplesEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `DeckPlayerAttributeTuple` objects.
         */
        public Builder setNodes(java.util.List<DeckPlayerAttributeTuple> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `DeckPlayerAttributeTuple` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<DeckPlayerAttributeTuplesEdge> edges) {
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
         * The count of *all* `DeckPlayerAttributeTuple` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public DeckPlayerAttributeTuplesConnection build() {
            return new DeckPlayerAttributeTuplesConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
