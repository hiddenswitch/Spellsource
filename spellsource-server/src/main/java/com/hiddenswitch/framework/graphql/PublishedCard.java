package com.hiddenswitch.framework.graphql;


public class PublishedCard implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;
    private String succession;
    private Card cardBySuccession;
    private CardsInDecksConnection cardsInDecksByCardId;

    public PublishedCard() {
    }

    public PublishedCard(String nodeId, String id, String succession, Card cardBySuccession, CardsInDecksConnection cardsInDecksByCardId) {
        this.nodeId = nodeId;
        this.id = id;
        this.succession = succession;
        this.cardBySuccession = cardBySuccession;
        this.cardsInDecksByCardId = cardsInDecksByCardId;
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

    public String getSuccession() {
        return succession;
    }
    public void setSuccession(String succession) {
        this.succession = succession;
    }

    /**
     * Reads a single `Card` that is related to this `PublishedCard`.
     */
    public Card getCardBySuccession() {
        return cardBySuccession;
    }
    /**
     * Reads a single `Card` that is related to this `PublishedCard`.
     */
    public void setCardBySuccession(Card cardBySuccession) {
        this.cardBySuccession = cardBySuccession;
    }

    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    public CardsInDecksConnection getCardsInDecksByCardId() {
        return cardsInDecksByCardId;
    }
    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    public void setCardsInDecksByCardId(CardsInDecksConnection cardsInDecksByCardId) {
        this.cardsInDecksByCardId = cardsInDecksByCardId;
    }



    public static PublishedCard.Builder builder() {
        return new PublishedCard.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;
        private String succession;
        private Card cardBySuccession;
        private CardsInDecksConnection cardsInDecksByCardId;

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

        public Builder setSuccession(String succession) {
            this.succession = succession;
            return this;
        }

        /**
         * Reads a single `Card` that is related to this `PublishedCard`.
         */
        public Builder setCardBySuccession(Card cardBySuccession) {
            this.cardBySuccession = cardBySuccession;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `CardsInDeck`.
         */
        public Builder setCardsInDecksByCardId(CardsInDecksConnection cardsInDecksByCardId) {
            this.cardsInDecksByCardId = cardsInDecksByCardId;
            return this;
        }


        public PublishedCard build() {
            return new PublishedCard(nodeId, id, succession, cardBySuccession, cardsInDecksByCardId);
        }

    }
}
