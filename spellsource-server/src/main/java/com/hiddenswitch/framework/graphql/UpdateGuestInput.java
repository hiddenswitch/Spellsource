package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateGuest` mutation.
 */
public class UpdateGuestInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private GuestPatch guestPatch;

    public UpdateGuestInput() {
    }

    public UpdateGuestInput(String clientMutationId, String nodeId, GuestPatch guestPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.guestPatch = guestPatch;
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

    public GuestPatch getGuestPatch() {
        return guestPatch;
    }
    public void setGuestPatch(GuestPatch guestPatch) {
        this.guestPatch = guestPatch;
    }



    public static UpdateGuestInput.Builder builder() {
        return new UpdateGuestInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private GuestPatch guestPatch;

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

        public Builder setGuestPatch(GuestPatch guestPatch) {
            this.guestPatch = guestPatch;
            return this;
        }


        public UpdateGuestInput build() {
            return new UpdateGuestInput(clientMutationId, nodeId, guestPatch);
        }

    }
}
