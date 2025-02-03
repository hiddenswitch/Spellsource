package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `CardsInDeck` object types. All fields are tested
for equality and combined with a logical ‘and.’
 */
public class CardsInDeckCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String deckId;
    private String cardId;

    public CardsInDeckCondition() {
    }

    public CardsInDeckCondition(String id, String deckId, String cardId) {
        this.id = id;
        this.deckId = deckId;
        this.cardId = cardId;
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

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static CardsInDeckCondition.Builder builder() {
        return new CardsInDeckCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String deckId;
        private String cardId;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public CardsInDeckCondition build() {
            return new CardsInDeckCondition(id, deckId, cardId);
        }

    }
}
