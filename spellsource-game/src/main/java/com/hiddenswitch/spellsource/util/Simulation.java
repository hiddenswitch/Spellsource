package com.hiddenswitch.spellsource.util;

import com.google.common.collect.ImmutableMap;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckListParsingException;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulation {
	private static Logger LOGGER = LoggerFactory.getLogger(Simulation.class);

	public static List<Supplier<Behaviour>> getBehaviourSuppliers(Map<String, Class<? extends Behaviour>> availableBehaviours, List<String> behaviours) {
		return behaviours.stream()
				.map(availableBehaviours::get)
				.map(behaviourClass -> {
					try {
						Constructor<? extends Behaviour> constructor = behaviourClass.getConstructor();
						// Try to create a new instance
						constructor.newInstance();

						// Now return the supplier.
						return (Supplier<Behaviour>) () -> {
							try {
								return constructor.newInstance();
							} catch (Exception ex) {
								return null;
							}
						};

					} catch (Exception ex) {
						return null;
					}
				}).collect(Collectors.toList());
	}

	public static Map<String, Class<? extends Behaviour>> getAllBehaviours() {
		return ImmutableMap.of(
				"GameStateValueBehaviour", GameStateValueBehaviour.class,
				"PlayRandomBehaviour", PlayRandomBehaviour.class
		);
	}

	public static ConcurrentMap<String, GameDeck> getDecks(List<String> deckLists) {
		return deckLists
				.stream()
				// Convert to deckLists
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
				.collect(Collectors.toConcurrentMap(GameDeck::getName, Function.identity()));
	}

	public static List<String[]> getCombinations(boolean mirrors, Map<String, GameDeck> decks, boolean twoDifferentBehaviours) {
		List<String[]> combinations;
		if (twoDifferentBehaviours) {
			// Combinations with replacement
			combinations = decks.keySet().stream().flatMap(deck1 -> decks.keySet().stream()
					.map(deck2 -> new String[]{deck1, deck2})).collect(Collectors.toList());
		} else {
			// Just include distinct combinations (combinations without replacement)
			combinations = GameContext.getDeckCombinations(new ArrayList<>(decks.keySet()));
		}

		if (!mirrors) {
			combinations.removeIf(pair -> pair[0].equals(pair[1]));
		}
		return combinations;
	}

	public static Map<String[], SimulationResult> getResults(Supplier<Behaviour> behaviourSupplier1, Supplier<Behaviour> behaviourSupplier2, int number, Map<String, GameDeck> decks, List<String[]> combinations, AtomicInteger matchesComplete) {
		// Get the results
		return combinations.stream()
				// Get a map of deck pairs..
				.collect(Collectors.toMap(Function.identity(),
						// ... to simulations, which are parallelized
						deckKeyPair -> {
							// Get a pair of decks
							List<GameDeck> deckPair = Arrays.stream(deckKeyPair).map(decks::get).collect(Collectors.toList());
							// Run a single simulation on the decks
							return GameContext.simulate(deckPair, behaviourSupplier1, behaviourSupplier2, number, true, matchesComplete);
						}));
	}

	public static void writeResults(PrintStream out, Map<String[], SimulationResult> results) {
		String statsHeaders = Stream.concat(Arrays.stream(Statistic.values()).sorted()
						.map(Enum::name)
						.map(s -> "Player 1 " + s),
				Arrays.stream(Statistic.values()).sorted()
						.map(Enum::name)
						.map(s -> "Player 2 " + s))
				.reduce((str1, str2) -> str1 + "\t" + str2).orElseThrow(NullPointerException::new);

		out.println("Deck 1\tDeck 2\tNumber of Games\t" + statsHeaders);

		// Write the results to the output, or standard out if it's not specified. Output as a TSV
		for (Map.Entry<String[], SimulationResult> result : results.entrySet()) {
			SimulationResult simulation = result.getValue();
			StringBuilder row = new StringBuilder();
			row.append(result.getKey()[0]);
			row.append("\t");
			row.append(result.getKey()[1]);
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
	}

	@NotNull
	public static Thread getMonitor(AtomicInteger counter, int total) {
		return Thread.ofVirtual().start(() -> {
			try {
				while (counter.get() <= total) {
					Thread.sleep(5000);
					int matchesNow = counter.get();
					LOGGER.info(String.format("Simulation getMonitor progress: %.2f%% (%d/%d completed)", (float) matchesNow / (float) total * 100.0f, matchesNow, total));
				}
			} catch (InterruptedException e) {
				int matchesNow = counter.get();
				LOGGER.info(String.format("Simulation getMonitor progress: %.2f%% (%d/%d completed)", (float) matchesNow / (float) total * 100.0f, matchesNow, total));
			}
		});
	}
}
