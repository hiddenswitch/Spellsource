package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `CardsInDeck`. Fields that are set will be updated.
 */
public class CardsInDeckPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private String cardId;

    public CardsInDeckPatch() {
    }

    public CardsInDeckPatch(String deckId, String cardId) {
        this.deckId = deckId;
        this.cardId = cardId;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static CardsInDeckPatch.Builder builder() {
        return new CardsInDeckPatch.Builder();
    }

    public static class Builder {

        private String deckId;
        private String cardId;

        public Builder() {
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public CardsInDeckPatch build() {
            return new CardsInDeckPatch(deckId, cardId);
        }

    }
}
