#!/usr/bin/env bash
set -e
echo Checking AWS environment
pip install awsebcli >/dev/null
echo Building
./client.sh >/dev/null

# Build the server
./gradlew clean >/dev/null
./gradlew net:shadowJar >/dev/null

cd www
./deploy.sh
cd ..

# Package the two necessary files into the right places into a zip file
zip artifact.zip \
    ./Dockerfile \
    ./Dockerrun.aws.json \
    ./net/build/libs/net-0.8.1-all.jar \
    ./server.sh >/dev/null

eb use metastone-dev >/dev/null
eb deploy --staged
