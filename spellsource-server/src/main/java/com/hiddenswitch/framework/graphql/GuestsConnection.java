package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `Guest` values.
 */
public class GuestsConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<Guest> nodes;
    private java.util.List<GuestsEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public GuestsConnection() {
    }

    public GuestsConnection(java.util.List<Guest> nodes, java.util.List<GuestsEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `Guest` objects.
     */
    public java.util.List<Guest> getNodes() {
        return nodes;
    }
    /**
     * A list of `Guest` objects.
     */
    public void setNodes(java.util.List<Guest> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `Guest` and cursor to aid in pagination.
     */
    public java.util.List<GuestsEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `Guest` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<GuestsEdge> edges) {
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
     * The count of *all* `Guest` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `Guest` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static GuestsConnection.Builder builder() {
        return new GuestsConnection.Builder();
    }

    public static class Builder {

        private java.util.List<Guest> nodes;
        private java.util.List<GuestsEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `Guest` objects.
         */
        public Builder setNodes(java.util.List<Guest> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `Guest` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<GuestsEdge> edges) {
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
         * The count of *all* `Guest` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public GuestsConnection build() {
            return new GuestsConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
