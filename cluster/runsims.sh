#!/usr/bin/env bash
set -e

# Build the latest cluster code
cd ..
./gradlew cluster:shadowJar
cd net/src/main/resources/decklists/current

# Execute the simulation
java -cp ../../../../../../cluster/build/libs/cluster-1.3.0-all.jar com.hiddenswitch.cluster.applications.Simulate --decks "Murloc Paladin.txt","Aggro Paladin.txt","Spiteful Priest.txt","Zoo Warlock.txt","Tempo Rogue.txt","Aggro Hunter.txt","Aggro Druid.txt","Secret Mage.txt","Cube Warlock.txt","Midrange Hunter.txt","Highlander Priest.txt","Big Priest.txt","Elemental Priest.txt","Token Shaman.txt","Dragon Priest.txt","Jade Druid.txt","Demon Warlock.txt","Big Spell Mage.txt","Miracle Rogue.txt","Exodia Mage.txt","Mill Rogue.txt" --number 10000 --output ./temporary_results.tsv
cp ./temporary_results.tsv ../../../../../../cluster/simulations.tsv
rm ./temporary_results.tsv