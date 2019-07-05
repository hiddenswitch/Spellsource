#!/usr/bin/env bash
set -e
echo "Make sure mongod is running at localhost:27017 using mkdir -pv .mongo && mongod --dbpath ./.mongo/"
sleep 2
export MONGO_URL=mongodb://localhost:27017/testdb
echo "Clearing database testdb"
mongo testdb --eval "printjson(db.dropDatabase())" > /dev/null
echo "Running tests"

# Configure the gradle command
if test "$CI" = "true" || ! command -v gradle > /dev/null ; then
  export GRADLE_CMD="./gradlew"
else
  export GRADLE_CMD=gradle
fi

${GRADLE_CMD} test