package com.hiddenswitch.framework.graphql;


/**
 * Information about pagination in a connection.
 */
public class PageInfo implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private String startCursor;
    private String endCursor;

    public PageInfo() {
    }

    public PageInfo(boolean hasNextPage, boolean hasPreviousPage, String startCursor, String endCursor) {
        this.hasNextPage = hasNextPage;
        this.hasPreviousPage = hasPreviousPage;
        this.startCursor = startCursor;
        this.endCursor = endCursor;
    }

    /**
     * When paginating forwards, are there more items?
     */
    public boolean getHasNextPage() {
        return hasNextPage;
    }
    /**
     * When paginating forwards, are there more items?
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    /**
     * When paginating backwards, are there more items?
     */
    public boolean getHasPreviousPage() {
        return hasPreviousPage;
    }
    /**
     * When paginating backwards, are there more items?
     */
    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    /**
     * When paginating backwards, the cursor to continue.
     */
    public String getStartCursor() {
        return startCursor;
    }
    /**
     * When paginating backwards, the cursor to continue.
     */
    public void setStartCursor(String startCursor) {
        this.startCursor = startCursor;
    }

    /**
     * When paginating forwards, the cursor to continue.
     */
    public String getEndCursor() {
        return endCursor;
    }
    /**
     * When paginating forwards, the cursor to continue.
     */
    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }



    public static PageInfo.Builder builder() {
        return new PageInfo.Builder();
    }

    public static class Builder {

        private boolean hasNextPage;
        private boolean hasPreviousPage;
        private String startCursor;
        private String endCursor;

        public Builder() {
        }

        /**
         * When paginating forwards, are there more items?
         */
        public Builder setHasNextPage(boolean hasNextPage) {
            this.hasNextPage = hasNextPage;
            return this;
        }

        /**
         * When paginating backwards, are there more items?
         */
        public Builder setHasPreviousPage(boolean hasPreviousPage) {
            this.hasPreviousPage = hasPreviousPage;
            return this;
        }

        /**
         * When paginating backwards, the cursor to continue.
         */
        public Builder setStartCursor(String startCursor) {
            this.startCursor = startCursor;
            return this;
        }

        /**
         * When paginating forwards, the cursor to continue.
         */
        public Builder setEndCursor(String endCursor) {
            this.endCursor = endCursor;
            return this;
        }


        public PageInfo build() {
            return new PageInfo(hasNextPage, hasPreviousPage, startCursor, endCursor);
        }

    }
}
