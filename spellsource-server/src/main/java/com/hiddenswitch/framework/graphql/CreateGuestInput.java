package com.hiddenswitch.framework.graphql;


/**
 * All input for the create `Guest` mutation.
 */
public class CreateGuestInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private GuestInput guest;

    public CreateGuestInput() {
    }

    public CreateGuestInput(String clientMutationId, GuestInput guest) {
        this.clientMutationId = clientMutationId;
        this.guest = guest;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public GuestInput getGuest() {
        return guest;
    }
    public void setGuest(GuestInput guest) {
        this.guest = guest;
    }



    public static CreateGuestInput.Builder builder() {
        return new CreateGuestInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private GuestInput guest;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setGuest(GuestInput guest) {
            this.guest = guest;
            return this;
        }


        public CreateGuestInput build() {
            return new CreateGuestInput(clientMutationId, guest);
        }

    }
}
