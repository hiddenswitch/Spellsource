package com.hiddenswitch.spellsource.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.impl.util.SimulationResultGenerator;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.Simulation;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.internal.scripts.JO;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.statistics.SimulationResult;
import py4j.GatewayServer;
import py4j.Py4JNetworkException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PythonBridge {
	private static final Map<String, Class<? extends Behaviour>> BEHAVIOURS = Simulation.getAllBehaviours();
	private static final Map<Long, Thread> JOBS = new ConcurrentHashMap<>();
	public static void main(String[] args) {
		Logging.setLoggingLevel(Level.OFF);
		GatewayServer gatewayServer = new GatewayServer();
		try {
			gatewayServer.start();
			System.out.println("{\"status\":\"ready\"}");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> gatewayServer.shutdown(true)));
		} catch (Py4JNetworkException ex) {
			System.out.println(String.format("{\"status\":\"failed\", \"message\": \"%s\"}", ex.getCause().getMessage()));
		}
	}

	public static Supplier<Behaviour> getBehaviourByName(String behaviourName) {
		List<Supplier<Behaviour>> behaviours = Simulation.getBehaviourSuppliers(BEHAVIOURS, Collections.singletonList(behaviourName));
		if (behaviours.isEmpty()) {
			return null;
		}
		return behaviours.get(0);
	}

	public static long simulate(SimulationResultGenerator generator, List<String> deckLists, int gamesPerBatch, List<Supplier<Behaviour>> behaviours, boolean mirrors) {
		final Map<String, Deck> decks = Simulation.getDecks(deckLists);
		final List<String[]> combinations = Simulation.getCombinations(mirrors, decks, behaviours.size() > 2);

		Thread job = Executors.defaultThreadFactory().newThread(() -> {
			try {
				for (String[] deckKeyPair : combinations) {
					// Get a pair of decks
					List<Deck> deckPair = Arrays.stream(deckKeyPair).map(decks::get).collect(Collectors.toList());
					// Run a single simulation on the decks

					try {
						GameContext.simulate(deckPair, behaviours, gamesPerBatch, simulationResult -> {
							generator.offer(new JsonObject()
									.put("decks", new JsonArray(Arrays.asList(
											simulationResult.getConfig().getPlayerConfig1().getDeck().getName(),
											simulationResult.getConfig().getPlayerConfig2().getDeck().getName())))
									.put("numberOfGames", simulationResult.getNumberOfGames())
									.put("results", new JsonArray(Arrays.asList(
											JsonObject.mapFrom(simulationResult.getPlayer1Stats().getStats()),
											JsonObject.mapFrom(simulationResult.getPlayer2Stats().getStats()))))
									.encode());
						});
					} catch (InterruptedException e) {
						return;
					}
				}
			} finally {
				JOBS.remove(Thread.currentThread().getId());
				generator.stopIteration();
			}
		});

		JOBS.put(job.getId(), job);
		job.run();

		return job.getId();
	}

	public static void terminate(long jobId) {
		JOBS.get(jobId).interrupt();
	}
}
