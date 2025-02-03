package com.hiddenswitch.framework.graphql;


public class CardsInDeck implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private Card cardByCardId;
    private String id;
    private String deckId;
    private String cardId;
    private PublishedCard publishedCardByCardId;
    private Deck deckByDeckId;

    public CardsInDeck() {
    }

    public CardsInDeck(String nodeId, Card cardByCardId, String id, String deckId, String cardId, PublishedCard publishedCardByCardId, Deck deckByDeckId) {
        this.nodeId = nodeId;
        this.cardByCardId = cardByCardId;
        this.id = id;
        this.deckId = deckId;
        this.cardId = cardId;
        this.publishedCardByCardId = publishedCardByCardId;
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

    public Card getCardByCardId() {
        return cardByCardId;
    }
    public void setCardByCardId(Card cardByCardId) {
        this.cardByCardId = cardByCardId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
     * deleting a deck deletes all its card references
     */
    public String getDeckId() {
        return deckId;
    }
    /**
     * deleting a deck deletes all its card references
     */
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    /**
     * cannot delete cards that are currently used in decks
     */
    public String getCardId() {
        return cardId;
    }
    /**
     * cannot delete cards that are currently used in decks
     */
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    /**
     * Reads a single `PublishedCard` that is related to this `CardsInDeck`.
     */
    public PublishedCard getPublishedCardByCardId() {
        return publishedCardByCardId;
    }
    /**
     * Reads a single `PublishedCard` that is related to this `CardsInDeck`.
     */
    public void setPublishedCardByCardId(PublishedCard publishedCardByCardId) {
        this.publishedCardByCardId = publishedCardByCardId;
    }

    /**
     * Reads a single `Deck` that is related to this `CardsInDeck`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `CardsInDeck`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }



    public static CardsInDeck.Builder builder() {
        return new CardsInDeck.Builder();
    }

    public static class Builder {

        private String nodeId;
        private Card cardByCardId;
        private String id;
        private String deckId;
        private String cardId;
        private PublishedCard publishedCardByCardId;
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

        public Builder setCardByCardId(Card cardByCardId) {
            this.cardByCardId = cardByCardId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * deleting a deck deletes all its card references
         */
        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        /**
         * cannot delete cards that are currently used in decks
         */
        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }

        /**
         * Reads a single `PublishedCard` that is related to this `CardsInDeck`.
         */
        public Builder setPublishedCardByCardId(PublishedCard publishedCardByCardId) {
            this.publishedCardByCardId = publishedCardByCardId;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `CardsInDeck`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }


        public CardsInDeck build() {
            return new CardsInDeck(nodeId, cardByCardId, id, deckId, cardId, publishedCardByCardId, deckByDeckId);
        }

    }
}
