package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteMatchmakingTicketByUserId` mutation.
 */
public class DeleteMatchmakingTicketByUserIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String userId;

    public DeleteMatchmakingTicketByUserIdInput() {
    }

    public DeleteMatchmakingTicketByUserIdInput(String clientMutationId, String userId) {
        this.clientMutationId = clientMutationId;
        this.userId = userId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }



    public static DeleteMatchmakingTicketByUserIdInput.Builder builder() {
        return new DeleteMatchmakingTicketByUserIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String userId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }


        public DeleteMatchmakingTicketByUserIdInput build() {
            return new DeleteMatchmakingTicketByUserIdInput(clientMutationId, userId);
        }

    }
}
