package com.hiddenswitch.cluster.applications;

import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.cluster.models.SimulationConfig;
import com.hiddenswitch.spellsource.util.Simulation;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.statistics.SimulationResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulate {

	public static void main(String[] args) {
		// Configure the simulation from the command line
		final SimulationConfig simulationConfig = new SimulationConfig().fromCommandLine(args);

		// Don't run the simulation if the configuration failed.
		if (!simulationConfig.isValid()) {
			return;
		}

		// Get the behaviours. A supplier is a function (lambda) that takes no arguments and returns a new instance of
		// an object. In this case, this returns new Behaviour objects.
		final Supplier<Behaviour> behaviourSupplier1 = simulationConfig.getBehaviourSupplier1();
		final Supplier<Behaviour> behaviourSupplier2 = simulationConfig.getBehaviourSupplier2();
		// Gets a reference to the print stream used to write the output.
		final PrintStream out = simulationConfig.getOutput() == null ? System.out : simulationConfig.getOutput();
		// The list of filepaths to decks to test.
		final List<String> deckPaths = simulationConfig.getDeckPaths();
		// The number of matches to play per unique deck matchup.
		final int number = simulationConfig.getNumber();
		// Should we suppress progress printing?
		final boolean quiet = simulationConfig.isQuiet();
		// Should we include mirror matchups?
		final boolean mirrors = simulationConfig.playMirrorMatchups();

		// Load all the cards specified in the resources of this JAR
		CardCatalogue.loadCardsFromPackage();

		// Turn the decks into Deck objects
		Stream<String> decklists = deckPaths.stream()
				.distinct()
				.map(str -> str.replace("\\ ", " "))
				// Open the files
				.map(File::new)
				// Read the contents of the files
				.map(file -> {
					try {
						return FileUtils.readFileToString(file);
					} catch (IOException e) {
						System.err.println(String.format("Deck Parsing: Failed to read file %s", file));
						return null;
					}
				})
				// Return null if for some reason we couldn't read the files, and filter the unread ones out
				.filter(Objects::nonNull);

		final Map<String, GameDeck> decks = Simulation.getDecks(decklists.collect(Collectors.toList()));

		if (decks.size() < 2) {
			System.err.println("Simulate: Too few decks were specified. You need at least two decks to generate pairs.");
			return;
		}
		// If the simulation is using two different behaviours, we want every behaviour to have had a chance to play
		// every deck
		boolean twoDifferentBehaviours = simulationConfig.twoDifferentBehaviours();

		// Create all possible deck to deck matchups
		final List<String[]> combinations;

		combinations = Simulation.getCombinations(mirrors, decks, twoDifferentBehaviours);

		// Store the progress
		AtomicInteger matchesComplete = new AtomicInteger(0);
		final int totalMatches = number * combinations.size();

		Strand progressThread = null;

		// Printing progress thread
		if (!quiet) {
			progressThread = Simulation.getMonitor(matchesComplete, totalMatches);
			progressThread.start();
		}

		final Map<String[], SimulationResult> results = Simulation.getResults(behaviourSupplier1, behaviourSupplier2, number, decks, combinations, matchesComplete);

		Simulation.writeResults(out, results);

		if (progressThread != null) {
			progressThread.interrupt();
		}
	}
}
