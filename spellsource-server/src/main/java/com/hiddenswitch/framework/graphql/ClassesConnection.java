package com.hiddenswitch.framework.graphql;


/**
 * A connection to a list of `Class` values.
 */
public class ClassesConnection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<Class> nodes;
    private java.util.List<ClassesEdge> edges;
    private PageInfo pageInfo;
    private int totalCount;

    public ClassesConnection() {
    }

    public ClassesConnection(java.util.List<Class> nodes, java.util.List<ClassesEdge> edges, PageInfo pageInfo, int totalCount) {
        this.nodes = nodes;
        this.edges = edges;
        this.pageInfo = pageInfo;
        this.totalCount = totalCount;
    }

    /**
     * A list of `Class` objects.
     */
    public java.util.List<Class> getNodes() {
        return nodes;
    }
    /**
     * A list of `Class` objects.
     */
    public void setNodes(java.util.List<Class> nodes) {
        this.nodes = nodes;
    }

    /**
     * A list of edges which contains the `Class` and cursor to aid in pagination.
     */
    public java.util.List<ClassesEdge> getEdges() {
        return edges;
    }
    /**
     * A list of edges which contains the `Class` and cursor to aid in pagination.
     */
    public void setEdges(java.util.List<ClassesEdge> edges) {
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
     * The count of *all* `Class` you could get from the connection.
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * The count of *all* `Class` you could get from the connection.
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }



    public static ClassesConnection.Builder builder() {
        return new ClassesConnection.Builder();
    }

    public static class Builder {

        private java.util.List<Class> nodes;
        private java.util.List<ClassesEdge> edges;
        private PageInfo pageInfo;
        private int totalCount;

        public Builder() {
        }

        /**
         * A list of `Class` objects.
         */
        public Builder setNodes(java.util.List<Class> nodes) {
            this.nodes = nodes;
            return this;
        }

        /**
         * A list of edges which contains the `Class` and cursor to aid in pagination.
         */
        public Builder setEdges(java.util.List<ClassesEdge> edges) {
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
         * The count of *all* `Class` you could get from the connection.
         */
        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }


        public ClassesConnection build() {
            return new ClassesConnection(nodes, edges, pageInfo, totalCount);
        }

    }
}
