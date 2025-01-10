package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `Deck` mutation.
 */
public class CreateDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private DeckInput deck;

    public CreateDeckInput() {
    }

    public CreateDeckInput(String clientMutationId, DeckInput deck) {
        this.clientMutationId = clientMutationId;
        this.deck = deck;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public DeckInput getDeck() {
        return deck;
    }
    public void setDeck(DeckInput deck) {
        this.deck = deck;
    }



    public static CreateDeckInput.Builder builder() {
        return new CreateDeckInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private DeckInput deck;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDeck(DeckInput deck) {
            this.deck = deck;
            return this;
        }


        public CreateDeckInput build() {
            return new CreateDeckInput(clientMutationId, deck);
        }

    }
}
