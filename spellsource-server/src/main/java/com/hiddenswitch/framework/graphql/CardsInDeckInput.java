package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `CardsInDeck`
 */
public class CardsInDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private String cardId;

    public CardsInDeckInput() {
    }

    public CardsInDeckInput(String deckId, String cardId) {
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



    public static CardsInDeckInput.Builder builder() {
        return new CardsInDeckInput.Builder();
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


        public CardsInDeckInput build() {
            return new CardsInDeckInput(deckId, cardId);
        }

    }
}
