package com.hiddenswitch.cluster.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.cluster.models.SimulationConfig;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.common.DeckListParsingException;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulate {

	/**
	 * Sets the default logging level to ERROR to prevent slow log printing.
	 */
	private static void setLogLevelToError() {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
				LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);
	}

	public static void main(String[] args) {
		setLogLevelToError();

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
		final Map<String, Deck> decks = deckPaths.stream()
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
				.filter(Objects::nonNull)
				// Convert to decklists
				.map(deckList -> {
					try {
						return DeckCreateRequest.fromDeckList(deckList);
					} catch (DeckListParsingException e) {
						System.err.println(String.format("Deck Parsing: Failed to parse deck from decklist. \n%s", e.getMessage()));
						return null;
					}
				})
				.filter(Objects::nonNull)
				.map(DeckCreateRequest::toGameDeck)
				// Make a key-value dictionary of the decks
				.collect(Collectors.toConcurrentMap(Deck::getName, Function.identity()));

		if (decks.size() < 2) {
			System.err.println("Simulate: Too few decks were specified. You need at least two decks to generate pairs.");
			return;
		}

		// Create all possible deck to deck matchups
		final List<String[]> combinations = GameContext.getDeckCombinations(new ArrayList<>(decks.keySet()));

		if (!mirrors) {
			combinations.removeIf(pair -> pair[0].equals(pair[1]));
		}

		// Store the progress
		AtomicInteger matchesComplete = new AtomicInteger(0);
		final int totalMatches = number * combinations.size();

		Thread progressThread = null;

		// Printing progress thread
		if (!quiet) {
			progressThread = new Thread(() -> {

				try {
					while (matchesComplete.get() <= totalMatches) {
						Thread.sleep(5000);
						int matchesNow = matchesComplete.get();
						System.err.println(String.format("Progress: %.2f%% (%d/%d matches completed)", (float) matchesNow / (float) totalMatches * 100.0f, matchesNow, totalMatches));
					}
				} catch (InterruptedException e) {
					int matchesNow = matchesComplete.get();
					System.err.println(String.format("Progress: %.2f%% (%d/%d matches completed)", (float) matchesNow / (float) totalMatches * 100.0f, matchesNow, totalMatches));
				}
			});

			progressThread.start();
		}

		// Get the results
		final Map<String[], SimulationResult> results = combinations.stream()
				// Get a map of deck pairs..
				.collect(Collectors.toMap(Function.identity(),
						// ... to simulations, which are parallelized
						deckKeyPair -> {
							// Get a pair of decks
							List<Deck> deckPair = Arrays.stream(deckKeyPair).map(decks::get).collect(Collectors.toList());
							// Run a single simulation on the decks
							return GameContext.simulate(deckPair, behaviourSupplier1, behaviourSupplier2, number, true, matchesComplete);
						}));

		String statsHeaders = Stream.concat(Arrays.stream(Statistic.values()).sorted()
						.map(Enum::name)
						.map(s -> "Player 1 " + s),
				Arrays.stream(Statistic.values()).sorted()
						.map(Enum::name)
						.map(s -> "Player 2 " + s))
				.reduce((str1, str2) -> str1 + "\t" + str2).orElseThrow(NullPointerException::new);

		out.println("Deck 1\tDeck 2\tNumber of Games\t" + statsHeaders);

		// Write the results to the output, or standard out if it's not specified. Output as a TSV
		for (SimulationResult simulation : results.values()) {
			StringBuilder row = new StringBuilder();
			row.append(simulation.getConfig().getPlayerConfig1().getDeck().getName());
			row.append("\t");
			row.append(simulation.getConfig().getPlayerConfig2().getDeck().getName());
			row.append("\t");
			row.append(simulation.getNumberOfGames());
			row.append("\t");
			row.append(Stream.concat(Arrays.stream(Statistic.values()).sorted().map(s -> simulation.getPlayer1Stats().getStats().getOrDefault(s, "")),
					Arrays.stream(Statistic.values()).sorted().map(s -> simulation.getPlayer2Stats().getStats().getOrDefault(s, "")))
					.map(Object::toString)
					.reduce((str1, str2) -> str1 + "\t" + str2).orElseThrow(NullPointerException::new));
			row.append("\n");
			out.print(row.toString());
		}

		out.flush();
		out.close();

		if (progressThread != null) {
			progressThread.interrupt();
		}

	}

}
