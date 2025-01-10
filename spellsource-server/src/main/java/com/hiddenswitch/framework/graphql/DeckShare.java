package com.hiddenswitch.framework.graphql;


/**
 * indicates a deck shared to a player
 */
public class DeckShare implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String deckId;
    private String shareRecipientId;
    private boolean trashedByRecipient;
    private Deck deckByDeckId;

    public DeckShare() {
    }

    public DeckShare(String nodeId, String deckId, String shareRecipientId, boolean trashedByRecipient, Deck deckByDeckId) {
        this.nodeId = nodeId;
        this.deckId = deckId;
        this.shareRecipientId = shareRecipientId;
        this.trashedByRecipient = trashedByRecipient;
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

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getShareRecipientId() {
        return shareRecipientId;
    }
    public void setShareRecipientId(String shareRecipientId) {
        this.shareRecipientId = shareRecipientId;
    }

    public boolean getTrashedByRecipient() {
        return trashedByRecipient;
    }
    public void setTrashedByRecipient(boolean trashedByRecipient) {
        this.trashedByRecipient = trashedByRecipient;
    }

    /**
     * Reads a single `Deck` that is related to this `DeckShare`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `DeckShare`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }



    public static DeckShare.Builder builder() {
        return new DeckShare.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String deckId;
        private String shareRecipientId;
        private boolean trashedByRecipient;
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

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setShareRecipientId(String shareRecipientId) {
            this.shareRecipientId = shareRecipientId;
            return this;
        }

        public Builder setTrashedByRecipient(boolean trashedByRecipient) {
            this.trashedByRecipient = trashedByRecipient;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `DeckShare`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }


        public DeckShare build() {
            return new DeckShare(nodeId, deckId, shareRecipientId, trashedByRecipient, deckByDeckId);
        }

    }
}
