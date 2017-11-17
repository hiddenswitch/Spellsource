#!/usr/bin/env bash
set -e
./client.sh

# Build the server
./gradlew clean
./gradlew net:shadowJar
