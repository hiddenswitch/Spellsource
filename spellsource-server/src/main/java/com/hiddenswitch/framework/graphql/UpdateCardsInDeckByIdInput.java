package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateCardsInDeckById` mutation.
 */
public class UpdateCardsInDeckByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private CardsInDeckPatch cardsInDeckPatch;

    public UpdateCardsInDeckByIdInput() {
    }

    public UpdateCardsInDeckByIdInput(String clientMutationId, String id, CardsInDeckPatch cardsInDeckPatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.cardsInDeckPatch = cardsInDeckPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public CardsInDeckPatch getCardsInDeckPatch() {
        return cardsInDeckPatch;
    }
    public void setCardsInDeckPatch(CardsInDeckPatch cardsInDeckPatch) {
        this.cardsInDeckPatch = cardsInDeckPatch;
    }



    public static UpdateCardsInDeckByIdInput.Builder builder() {
        return new UpdateCardsInDeckByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private CardsInDeckPatch cardsInDeckPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCardsInDeckPatch(CardsInDeckPatch cardsInDeckPatch) {
            this.cardsInDeckPatch = cardsInDeckPatch;
            return this;
        }


        public UpdateCardsInDeckByIdInput build() {
            return new UpdateCardsInDeckByIdInput(clientMutationId, id, cardsInDeckPatch);
        }

    }
}
