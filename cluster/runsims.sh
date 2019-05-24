#!/usr/bin/env bash
set -e

# Build the latest cluster code
cd ..
echo Rebuilding executable
./gradlew cluster:shadowJar >/dev/null
cd net/src/main/resources/decklists/current

# Override the logging level. Useful to suppress warnings from cards that appear to have buggy behaviour, but is usually
# just the AI playing a card in a way it would never make sense to play. For example, playing Demonic Project when
# neither player has minions in their hands.
export SPELLSOURCE_LOGGING_LEVEL=ERROR

# Execute the simulation
# Gets a comma-separated list of deck file names.
DECKS=$(ls | paste -s -d, -)
java -cp ../../../../../../cluster/build/libs/cluster-0.8.29.jar com.hiddenswitch.cluster.applications.Simulate --decks "${DECKS}" --number 10000 --output ../temporary_results.tsv
cp ../temporary_results.tsv ../../../../../../cluster/simulations.tsv
rm ../temporary_results.tsv