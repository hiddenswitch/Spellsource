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
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.AccountsTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.BotsTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.BroadcastTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.CardsTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.ConnectionTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.ConversationTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.DeckParsingTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.DeckTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.DraftTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.FriendTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.GamesTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.GatewayTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.InventoryTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.InvitesTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.LogicTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.ModelsTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.PersistenceTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.PythonBridgeTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.ReplayTest
${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.TypeTest

if [[ "$CI" != "true" ]] ; then
  echo "Running local only tests"
  sleep 4
  ${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.ClusterTest
  ${GRADLE_CMD} net:test --tests=com.hiddenswitch.spellsource.SimultaneousGamesTest
fi