package com.hiddenswitch.framework.graphql;


/**
 * All input for the `setCardsInDeck` mutation.
 */
public class SetCardsInDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String deck;
    private java.util.List<String> cardIds;

    public SetCardsInDeckInput() {
    }

    public SetCardsInDeckInput(String clientMutationId, String deck, java.util.List<String> cardIds) {
        this.clientMutationId = clientMutationId;
        this.deck = deck;
        this.cardIds = cardIds;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getDeck() {
        return deck;
    }
    public void setDeck(String deck) {
        this.deck = deck;
    }

    public java.util.List<String> getCardIds() {
        return cardIds;
    }
    public void setCardIds(java.util.List<String> cardIds) {
        this.cardIds = cardIds;
    }



    public static SetCardsInDeckInput.Builder builder() {
        return new SetCardsInDeckInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String deck;
        private java.util.List<String> cardIds;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDeck(String deck) {
            this.deck = deck;
            return this;
        }

        public Builder setCardIds(java.util.List<String> cardIds) {
            this.cardIds = cardIds;
            return this;
        }


        public SetCardsInDeckInput build() {
            return new SetCardsInDeckInput(clientMutationId, deck, cardIds);
        }

    }
}
