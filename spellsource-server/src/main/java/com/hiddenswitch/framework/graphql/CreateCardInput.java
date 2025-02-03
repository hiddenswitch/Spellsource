package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `Card` mutation.
 */
public class CreateCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private CardInput card;

    public CreateCardInput() {
    }

    public CreateCardInput(String clientMutationId, CardInput card) {
        this.clientMutationId = clientMutationId;
        this.card = card;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public CardInput getCard() {
        return card;
    }
    public void setCard(CardInput card) {
        this.card = card;
    }



    public static CreateCardInput.Builder builder() {
        return new CreateCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private CardInput card;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setCard(CardInput card) {
            this.card = card;
            return this;
        }


        public CreateCardInput build() {
            return new CreateCardInput(clientMutationId, card);
        }

    }
}
