package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGuestById` mutation.
 */
public class UpdateGuestByIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String id;
    private GuestPatch guestPatch;

    public UpdateGuestByIdInput() {
    }

    public UpdateGuestByIdInput(String clientMutationId, String id, GuestPatch guestPatch) {
        this.clientMutationId = clientMutationId;
        this.id = id;
        this.guestPatch = guestPatch;
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

    public GuestPatch getGuestPatch() {
        return guestPatch;
    }
    public void setGuestPatch(GuestPatch guestPatch) {
        this.guestPatch = guestPatch;
    }



    public static UpdateGuestByIdInput.Builder builder() {
        return new UpdateGuestByIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String id;
        private GuestPatch guestPatch;

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

        public Builder setGuestPatch(GuestPatch guestPatch) {
            this.guestPatch = guestPatch;
            return this;
        }


        public UpdateGuestByIdInput build() {
            return new UpdateGuestByIdInput(clientMutationId, id, guestPatch);
        }

    }
}
