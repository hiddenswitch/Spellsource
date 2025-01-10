package com.hiddenswitch.framework.graphql;


public class BotUser implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;

    public BotUser() {
    }

    public BotUser(String nodeId, String id) {
        this.nodeId = nodeId;
        this.id = id;
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



    public static BotUser.Builder builder() {
        return new BotUser.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;

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


        public BotUser build() {
            return new BotUser(nodeId, id);
        }

    }
}
