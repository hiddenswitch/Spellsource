package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.SimulationResultGenerator;
import com.hiddenswitch.spellsource.util.Simulation;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.decks.GameDeck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.vertx.core.json.JsonObject.mapFrom;

public class PythonBridge {
	/**
	 * The main entry point fo the {@code spellsource} python package's interface with the Java code.
	 */

	private static final Logger logger = LoggerFactory.getLogger(PythonBridge.class);
	private static final Map<String, Class<? extends Behaviour>> BEHAVIOURS = Simulation.getAllBehaviours();
	private static final Map<Long, Thread> JOBS = new ConcurrentHashMap<>();

	public static Supplier<Behaviour> getBehaviourByName(String behaviourName) {
		List<Supplier<Behaviour>> behaviours = Simulation.getBehaviourSuppliers(BEHAVIOURS, Collections.singletonList(behaviourName));
		if (behaviours.isEmpty()) {
			return null;
		}
		return behaviours.get(0);
	}

	/**
	 * Used by the python package to actually start a simulation and receive callbacks about the simulation's progress.
	 *
	 * @param generator
	 * @param deckLists
	 * @param gamesPerBatch
	 * @param behaviours
	 * @param mirrors
	 * @param reduce
	 * @return
	 */
	public static long simulate(SimulationResultGenerator generator, List<String> deckLists, int gamesPerBatch, List<Supplier<Behaviour>> behaviours, boolean mirrors, boolean reduce) {
		final Map<String, GameDeck> decks = Simulation.getDecks(deckLists);
		final List<String[]> combinations = Simulation.getCombinations(mirrors, decks, behaviours.size() >= 2
				&& !behaviours.get(0).get().getClass().equals(behaviours.get(1).get().getClass()));

		Thread job = Executors.defaultThreadFactory().newThread(() -> {
			try {
				for (String[] deckKeyPair : combinations) {
					// Get a pair of decks
					List<GameDeck> deckPair = Arrays.stream(deckKeyPair).map(decks::get).collect(Collectors.toList());
					// Run a single simulation on the decks

					try {
						GameContext.simulate(deckPair, behaviours, gamesPerBatch, reduce, simulationResult -> {
							generator.offer(new JsonObject()
									.put("decks", new JsonArray(Arrays.asList(
											deckKeyPair[0],
											deckKeyPair[1])))
									.put("numberOfGames", simulationResult.getNumberOfGames())
									.put("results", new JsonArray(Arrays.asList(
											mapFrom(simulationResult.getPlayer1Stats().getStats()),
											mapFrom(simulationResult.getPlayer2Stats().getStats()))))
									.encode());
						});
					} catch (InterruptedException e) {
						logger.warn("simulate: Interrupted {} {}", deckKeyPair, e);
						return;
					}
				}
			} finally {
				JOBS.remove(Thread.currentThread().getId());
				generator.stopIteration();
			}
		});

		JOBS.put(job.getId(), job);
		job.start();

		return job.getId();
	}

	public static void terminate(long jobId) {
		JOBS.get(jobId).interrupt();
	}

}
