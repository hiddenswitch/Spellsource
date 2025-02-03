package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `HardRemovalCard` mutation.
 */
public class CreateHardRemovalCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private HardRemovalCardInput hardRemovalCard;

    public CreateHardRemovalCardInput() {
    }

    public CreateHardRemovalCardInput(String clientMutationId, HardRemovalCardInput hardRemovalCard) {
        this.clientMutationId = clientMutationId;
        this.hardRemovalCard = hardRemovalCard;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public HardRemovalCardInput getHardRemovalCard() {
        return hardRemovalCard;
    }
    public void setHardRemovalCard(HardRemovalCardInput hardRemovalCard) {
        this.hardRemovalCard = hardRemovalCard;
    }



    public static CreateHardRemovalCardInput.Builder builder() {
        return new CreateHardRemovalCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private HardRemovalCardInput hardRemovalCard;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setHardRemovalCard(HardRemovalCardInput hardRemovalCard) {
            this.hardRemovalCard = hardRemovalCard;
            return this;
        }


        public CreateHardRemovalCardInput build() {
            return new CreateHardRemovalCardInput(clientMutationId, hardRemovalCard);
        }

    }
}
