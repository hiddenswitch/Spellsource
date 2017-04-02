#!/bin/bash
git apply local.patch
./gradlew swagger
makecsharp.sh
./gradlew net:run
git apply -R local.patch
