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
gradle net:test --tests=com.hiddenswitch.spellsource.AccountsTest
gradle net:test --tests=com.hiddenswitch.spellsource.BotsTest
gradle net:test --tests=com.hiddenswitch.spellsource.BroadcastTest
gradle net:test --tests=com.hiddenswitch.spellsource.CardsTest
# gradle net:test --tests=com.hiddenswitch.spellsource.ClusterTest
gradle net:test --tests=com.hiddenswitch.spellsource.ConnectionTest
gradle net:test --tests=com.hiddenswitch.spellsource.ConversationTest
gradle net:test --tests=com.hiddenswitch.spellsource.DeckParsingTest
gradle net:test --tests=com.hiddenswitch.spellsource.DeckTest
gradle net:test --tests=com.hiddenswitch.spellsource.DraftTest
gradle net:test --tests=com.hiddenswitch.spellsource.FriendTest
gradle net:test --tests=com.hiddenswitch.spellsource.GamesTest
gradle net:test --tests=com.hiddenswitch.spellsource.GatewayTest
gradle net:test --tests=com.hiddenswitch.spellsource.InventoryTest
gradle net:test --tests=com.hiddenswitch.spellsource.InvitesTest
gradle net:test --tests=com.hiddenswitch.spellsource.LogicTest
gradle net:test --tests=com.hiddenswitch.spellsource.ModelsTest
gradle net:test --tests=com.hiddenswitch.spellsource.PersistenceTest
gradle net:test --tests=com.hiddenswitch.spellsource.PythonBridgeTest
gradle net:test --tests=com.hiddenswitch.spellsource.ReplayTest
gradle net:test --tests=com.hiddenswitch.spellsource.SimultaneousGamesTest
gradle net:test --tests=com.hiddenswitch.spellsource.TypeTest
