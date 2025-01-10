package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateCardBySuccession` mutation.
 */
public class UpdateCardBySuccessionInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String succession;
    private CardPatch cardPatch;

    public UpdateCardBySuccessionInput() {
    }

    public UpdateCardBySuccessionInput(String clientMutationId, String succession, CardPatch cardPatch) {
        this.clientMutationId = clientMutationId;
        this.succession = succession;
        this.cardPatch = cardPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getSuccession() {
        return succession;
    }
    public void setSuccession(String succession) {
        this.succession = succession;
    }

    public CardPatch getCardPatch() {
        return cardPatch;
    }
    public void setCardPatch(CardPatch cardPatch) {
        this.cardPatch = cardPatch;
    }



    public static UpdateCardBySuccessionInput.Builder builder() {
        return new UpdateCardBySuccessionInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String succession;
        private CardPatch cardPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setSuccession(String succession) {
            this.succession = succession;
            return this;
        }

        public Builder setCardPatch(CardPatch cardPatch) {
            this.cardPatch = cardPatch;
            return this;
        }


        public UpdateCardBySuccessionInput build() {
            return new UpdateCardBySuccessionInput(clientMutationId, succession, cardPatch);
        }

    }
}
