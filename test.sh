#!/usr/bin/env bash
set -e
echo "Make sure mongod is running at localhost:27017 using mkdir -pv .mongo && mongod --dbpath ./.mongo/"
sleep 2
export MONGO_URL=mongodb://localhost:27017/testdb
echo "Clearing database"
mongo testdb --eval "printjson(db.dropDatabase())" > /dev/null
echo "Running tests"
gradle cards:test
gradle game:test
gradle net:test -Dexclude-tests="**/SimultaneousGamesTest.class,**/ClusterTest.class"
gradle net:test --tests=com.hiddenswitch.spellsource.SimultaneousGamesTest.testSimultaneousGames
gradle net:test --tests=com.hiddenswitch.spellsource.ClusterTest
