FROM scratch AS jdk-amd64
ADD https://corretto.aws/downloads/latest/amazon-corretto-27-x64-linux-jdk.tar.gz /jdk.tar.gz
ADD https://corretto.aws/downloads/latest_sha256/amazon-corretto-27-x64-linux-jdk.tar.gz /jdk.sha256

FROM scratch AS jdk-arm64
ADD https://corretto.aws/downloads/latest/amazon-corretto-27-aarch64-linux-jdk.tar.gz /jdk.tar.gz
ADD https://corretto.aws/downloads/latest_sha256/amazon-corretto-27-aarch64-linux-jdk.tar.gz /jdk.sha256

FROM jdk-${TARGETARCH} AS jdk

FROM eclipse-temurin:27-jre AS runtime
RUN --mount=type=bind,from=jdk,source=/jdk.tar.gz,target=/tmp/jdk.tar.gz \
    --mount=type=bind,from=jdk,source=/jdk.sha256,target=/tmp/jdk.sha256 \
    printf '%s  /tmp/jdk.tar.gz\n' "$(cat /tmp/jdk.sha256)" | sha256sum --check - \
 && rm -rf "$JAVA_HOME" \
 && mkdir -p "$JAVA_HOME" \
 && tar -xzf /tmp/jdk.tar.gz -C "$JAVA_HOME" --strip-components=1
ENV JAVA_VERSION=27

FROM runtime
ENV MLFLOW_OIDC_ISSUER_URL=https://login.appmana.com/realms/appmana \
    MLFLOW_OIDC_CLIENT_ID=spellsource-mlflow-trainer \
    MLFLOW_OIDC_CLIENT_ASSERTION_FILE=/var/run/secrets/keycloak/assertion
COPY build/libs/spellsource-heuristic-trainer-*.jar /app/trainer.jar
ENTRYPOINT ["java", "--enable-preview", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=85.0", "-jar", "/app/trainer.jar"]
