package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.statistics.Statistic;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Evaluates a candidate FeatureVector by playing games against a Hall of Fame
 * of historically strong opponents.
 * <p>
 * Based on the competitive coevolution approach from García-Sánchez &amp; Tonda
 * (2019) "Optimizing Hearthstone agents using an evolutionary algorithm",
 * adapted for CMA-ES where we cannot do full round-robin within a generation.
 * Instead, we use a Hall of Fame archive (Rosin &amp; Belew 1997) as the opponent set.
 * <p>
 * Matchups are distributed across HoF members and deck pairs. The total game
 * budget stays constant regardless of HoF size — more opponents means fewer
 * games per opponent, but broader coverage.
 * <p>
 * All games are parallelized across available cores.
 */
public class FitnessEvaluator {
	private static final Logger LOG = Logger.getLogger(FitnessEvaluator.class.getName());
	private static final AtomicInteger GAME_THREAD_COUNTER = new AtomicInteger(1);
	private static final int MAX_CONCURRENT_GAMES = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
	private static final Semaphore GAME_CONCURRENCY_LIMITER = new Semaphore(MAX_CONCURRENT_GAMES);
	private static final ExecutorService GAME_EXECUTOR = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "spellsource-trainer-game-" + GAME_THREAD_COUNTER.getAndIncrement());
		thread.setDaemon(true);
		return thread;
	});

	private final int gsvbDepth;
	private final int gsvbTimeout;
	private final long gameTimeoutMs;
	private MlflowReporter mlflow;

	public FitnessEvaluator(int gsvbDepth, int gsvbTimeout, long gameTimeoutMs) {
		this.gsvbDepth = gsvbDepth;
		this.gsvbTimeout = gsvbTimeout;
		this.gameTimeoutMs = gameTimeoutMs;
	}

	public FitnessEvaluator(int gsvbDepth, int gsvbTimeout) {
		this(gsvbDepth, gsvbTimeout, 90_000);
	}

	public void setMlflow(MlflowReporter mlflow) {
		this.mlflow = mlflow;
	}

	/**
	 * A single game task: deck pair, opponent weights, who is candidate (P1 or P2), and deterministic game seed.
	 */
	private record GameTask(GameDeck deck1, GameDeck deck2, FeatureVector opponent, boolean candidateIsPlayer1,
	                        int gameIndex, long gameSeed) {
	}

	/**
	 * Evaluates a candidate against a Hall of Fame of opponents.
	 */
	public EvalResult evaluate(String id, FeatureVector candidate, List<FeatureVector> opponents,
	                           List<GameDeck> allDecks, int matchupsToSample, int gamesPerMatchup) {
		return evaluateWithCrn(id, candidate, opponents, allDecks, matchupsToSample, gamesPerMatchup,
				ThreadLocalRandom.current().nextLong());
	}

	/**
	 * Evaluates a candidate against a list of opponents (e.g. Baseline Fittest + Generation Elites) using
	 * Common Random Numbers (CRN) and paired swap-side games.
	 * <p>
	 * Given the same {@code crnSeed}, every candidate across all pods in a generation faces the exact same
	 * deck matchups, opponent assignments, and per-game RNG seeds, playing both sides (P1 and P2) of each seed.
	 */
	public EvalResult evaluateWithCrn(String id, FeatureVector candidate, List<FeatureVector> opponents,
	                                  List<GameDeck> allDecks, int matchupsToSample, int gamesPerMatchup,
	                                  long crnSeed) {
		long start = System.currentTimeMillis();
		Random rng = new Random(crnSeed);

		// Sample deterministic non-mirror deck pairs for this generation seed
		List<int[]> matchups = sampleMatchups(allDecks.size(), matchupsToSample, rng);

		// Flatten all games across all matchups into a single list.
		// Each pair of games shares the exact same gameSeed with swapped sides (P1 vs P2).
		List<GameTask> allTasks = new ArrayList<>();
		int gameIndex = 0;
		for (int m = 0; m < matchups.size(); m++) {
			int[] matchup = matchups.get(m);
			GameDeck deck1 = allDecks.get(matchup[0]);
			GameDeck deck2 = allDecks.get(matchup[1]);
			FeatureVector opponent = opponents.get(m % opponents.size());

			int pairs = Math.max(1, gamesPerMatchup / 2);
			for (int i = 0; i < pairs; i++) {
				long gameSeed = crnSeed * 6364136223846793005L + (m + 1L) * 1442695040888963407L + i * 997L;
				allTasks.add(new GameTask(deck1, deck2, opponent, true, gameIndex++, gameSeed));
				allTasks.add(new GameTask(deck1, deck2, opponent, false, gameIndex++, gameSeed));
			}
		}

		// Run all games in parallel
		AtomicInteger wins = new AtomicInteger(0);
		AtomicInteger completed = new AtomicInteger(0);
		AtomicInteger timeouts = new AtomicInteger(0);

		allTasks.parallelStream().forEach(task -> {
			boolean acquired = false;
			try {
				GAME_CONCURRENCY_LIMITER.acquire();
				acquired = true;
				FeatureVector p1Weights = task.candidateIsPlayer1 ? candidate : task.opponent;
				FeatureVector p2Weights = task.candidateIsPlayer1 ? task.opponent : candidate;

				GameContext game = GameContext.fromDecks(task.gameSeed, List.of(task.deck1, task.deck2));
				game.setBehaviour(0, createBehaviour(p1Weights));
				game.setBehaviour(1, createBehaviour(p2Weights));

				game.init();

				// Run with per-game timeout using shared daemon thread pool
				Future<?> future = GAME_EXECUTOR.submit(() -> game.resume());
				try {
					future.get(gameTimeoutMs, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
					future.cancel(true);
					timeouts.incrementAndGet();
					LOG.info(String.format("Game timeout: %s game %d (%s vs %s) at turn %d",
							id, task.gameIndex,
							task.deck1.getName(), task.deck2.getName(),
							game.getTurn()));
					if (mlflow != null) {
						try {
							String summary = String.format(
									"{\"evalId\":\"%s\",\"gameIndex\":%d,\"deck1\":\"%s\",\"deck2\":\"%s\",\"turn\":%d,\"seed\":%d}",
									id, task.gameIndex, task.deck1.getName(), task.deck2.getName(), game.getTurn(), task.gameSeed);
							mlflow.logTimeoutTrace(id, task.gameIndex, summary);
						} catch (Exception traceEx) {
							// best effort
						}
					}
					completed.incrementAndGet();
					return;
				}

				boolean candidateWon;
				if (task.candidateIsPlayer1) {
					candidateWon = game.getPlayer1().getStatistics().getLong(Statistic.GAMES_WON) > 0;
				} else {
					candidateWon = game.getPlayer2().getStatistics().getLong(Statistic.GAMES_WON) > 0;
				}
				if (candidateWon) {
					wins.incrementAndGet();
				}
			} catch (Throwable t) {
				// Count as a loss
			} finally {
				if (acquired) {
					GAME_CONCURRENCY_LIMITER.release();
				}
			}
			completed.incrementAndGet();
		});

		int totalGames = completed.get();
		double winRate = totalGames > 0 ? (double) wins.get() / totalGames : 0.5;
		long duration = System.currentTimeMillis() - start;

		String timeoutStr = timeouts.get() > 0 ? String.format(", %d timeouts", timeouts.get()) : "";
		LOG.info(String.format("Evaluated %s: %.1f%% win rate (%d/%d games, %d HoF opponents%s) in %ds",
				id, winRate * 100, wins.get(), totalGames, opponents.size(), timeoutStr, duration / 1000));

		return new EvalResult(id, winRate, totalGames, duration);
	}

	/**
	 * Convenience overload for single-opponent evaluation (used by distributed workers).
	 */
	public EvalResult evaluate(String id, FeatureVector candidate, FeatureVector baseline,
	                           List<GameDeck> allDecks, int matchupsToSample, int gamesPerMatchup) {
		return evaluate(id, candidate, List.of(baseline), allDecks, matchupsToSample, gamesPerMatchup);
	}

	private GameStateValueBehaviour createBehaviour(FeatureVector weights) {
		GameStateValueBehaviour gsvb = new GameStateValueBehaviour(weights, "Trainer") {
			@Override
			public net.demilich.metastone.game.actions.GameAction requestAction(
					GameContext context,
					net.demilich.metastone.game.Player player,
					List<net.demilich.metastone.game.actions.GameAction> validActions) {
				if (Thread.currentThread().isInterrupted()) {
					return validActions.stream()
							.filter(ga -> ga != null && ga.getActionType() == com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType.END_TURN)
							.findFirst().orElse(validActions.get(0));
				}
				return super.requestAction(context, player, validActions);
			}
		};
		gsvb.setMaxDepth(gsvbDepth);
		gsvb.setTimeout(gsvbTimeout);
		gsvb.setLethalTimeout(gsvbTimeout * 2L);
		gsvb.setExpandDepthForLethal(false);
		gsvb.setParallel(false);
		gsvb.setThrowsExceptions(false);
		return gsvb;
	}

	private List<int[]> sampleMatchups(int deckCount, int numMatchups, Random rng) {
		List<int[]> allPairs = new ArrayList<>();
		for (int i = 0; i < deckCount; i++) {
			for (int j = i + 1; j < deckCount; j++) {
				allPairs.add(new int[]{i, j});
			}
		}

		if (allPairs.size() <= numMatchups) {
			return allPairs;
		}

		Collections.shuffle(allPairs, rng);
		return allPairs.subList(0, numMatchups);
	}

	public int getGsvbDepth() {
		return gsvbDepth;
	}

	public int getGsvbTimeout() {
		return gsvbTimeout;
	}

	/**
	 * Creates a FeatureVector from a weight map (feature name → value).
	 */
	public static FeatureVector fromWeightMap(Map<String, Double> weights) {
		FeatureVector fv = new FeatureVector();
		for (var entry : weights.entrySet()) {
			WeightedFeature feature = WeightedFeature.valueOf(entry.getKey());
			fv.set(feature, entry.getValue());
		}
		return fv;
	}

	/**
	 * Converts a FeatureVector to a weight map (feature name → value).
	 */
	public static Map<String, Double> toWeightMap(FeatureVector fv) {
		Map<String, Double> map = new LinkedHashMap<>();
		for (WeightedFeature feature : WeightedFeature.values()) {
			map.put(feature.name(), fv.get(feature));
		}
		return map;
	}
}
