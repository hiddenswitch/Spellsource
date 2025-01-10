package com.hiddenswitch.framework.graphql;


public class DeckPlayerAttributeTuple implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;
    private String deckId;
    private int attribute;
    private String stringValue;
    private Deck deckByDeckId;

    public DeckPlayerAttributeTuple() {
    }

    public DeckPlayerAttributeTuple(String nodeId, String id, String deckId, int attribute, String stringValue, Deck deckByDeckId) {
        this.nodeId = nodeId;
        this.id = id;
        this.deckId = deckId;
        this.attribute = attribute;
        this.stringValue = stringValue;
        this.deckByDeckId = deckByDeckId;
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

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public int getAttribute() {
        return attribute;
    }
    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public String getStringValue() {
        return stringValue;
    }
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }



    public static DeckPlayerAttributeTuple.Builder builder() {
        return new DeckPlayerAttributeTuple.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;
        private String deckId;
        private int attribute;
        private String stringValue;
        private Deck deckByDeckId;

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

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setAttribute(int attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setStringValue(String stringValue) {
            this.stringValue = stringValue;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `DeckPlayerAttributeTuple`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }


        public DeckPlayerAttributeTuple build() {
            return new DeckPlayerAttributeTuple(nodeId, id, deckId, attribute, stringValue, deckByDeckId);
        }

    }
}
