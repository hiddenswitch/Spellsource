#!/bin/bash
# PATHS="client/README.md client/docs/DefaultApi.md client/src/main/java/com/hiddenswitch/proto3/net/client/ApiClient.java net/src/main/resources/server.yaml shared/build.gradle"
git apply local.patch
# git update-index --assume-unchanged $PATHS
./gradlew swagger
./makecsharp.sh
./gradlew --continuous  net:run $@
# git update-index --no-assume-unchanged $PATHS
git apply -R local.patch
./gradlew swagger
./makecsharp.sh
