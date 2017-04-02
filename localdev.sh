#!/bin/bash
git apply local.patch
./gradlew net:run
git apply -R local.patch
