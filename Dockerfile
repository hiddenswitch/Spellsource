# Multistage build in Java11 environment
FROM adoptopenjdk:11-jdk-hotspot-bionic
# Adds the specially-crafted context from .dockerignore to here
# The .dockerignore does not copy intermediate files from the host, because they might be compiled on the wrong JDK
ADD ./ /app
WORKDIR /app
RUN ./gradlew --no-daemon net:shadowJar

FROM phusion/baseimage:0.11

# Always get missing locales, libcurl / curl and ca-certificates
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates locales \
    && echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
    && locale-gen en_US.UTF-8 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=adoptopenjdk:11-jdk-hotspot-bionic /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="/opt/java/openjdk/bin:$PATH"

# TODO: Enforce that the file output from our application actually matches this one. The asterisk is a workaround for finding it.
ENV SPELLSOURCE_SHADOWJAR_CLASSIFIER=all
COPY --from=0 /app/net/build/libs/net-*-${SPELLSOURCE_SHADOWJAR_CLASSIFIER}.jar /data/net.jar
# Get the phusion base image docker run scripts
COPY docker/root/etc/service /etc/service
# TODO: Enforce that the jaeger-agent version matches our collector version
COPY --from=jaegertracing/jaeger-agent:1.17.1 /go/bin/agent-linux /go/bin/agent-linux

# Health check for whether the code file was successfully copied and Java is correctly installed
RUN ldconfig; \
    javac --version; \
    java --version; \
    test $(java -cp /data/net.jar com.hiddenswitch.spellsource.net.applications.HealthCheck) = 'OK' || exit 1;

# Define working directory.
WORKDIR /data

# HTTP and WebSocket hosting port
ENV PORT=80
# Port for Atomix, our cluster data manager
ENV ATOMIX_PORT=5701
# Port for TCP connections from the Vertx eventbus
ENV VERTX_CLUSTER_PORT=5710
# Do not launch the UDP broadcaster inside this server instance
ENV SPELLSOURCE_BROADCAST=false

EXPOSE 80

# Use baseimage-docker's init system. See docker/root/etc/service for the run scripts.
CMD ["/sbin/my_init"]