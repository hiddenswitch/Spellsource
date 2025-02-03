package com.hiddenswitch.framework.graphql;


public class HardRemovalCard implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String cardId;

    public HardRemovalCard() {
    }

    public HardRemovalCard(String nodeId, String cardId) {
        this.nodeId = nodeId;
        this.cardId = cardId;
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

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static HardRemovalCard.Builder builder() {
        return new HardRemovalCard.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String cardId;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public HardRemovalCard build() {
            return new HardRemovalCard(nodeId, cardId);
        }

    }
}
