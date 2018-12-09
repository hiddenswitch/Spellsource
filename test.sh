#!/usr/bin/env bash
set -e
echo "Make sure mongod is running at localhost:27017 using mkdir -pv .mongo && mongod --dbpath ./.mongo/"
sleep 2
export MONGO_URL=mongodb://localhost:27017/testdb
echo "Clearing database testdb"
mongo testdb --eval "printjson(db.dropDatabase())" > /dev/null
echo "Running tests"

if [[ "$CI" = "true" ]] ; then
  export GRADLE_CMD="./gradlew"
else
  export GRADLE_CMD=gradle
fi

${GRADLE_CMD} cards:test
${GRADLE_CMD} game:test
${GRADLE_CMD} net:test