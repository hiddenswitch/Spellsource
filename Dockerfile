#
# Oracle Java 8 Dockerfile and...
#
# https://github.com/dockerfile/java
# https://github.com/dockerfile/java/tree/master/oracle-java8
#


# Use phusion/baseimage as base image. To make your builds
# reproducible, make sure you lock down to a specific version, not
# to `latest`! See
# https://github.com/phusion/baseimage-docker/blob/master/Changelog.md
# for a list of version numbers.
FROM phusion/baseimage:0.9.22

ADD ./net/build/libs/net-1.3.0-all.jar /data/net-1.3.0-all.jar
ADD ./net/lib/quasar-core-0.7.9-jdk8.jar /data/quasar-core-0.7.9-jdk8.jar

# Ad the daemon for the main java process

RUN mkdir /etc/service/java
COPY server.sh /etc/service/java/run
RUN chmod +x /etc/service/java/run

# Install Java.
ENV     JAVA_VERSION_MAJOR=8
ENV     JAVA_VERSION_MINOR=161

RUN mkdir -p /usr/lib/jvm \
          && cd /usr/lib/jvm \
          && curl -s -L -O -k "www.hiddenswitch.com/jdk-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz" \
          && tar xf jdk-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz \
          && rm jdk-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz \
          && update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR}/bin/java" 1

# Define working directory.
WORKDIR /data

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR}

EXPOSE 80

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]