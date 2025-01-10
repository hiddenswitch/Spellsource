package com.hiddenswitch.framework.graphql;


/**
 * All input for the `getUserAttribute` mutation.
 */
public class GetUserAttributeInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String idUser;
    private String attribute;
    private String orDefault;

    public GetUserAttributeInput() {
    }

    public GetUserAttributeInput(String clientMutationId, String idUser, String attribute, String orDefault) {
        this.clientMutationId = clientMutationId;
        this.idUser = idUser;
        this.attribute = attribute;
        this.orDefault = orDefault;
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

    public String getOrDefault() {
        return orDefault;
    }
    public void setOrDefault(String orDefault) {
        this.orDefault = orDefault;
    }



    public static GetUserAttributeInput.Builder builder() {
        return new GetUserAttributeInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String idUser;
        private String attribute;
        private String orDefault;

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

        public Builder setOrDefault(String orDefault) {
            this.orDefault = orDefault;
            return this;
        }


        public GetUserAttributeInput build() {
            return new GetUserAttributeInput(clientMutationId, idUser, attribute, orDefault);
        }

    }
}
