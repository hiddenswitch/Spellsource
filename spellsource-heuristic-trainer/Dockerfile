FROM eclipse-temurin:27-jre
ENV MLFLOW_OIDC_ISSUER_URL=https://login.appmana.com/realms/appmana \
    MLFLOW_OIDC_CLIENT_ID=spellsource-mlflow-trainer \
    MLFLOW_OIDC_CLIENT_ASSERTION_FILE=/var/run/secrets/keycloak/assertion
COPY build/libs/spellsource-heuristic-trainer-*.jar /app/trainer.jar
ENTRYPOINT ["java", "--enable-preview", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=85.0", "-jar", "/app/trainer.jar"]
