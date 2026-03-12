package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.*;

@Command(name = "trainer", mixinStandardHelpOptions = true,
		description = "Spellsource GSVB Heuristic Trainer",
		subcommands = {TrainerMain.LocalCmd.class, TrainerMain.CoordinatorCmd.class, TrainerMain.WorkerCmd.class})
public class TrainerMain {
	private static final Logger LOG = Logger.getLogger(TrainerMain.class.getName());

	public static void main(String[] args) {
		setupLogging();
		int exitCode = new CommandLine(new TrainerMain()).execute(args);
		System.exit(exitCode);
	}

	static abstract class TrainingOptions {
		@Option(names = "--generations", description = "Max CMA-ES generations", defaultValue = "50")
		int generations;

		@Option(names = "--population", description = "Population size", defaultValue = "12")
		int population;

		@Option(names = "--matchups-per-eval", description = "Deck matchups sampled per evaluation", defaultValue = "30")
		int matchupsPerEval;

		@Option(names = "--games-per-matchup", description = "Games per matchup (half as each side)", defaultValue = "4")
		int gamesPerMatchup;

		@Option(names = "--gsvb-depth", description = "GSVB search depth", defaultValue = "2")
		int gsvbDepth;

		@Option(names = "--gsvb-timeout", description = "GSVB timeout ms", defaultValue = "5000")
		int gsvbTimeout;

		@Option(names = "--mlflow-uri", description = "MLflow tracking URI")
		String mlflowUri;

		@Option(names = "--output", description = "Write final weights as Java source file")
		String output;

		@Option(names = "--seed", description = "Random seed for CMA-ES (default: ISLAND_INDEX env or random)")
		Long seed;

		Long getEffectiveSeed() {
			if (seed != null) {
				return seed;
			}
			String islandIndex = System.getenv("ISLAND_INDEX");
			if (islandIndex != null) {
				return Long.parseLong(islandIndex);
			}
			return null;
		}

		@Option(names = "--sigma", description = "Initial CMA-ES step size", defaultValue = "5.0")
		double sigma;
	}

	@Command(name = "local", description = "Run everything in-process")
	static class LocalCmd extends TrainingOptions implements Callable<Integer> {
		@Override
		public Integer call() throws Exception {
			loadCards();

			CardIdMapper mapper = new CardIdMapper();
			DeckPool deckPool = new DeckPool(mapper);
			requireDecks(deckPool);

			FitnessEvaluator evaluator = new FitnessEvaluator(gsvbDepth, gsvbTimeout);
			MlflowReporter mlflow = mlflowUri != null ? new MlflowReporter(mlflowUri) : null;

			try {
				double[] initialPoint = null;
				if (mlflow != null) {
					initialPoint = mlflow.loadBestWeights();
					mlflow.joinTrainingSession(generations, population, matchupsPerEval, gamesPerMatchup, getEffectiveSeed());
				}

				CmaesTrainer trainer = new CmaesTrainer(
						generations, population, matchupsPerEval, gamesPerMatchup,
						deckPool.getDecks(), evaluator, mlflow, null, getEffectiveSeed(), sigma, initialPoint
				);

				FeatureVector best = trainer.train();
				reportResults(best, trainer.getBestWinRate(), output);
			} finally {
				if (mlflow != null) {
					mlflow.close();
				}
			}
			return 0;
		}
	}

	@Command(name = "coordinator", description = "CMA-ES loop, pushes work to Redis")
	static class CoordinatorCmd extends TrainingOptions implements Callable<Integer> {
		@Option(names = "--redis-uri", description = "Redis URI", defaultValue = "redis://localhost:6379")
		String redisUri;

		@Override
		public Integer call() throws Exception {
			loadCards();

			CardIdMapper mapper = new CardIdMapper();
			DeckPool deckPool = new DeckPool(mapper);
			requireDecks(deckPool);

			FitnessEvaluator evaluator = new FitnessEvaluator(gsvbDepth, gsvbTimeout);
			MlflowReporter mlflow = mlflowUri != null ? new MlflowReporter(mlflowUri) : null;
			RedisQueue redis = new RedisQueue(redisUri);

			try {
				double[] initialPoint = null;
				if (mlflow != null) {
					initialPoint = mlflow.loadBestWeights();
					mlflow.joinTrainingSession(generations, population, matchupsPerEval, gamesPerMatchup, getEffectiveSeed());
				}
				redis.clear();

				CmaesTrainer trainer = new CmaesTrainer(
						generations, population, matchupsPerEval, gamesPerMatchup,
						deckPool.getDecks(), evaluator, mlflow, redis, getEffectiveSeed(), sigma, initialPoint
				);

				FeatureVector best = trainer.train();
				reportResults(best, trainer.getBestWinRate(), output);
				redis.signalDone();
			} finally {
				if (mlflow != null) {
					mlflow.close();
				}
				redis.close();
			}
			return 0;
		}
	}

	@Command(name = "worker", description = "Pops work from Redis, runs game simulations")
	static class WorkerCmd implements Callable<Integer> {
		@Option(names = "--gsvb-depth", description = "GSVB search depth", defaultValue = "2")
		int gsvbDepth;

		@Option(names = "--gsvb-timeout", description = "GSVB timeout ms", defaultValue = "5000")
		int gsvbTimeout;

		@Option(names = "--redis-uri", description = "Redis URI", defaultValue = "redis://localhost:6379")
		String redisUri;

		@Override
		public Integer call() throws Exception {
			loadCards();

			CardIdMapper mapper = new CardIdMapper();
			DeckPool deckPool = new DeckPool(mapper);
			requireDecks(deckPool);

			FitnessEvaluator evaluator = new FitnessEvaluator(gsvbDepth, gsvbTimeout);
			RedisQueue redis = new RedisQueue(redisUri);

			LOG.info("Worker started. Waiting for evaluation requests...");

			try {
				while (!redis.isDone()) {
					Optional<EvalRequest> requestOpt = redis.popRequest(Duration.ofSeconds(30));
					if (requestOpt.isEmpty()) {
						continue;
					}

					EvalRequest request = requestOpt.get();
					LOG.info("Processing request: " + request.id());

					FeatureVector candidate = FitnessEvaluator.fromWeightMap(request.weights());
					FeatureVector baseline = FitnessEvaluator.fromWeightMap(request.baselineWeights());

					EvalResult result = evaluator.evaluate(
							request.id(), candidate, baseline,
							deckPool.getDecks(),
							request.matchupsToSample(),
							request.gamesPerMatchup()
					);

					redis.pushResult(result);
				}
			} finally {
				redis.close();
			}

			LOG.info("Worker done (training:done signal received)");
			return 0;
		}
	}

	private static void loadCards() {
		LOG.info("Loading card catalogue...");
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
		LOG.info("Card catalogue loaded");
	}

	private static void requireDecks(DeckPool pool) {
		if (pool.size() < 2) {
			LOG.severe("Need at least 2 valid decks. Found: " + pool.size());
			System.exit(1);
		}
		LOG.info("Deck pool: " + pool.size() + " decks");
	}

	private static void reportResults(FeatureVector best, double winRate, String outputFile) throws IOException {
		LOG.info("Training complete. Best win rate: " + String.format("%.1f%%", winRate * 100));
		LOG.info("Best weights:\n" + best.toString());

		if (outputFile != null) {
			writeJavaSource(best, outputFile);
		} else {
			printJavaSource(best);
		}
	}

	private static void printJavaSource(FeatureVector fv) {
		System.out.println("\n// Paste into FeatureVector.getFittest():");
		System.out.println("public static FeatureVector getFittest() {");
		System.out.println("\tFeatureVector defaultVector = new FeatureVector();");
		for (WeightedFeature feature : WeightedFeature.values()) {
			System.out.printf("\tdefaultVector.set(WeightedFeature.%s, %s);%n",
					feature.name(), formatWeight(fv.get(feature)));
		}
		System.out.println("\treturn defaultVector;");
		System.out.println("}");
	}

	private static void writeJavaSource(FeatureVector fv, String path) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
			writer.println("// Generated by spellsource-heuristic-trainer");
			writer.println("// Paste into FeatureVector.getFittest():");
			writer.println("public static FeatureVector getFittest() {");
			writer.println("\tFeatureVector defaultVector = new FeatureVector();");
			for (WeightedFeature feature : WeightedFeature.values()) {
				writer.printf("\tdefaultVector.set(WeightedFeature.%s, %s);%n",
						feature.name(), formatWeight(fv.get(feature)));
			}
			writer.println("\treturn defaultVector;");
			writer.println("}");
		}
		LOG.info("Wrote weights to " + path);
	}

	private static String formatWeight(double value) {
		if (value == Math.floor(value) && !Double.isInfinite(value)) {
			return String.valueOf((int) value);
		}
		return String.format("%.3f", value);
	}

	private static void setupLogging() {
		Logger root = Logger.getLogger("");
		root.setLevel(Level.INFO);
		for (Handler handler : root.getHandlers()) {
			handler.setLevel(Level.INFO);
			if (handler instanceof ConsoleHandler) {
				handler.setFormatter(new SimpleFormatter() {
					@Override
					public String format(LogRecord record) {
						return String.format("[%1$tT] %2$s: %3$s%n",
								record.getMillis(), record.getLoggerName(), record.getMessage());
					}
				});
			}
		}
	}
}
