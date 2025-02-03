package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateHardRemovalCard` mutation.
 */
public class UpdateHardRemovalCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String nodeId;
    private HardRemovalCardPatch hardRemovalCardPatch;

    public UpdateHardRemovalCardInput() {
    }

    public UpdateHardRemovalCardInput(String clientMutationId, String nodeId, HardRemovalCardPatch hardRemovalCardPatch) {
        this.clientMutationId = clientMutationId;
        this.nodeId = nodeId;
        this.hardRemovalCardPatch = hardRemovalCardPatch;
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

    public HardRemovalCardPatch getHardRemovalCardPatch() {
        return hardRemovalCardPatch;
    }
    public void setHardRemovalCardPatch(HardRemovalCardPatch hardRemovalCardPatch) {
        this.hardRemovalCardPatch = hardRemovalCardPatch;
    }



    public static UpdateHardRemovalCardInput.Builder builder() {
        return new UpdateHardRemovalCardInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String nodeId;
        private HardRemovalCardPatch hardRemovalCardPatch;

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

        public Builder setHardRemovalCardPatch(HardRemovalCardPatch hardRemovalCardPatch) {
            this.hardRemovalCardPatch = hardRemovalCardPatch;
            return this;
        }


        public UpdateHardRemovalCardInput build() {
            return new UpdateHardRemovalCardInput(clientMutationId, nodeId, hardRemovalCardPatch);
        }

    }
}
