#!/usr/bin/env bash
set -e

if [[ "$CI" != "true" ]] && ! pgrep -x "mongod" > /dev/null ; then
    echo "Make sure mongod is running at localhost:27017 using"
    echo "  mkdir -pv .mongo && mongod --dbpath ./.mongo/"
    exit 1
fi

sleep 2
export MONGO_URL=mongodb://localhost:27017/testdb
echo "Clearing database testdb"
mongo testdb --eval "printjson(db.dropDatabase())" > /dev/null
echo "Running tests"

# Configure the gradle command
if [[ "$CI" = "true" ]] || ! command -v gradle > /dev/null ; then
  export GRADLE_CMD="./gradlew"
else
  export GRADLE_CMD=gradle
fi

${GRADLE_CMD} game:test
${GRADLE_CMD} cards:test
${GRADLE_CMD} hearthstone:test
${GRADLE_CMD} customhearthstone:test
${GRADLE_CMD} net:test