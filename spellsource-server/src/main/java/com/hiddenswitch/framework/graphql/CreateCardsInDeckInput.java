package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `CardsInDeck` mutation.
 */
public class CreateCardsInDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private CardsInDeckInput cardsInDeck;

    public CreateCardsInDeckInput() {
    }

    public CreateCardsInDeckInput(String clientMutationId, CardsInDeckInput cardsInDeck) {
        this.clientMutationId = clientMutationId;
        this.cardsInDeck = cardsInDeck;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public CardsInDeckInput getCardsInDeck() {
        return cardsInDeck;
    }
    public void setCardsInDeck(CardsInDeckInput cardsInDeck) {
        this.cardsInDeck = cardsInDeck;
    }



    public static CreateCardsInDeckInput.Builder builder() {
        return new CreateCardsInDeckInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private CardsInDeckInput cardsInDeck;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setCardsInDeck(CardsInDeckInput cardsInDeck) {
            this.cardsInDeck = cardsInDeck;
            return this;
        }


        public CreateCardsInDeckInput build() {
            return new CreateCardsInDeckInput(clientMutationId, cardsInDeck);
        }

    }
}
