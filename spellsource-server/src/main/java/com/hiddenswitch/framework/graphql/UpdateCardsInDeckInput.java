package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateCardsInDeck` mutation.
 */
public class UpdateCardsInDeckInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private CardsInDeckPatch cardsInDeckPatch;

    public UpdateCardsInDeckInput() {
    }

    public UpdateCardsInDeckInput(String clientMutationId, String nodeId, CardsInDeckPatch cardsInDeckPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.cardsInDeckPatch = cardsInDeckPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public CardsInDeckPatch getCardsInDeckPatch() {
        return cardsInDeckPatch;
    }
    public void setCardsInDeckPatch(CardsInDeckPatch cardsInDeckPatch) {
        this.cardsInDeckPatch = cardsInDeckPatch;
    }



    public static UpdateCardsInDeckInput.Builder builder() {
        return new UpdateCardsInDeckInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private CardsInDeckPatch cardsInDeckPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setCardsInDeckPatch(CardsInDeckPatch cardsInDeckPatch) {
            this.cardsInDeckPatch = cardsInDeckPatch;
            return this;
        }


        public UpdateCardsInDeckInput build() {
            return new UpdateCardsInDeckInput(clientMutationId, nodeId, cardsInDeckPatch);
        }

    }
}
