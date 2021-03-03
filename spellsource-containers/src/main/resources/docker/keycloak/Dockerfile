FROM docker.io/bitnami/keycloak:12.0.4-debian-10-r0
COPY keycloak-scrypt.jar /opt/bitnami/keycloak/standalone/deployments/
COPY libkeycloak.sh /opt/bitnami/scripts/libkeycloak.sh
COPY keycloak-env.sh /opt/bitnami/scripts/keycloak-env.sh