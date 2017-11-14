#!/usr/bin/env bash

./client.sh

# Build the server
./gradlew clean
./gradlew net:shadowJar