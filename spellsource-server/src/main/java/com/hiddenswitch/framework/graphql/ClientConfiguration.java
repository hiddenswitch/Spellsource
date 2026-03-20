package com.hiddenswitch.framework.graphql;


public class ClientConfiguration implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String keycloakResetPasswordUrl;
    private String keycloakAccountManagementUrl;
    private String graphQlUrl;

    public ClientConfiguration() {
    }

    public ClientConfiguration(String keycloakResetPasswordUrl, String keycloakAccountManagementUrl, String graphQlUrl) {
        this.keycloakResetPasswordUrl = keycloakResetPasswordUrl;
        this.keycloakAccountManagementUrl = keycloakAccountManagementUrl;
        this.graphQlUrl = graphQlUrl;
    }

    public String getKeycloakResetPasswordUrl() {
        return keycloakResetPasswordUrl;
    }
    public void setKeycloakResetPasswordUrl(String keycloakResetPasswordUrl) {
        this.keycloakResetPasswordUrl = keycloakResetPasswordUrl;
    }

    public String getKeycloakAccountManagementUrl() {
        return keycloakAccountManagementUrl;
    }
    public void setKeycloakAccountManagementUrl(String keycloakAccountManagementUrl) {
        this.keycloakAccountManagementUrl = keycloakAccountManagementUrl;
    }

    public String getGraphQlUrl() {
        return graphQlUrl;
    }
    public void setGraphQlUrl(String graphQlUrl) {
        this.graphQlUrl = graphQlUrl;
    }



    public static ClientConfiguration.Builder builder() {
        return new ClientConfiguration.Builder();
    }

    public static class Builder {

        private String keycloakResetPasswordUrl;
        private String keycloakAccountManagementUrl;
        private String graphQlUrl;

        public Builder() {
        }

        public Builder setKeycloakResetPasswordUrl(String keycloakResetPasswordUrl) {
            this.keycloakResetPasswordUrl = keycloakResetPasswordUrl;
            return this;
        }

        public Builder setKeycloakAccountManagementUrl(String keycloakAccountManagementUrl) {
            this.keycloakAccountManagementUrl = keycloakAccountManagementUrl;
            return this;
        }

        public Builder setGraphQlUrl(String graphQlUrl) {
            this.graphQlUrl = graphQlUrl;
            return this;
        }


        public ClientConfiguration build() {
            return new ClientConfiguration(keycloakResetPasswordUrl, keycloakAccountManagementUrl, graphQlUrl);
        }

    }
}
