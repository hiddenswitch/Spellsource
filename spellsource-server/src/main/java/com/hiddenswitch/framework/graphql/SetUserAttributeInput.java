package com.hiddenswitch.framework.graphql;


/**
 * All input for the `setUserAttribute` mutation.
 */
public class SetUserAttributeInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String idUser;
    private String attribute;
    private String val;

    public SetUserAttributeInput() {
    }

    public SetUserAttributeInput(String clientMutationId, String idUser, String attribute, String val) {
        this.clientMutationId = clientMutationId;
        this.idUser = idUser;
        this.attribute = attribute;
        this.val = val;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getIdUser() {
        return idUser;
    }
    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getAttribute() {
        return attribute;
    }
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getVal() {
        return val;
    }
    public void setVal(String val) {
        this.val = val;
    }



    public static SetUserAttributeInput.Builder builder() {
        return new SetUserAttributeInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String idUser;
        private String attribute;
        private String val;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setIdUser(String idUser) {
            this.idUser = idUser;
            return this;
        }

        public Builder setAttribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setVal(String val) {
            this.val = val;
            return this;
        }


        public SetUserAttributeInput build() {
            return new SetUserAttributeInput(clientMutationId, idUser, attribute, val);
        }

    }
}
