package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.decks.GameDeck;
import org.moeaframework.algorithm.CMAES;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.TypedProperties;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

/**
 * CMA-ES optimization loop for training GSVB heuristic weights,
 * using MOEAFramework's CMA-ES implementation with Hall of Fame evaluation.
 * <p>
 * Based on:
 * - García-Sánchez &amp; Tonda (2019) "Optimizing Hearthstone agents using an
 *   evolutionary algorithm" — competitive coevolution for card game heuristics
 * - Rosin &amp; Belew (1997) "New Methods for Competitive Coevolution" — Hall of
 *   Fame archive to prevent overfitting to a single opponent
 * - Hansen (2016) "The CMA Evolution Strategy: A Tutorial" — IPOP-CMA-ES
 * <p>
 * Fitness = win rate against all Hall of Fame members across sampled deck matchups.
 * The HoF grows as training discovers stronger solutions, so evaluation gets
 * progressively harder and win rates remain meaningful.
 */
public class CmaesTrainer {
	private static final Logger LOG = Logger.getLogger(CmaesTrainer.class.getName());
	private static final int DIMENSION = WeightedFeature.values().length;
	private static final int HOF_MAX_SIZE = 8;

	private final int generations;
	private final int populationSize;
	private final int matchupsPerEval;
	private final int gamesPerMatchup;
	private final List<GameDeck> decks;
	private final FitnessEvaluator evaluator;
	private final MlflowReporter mlflow;
	private final RedisQueue redis;
	private final Long seed;
	private final double sigmaInit;
	private final double[] initialPoint;

	private final HallOfFame hof;
	private FeatureVector bestSoFar;
	private double bestWinRate = 0.0;
	private int evaluationCounter = 0;

	public CmaesTrainer(int generations, int populationSize, int matchupsPerEval, int gamesPerMatchup,
	                     List<GameDeck> decks, FitnessEvaluator evaluator, MlflowReporter mlflow, RedisQueue redis,
	                     Long seed, double sigmaInit) {
		this(generations, populationSize, matchupsPerEval, gamesPerMatchup,
				decks, evaluator, mlflow, redis, seed, sigmaInit, null, 0.0);
	}

	public CmaesTrainer(int generations, int populationSize, int matchupsPerEval, int gamesPerMatchup,
	                     List<GameDeck> decks, FitnessEvaluator evaluator, MlflowReporter mlflow, RedisQueue redis,
	                     Long seed, double sigmaInit, double[] initialPoint, double restoredBestWinRate) {
		this.generations = generations;
		this.populationSize = populationSize;
		this.matchupsPerEval = matchupsPerEval;
		this.gamesPerMatchup = gamesPerMatchup;
		this.decks = decks;
		this.evaluator = evaluator;
		this.mlflow = mlflow;
		this.redis = redis;
		this.seed = seed;
		this.sigmaInit = sigmaInit;
		this.initialPoint = initialPoint;

		this.hof = new HallOfFame(HOF_MAX_SIZE);
		hof.seed(FeatureVector.getFittest());
		// If resuming from MLflow, seed HoF with the loaded best weights too
		if (initialPoint != null && initialPoint.length == DIMENSION) {
			FeatureVector loaded = arrayToFeatureVector(initialPoint);
			hof.add(loaded);
			this.bestSoFar = loaded.clone();
			this.bestWinRate = restoredBestWinRate;
		} else {
			this.bestSoFar = FeatureVector.getFittest();
		}
	}

	private static final int STAGNATION_LIMIT = 5; // generations without improvement before IPOP restart

	/**
	 * Runs IPOP-CMA-ES with inter-island migration.
	 * Outer loop restarts CMA-ES with doubled population on stagnation.
	 * Each generation checks MLflow for better solutions from other islands.
	 */
	public FeatureVector train() {
		WeightedFeature[] features = WeightedFeature.values();

		// Starting point
		double[] startPoint = new double[DIMENSION];
		if (initialPoint != null && initialPoint.length == DIMENSION) {
			System.arraycopy(initialPoint, 0, startPoint, 0, DIMENSION);
		} else {
			FeatureVector defaultWeights = FeatureVector.getFittest();
			for (int i = 0; i < DIMENSION; i++) {
				startPoint[i] = defaultWeights.get(features[i]);
			}
		}

		if (seed != null) {
			PRNG.setSeed(seed);
		}

		int totalBudget = generations * populationSize;
		int currentPopulation = populationSize;
		int restartCount = 0;

		LOG.info(String.format("Starting IPOP-CMA-ES with HoF evaluation: %d dimensions, population=%d, budget=%d, sigma=%.1f, seed=%s, startPoint=%s, hofMaxSize=%d",
				DIMENSION, populationSize, totalBudget, sigmaInit,
				seed != null ? seed.toString() : "random",
				initialPoint != null ? "mlflow_best" : "getFittest()",
				HOF_MAX_SIZE));

		while (evaluationCounter < totalBudget) {
			int gensWithoutImprovement = 0;
			int remainingBudget = totalBudget - evaluationCounter;
			if (remainingBudget < currentPopulation) {
				break;
			}

			// Use current best as start point for restarts
			double[] currentStart = bestWinRate > 0 ? getBestWeightsArray() : startPoint;

			GsvbProblem problem = new GsvbProblem();
			CMAES optimizer = new CMAES(problem, currentPopulation, null, new NondominatedPopulation());

			TypedProperties props = new TypedProperties();
			props.setDouble("sigma", sigmaInit);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < currentStart.length; i++) {
				if (i > 0) sb.append(',');
				sb.append(currentStart[i]);
			}
			props.setString("initialSearchPoint", sb.toString());
			optimizer.applyConfiguration(props);

			LOG.info(String.format("CMA-ES run #%d: population=%d, remaining budget=%d",
					restartCount, currentPopulation, remainingBudget));

			optimizer.step(); // initialize

			while (evaluationCounter < totalBudget) {
				double bestBeforeGen = bestWinRate;
				optimizer.step(); // one generation = currentPopulation evaluations

				// Check stagnation
				if (bestWinRate <= bestBeforeGen) {
					gensWithoutImprovement++;
				} else {
					gensWithoutImprovement = 0;
				}

				// Migration check every generation
				checkMigration();

				// IPOP: restart with larger population on stagnation
				if (gensWithoutImprovement >= STAGNATION_LIMIT) {
					LOG.info(String.format("Stagnation after %d generations. Best=%.1f%%. Triggering IPOP restart.",
							STAGNATION_LIMIT, bestWinRate * 100));
					break;
				}
			}

			optimizer.terminate();

			// IPOP: double population on stagnation restart
			if (gensWithoutImprovement >= STAGNATION_LIMIT && evaluationCounter < totalBudget) {
				currentPopulation = Math.min(currentPopulation * 2, totalBudget - evaluationCounter);
				restartCount++;
				LOG.info(String.format("IPOP restart #%d: new population=%d", restartCount, currentPopulation));
			}
		}

		LOG.info(String.format("IPOP-CMA-ES complete. Best: %.1f%% after %d evaluations, %d restarts",
				bestWinRate * 100, evaluationCounter, restartCount));

		return bestSoFar;
	}

	/**
	 * Checks MLflow for better solutions from other islands.
	 * Migrated solutions are added to the Hall of Fame.
	 */
	private void checkMigration() {
		if (mlflow == null) {
			return;
		}
		try {
			MlflowReporter.MigrationData migration = mlflow.loadBestFromOtherIslands(bestWinRate);
			if (migration != null) {
				FeatureVector migrated = arrayToFeatureVector(migration.weights());
				double oldBest = bestWinRate;
				bestWinRate = migration.winRate();
				bestSoFar = migrated.clone();
				hof.add(migrated);
				LOG.info(String.format("Migration: adopted %.1f%% solution from %s (was %.1f%%), HoF=%d",
						migration.winRate() * 100, migration.sourceIsland(), oldBest * 100, hof.size()));
			}
		} catch (Exception e) {
			LOG.warning("Migration check error: " + e.getMessage());
		}
	}

	/**
	 * MOEAFramework Problem that evaluates FeatureVector candidates.
	 */
	private class GsvbProblem extends AbstractProblem {
		GsvbProblem() {
			super(DIMENSION, 1);
		}

		@Override
		public Solution newSolution() {
			Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());
			for (int i = 0; i < getNumberOfVariables(); i++) {
				solution.setVariable(i, new RealVariable(-100.0, 100.0));
			}
			return solution;
		}

		@Override
		public void evaluate(Solution solution) {
			evaluationCounter++;
			String candidateId = "eval" + evaluationCounter;
			double[] point = EncodingUtils.getReal(solution);
			FeatureVector candidate = arrayToFeatureVector(point);

			double winRate;
			if (redis != null) {
				winRate = evaluateDistributed(candidateId, candidate);
			} else {
				winRate = evaluateLocal(candidateId, candidate);
			}

			if (mlflow != null) {
				mlflow.logCandidate(candidateId, candidate, winRate, evaluationCounter);
			}

			if (winRate > bestWinRate) {
				bestWinRate = winRate;
				bestSoFar = candidate.clone();
				hof.add(candidate);
				LOG.info(String.format("New best: %.1f%% win rate (eval %d), HoF=%d members",
						winRate * 100, evaluationCounter, hof.size()));

				if (mlflow != null) {
					mlflow.logBestSoFar(bestSoFar, bestWinRate, evaluationCounter);
				}
			}

			// CMA-ES minimizes, so negate win rate
			solution.setObjective(0, -winRate);
		}
	}

	private double evaluateLocal(String candidateId, FeatureVector candidate) {
		List<FeatureVector> opponents = hof.getMembers();
		EvalResult result = evaluator.evaluate(candidateId, candidate, opponents, decks, matchupsPerEval, gamesPerMatchup);
		return result.winRate();
	}

	private double evaluateDistributed(String candidateId, FeatureVector candidate) {
		// For distributed workers, send the current best as the baseline opponent.
		// Workers use single-opponent evaluation; the coordinator uses the full pool locally.
		FeatureVector opponent = bestSoFar != null ? bestSoFar : FeatureVector.getFittest();
		EvalRequest request = new EvalRequest(
				candidateId,
				evaluationCounter,
				FitnessEvaluator.toWeightMap(candidate),
				FitnessEvaluator.toWeightMap(opponent),
				matchupsPerEval,
				gamesPerMatchup,
				evaluator.getGsvbDepth(),
				evaluator.getGsvbTimeout()
		);
		redis.pushRequest(request);
		EvalResult result = redis.waitForResult(candidateId, Duration.ofMinutes(30));
		return result.winRate();
	}

	static FeatureVector arrayToFeatureVector(double[] values) {
		FeatureVector fv = new FeatureVector();
		WeightedFeature[] features = WeightedFeature.values();
		for (int i = 0; i < features.length; i++) {
			fv.set(features[i], values[i]);
		}
		return fv;
	}

	/**
	 * Seeds the Hall of Fame with an additional member (e.g. from another island on resume).
	 */
	public void seedHofMember(double[] weights) {
		if (weights != null && weights.length == DIMENSION) {
			hof.add(arrayToFeatureVector(weights));
		}
	}

	public FeatureVector getBestSoFar() {
		return bestSoFar;
	}

	public double getBestWinRate() {
		return bestWinRate;
	}

	/**
	 * Returns the best weights as a double array, suitable for seeding the next epoch.
	 */
	public double[] getBestWeightsArray() {
		WeightedFeature[] features = WeightedFeature.values();
		double[] weights = new double[features.length];
		for (int i = 0; i < features.length; i++) {
			weights[i] = bestSoFar.get(features[i]);
		}
		return weights;
	}
}
