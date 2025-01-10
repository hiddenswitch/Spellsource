package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateHardRemovalCardByCardId` mutation.
 */
public class UpdateHardRemovalCardByCardIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String cardId;
    private HardRemovalCardPatch hardRemovalCardPatch;

    public UpdateHardRemovalCardByCardIdInput() {
    }

    public UpdateHardRemovalCardByCardIdInput(String clientMutationId, String cardId, HardRemovalCardPatch hardRemovalCardPatch) {
        this.clientMutationId = clientMutationId;
        this.cardId = cardId;
        this.hardRemovalCardPatch = hardRemovalCardPatch;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public HardRemovalCardPatch getHardRemovalCardPatch() {
        return hardRemovalCardPatch;
    }
    public void setHardRemovalCardPatch(HardRemovalCardPatch hardRemovalCardPatch) {
        this.hardRemovalCardPatch = hardRemovalCardPatch;
    }



    public static UpdateHardRemovalCardByCardIdInput.Builder builder() {
        return new UpdateHardRemovalCardByCardIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String cardId;
        private HardRemovalCardPatch hardRemovalCardPatch;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder setHardRemovalCardPatch(HardRemovalCardPatch hardRemovalCardPatch) {
            this.hardRemovalCardPatch = hardRemovalCardPatch;
            return this;
        }


        public UpdateHardRemovalCardByCardIdInput build() {
            return new UpdateHardRemovalCardByCardIdInput(clientMutationId, cardId, hardRemovalCardPatch);
        }

    }
}
