FROM phusion/baseimage:0.11

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates locales \
    && echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
    && locale-gen en_US.UTF-8 \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_VERSION jdk-12.0.2+10

RUN set -eux; \
    ARCH="$(dpkg --print-architecture)"; \
    case "${ARCH}" in \
       aarch64|arm64) \
         ESUM='855f046afc5a5230ad6da45a5c811194267acd1748f16b648bfe5710703fe8c6'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_aarch64_linux_hotspot_12.0.2_10.tar.gz'; \
         ;; \
       armhf) \
         ESUM='9fec85826ffb7b2b2cf2853a6ed3e001b528ed5cf13e435cd13026398b5178d8'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_arm_linux_hotspot_12.0.2_10.tar.gz'; \
         ;; \
       ppc64el|ppc64le) \
         ESUM='4b0c9f5cdea1b26d7f079fa6478aceebf1923c947c4209d5709c0869dd71b98f'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_ppc64le_linux_hotspot_12.0.2_10.tar.gz'; \
         ;; \
       s390x) \
         ESUM='9897deeaf7a2c90374fbaca8b3eb8e63267d8fc1863b43b21c0bfc86e4783470'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_s390x_linux_hotspot_12.0.2_10.tar.gz'; \
         ;; \
       amd64|x86_64) \
         ESUM='1202f536984c28d68681d51207a84b6c76e5998579132d3fe1b8085aa6a5f21e'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_x64_linux_hotspot_12.0.2_10.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
    curl -LfsSo /tmp/openjdk.tar.gz ${BINARY_URL}; \
    echo "${ESUM} */tmp/openjdk.tar.gz" | sha256sum -c -; \
    mkdir -p /opt/java/openjdk; \
    cd /opt/java/openjdk; \
    tar -xf /tmp/openjdk.tar.gz --strip-components=1; \
    rm -rf /tmp/openjdk.tar.gz; \
    ldconfig;

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/java/openjdk/bin:$PATH"

# basic smoke test
RUN javac --version; \
    java --version;

ENV SPELLSOURCE_VERSION=0.8.55
ADD ./net/build/libs/net-${SPELLSOURCE_VERSION}.jar /data/net-${SPELLSOURCE_VERSION}.jar

RUN mkdir /etc/service/java
COPY server.sh /etc/service/java/run
RUN chmod +x /etc/service/java/run

RUN mkdir /etc/service/jaegeragent
COPY agent.sh /etc/service/jaegeragent/run
RUN chmod +x /etc/service/jaegeragent/run

COPY --from=jaegertracing/jaeger-agent:1.13 /go/bin/agent-linux /go/bin/agent-linux

# Define working directory.
WORKDIR /data

ENV PORT=80
ENV ATOMIX_PORT=5701
ENV VERTX_CLUSTER_PORT=5710
ENV SPELLSOURCE_BROADCAST=false

EXPOSE 80

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]