package com.hiddenswitch.framework.graphql;


public class Guest implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;
    private String userId;

    public Guest() {
    }

    public Guest(String nodeId, String id, String userId) {
        this.nodeId = nodeId;
        this.id = id;
        this.userId = userId;
    }

    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public String getNodeId() {
        return nodeId;
    }
    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public static Guest.Builder builder() {
        return new Guest.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;
        private String userId;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }


        public Guest build() {
            return new Guest(nodeId, id, userId);
        }

    }
}
