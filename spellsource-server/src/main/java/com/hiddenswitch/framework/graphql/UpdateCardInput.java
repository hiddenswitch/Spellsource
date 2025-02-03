package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateCard` mutation.
 */
public class UpdateCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private CardPatch cardPatch;

    public UpdateCardInput() {
    }

    public UpdateCardInput(String clientMutationId, String nodeId, CardPatch cardPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.cardPatch = cardPatch;
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

    public CardPatch getCardPatch() {
        return cardPatch;
    }
    public void setCardPatch(CardPatch cardPatch) {
        this.cardPatch = cardPatch;
    }



    public static UpdateCardInput.Builder builder() {
        return new UpdateCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private CardPatch cardPatch;

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

        public Builder setCardPatch(CardPatch cardPatch) {
            this.cardPatch = cardPatch;
            return this;
        }


        public UpdateCardInput build() {
            return new UpdateCardInput(clientMutationId, nodeId, cardPatch);
        }

    }
}
