package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateBannedDraftCard` mutation.
 */
public class UpdateBannedDraftCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private BannedDraftCardPatch bannedDraftCardPatch;

    public UpdateBannedDraftCardInput() {
    }

    public UpdateBannedDraftCardInput(String clientMutationId, String nodeId, BannedDraftCardPatch bannedDraftCardPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.bannedDraftCardPatch = bannedDraftCardPatch;
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

    public BannedDraftCardPatch getBannedDraftCardPatch() {
        return bannedDraftCardPatch;
    }
    public void setBannedDraftCardPatch(BannedDraftCardPatch bannedDraftCardPatch) {
        this.bannedDraftCardPatch = bannedDraftCardPatch;
    }



    public static UpdateBannedDraftCardInput.Builder builder() {
        return new UpdateBannedDraftCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private BannedDraftCardPatch bannedDraftCardPatch;

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

        public Builder setBannedDraftCardPatch(BannedDraftCardPatch bannedDraftCardPatch) {
            this.bannedDraftCardPatch = bannedDraftCardPatch;
            return this;
        }


        public UpdateBannedDraftCardInput build() {
            return new UpdateBannedDraftCardInput(clientMutationId, nodeId, bannedDraftCardPatch);
        }

    }
}
