#!/usr/bin/env bash
set -e
./client.sh

# Build the server
./gradlew clean
./gradlew net:shadowJar

# Package the two necessary files into the right places into a zip file
zip artifact.zip \
    ./Dockerfile \
    ./Dockerrun.aws.json \
    ./net/build/libs/net-1.3.0-all.jar \
    ./net/lib/quasar-core-0.7.9-jdk8.jar \
    ./server.sh