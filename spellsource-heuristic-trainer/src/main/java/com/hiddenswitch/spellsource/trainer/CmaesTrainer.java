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
 * using MOEAFramework's CMA-ES implementation.
 */
public class CmaesTrainer {
	private static final Logger LOG = Logger.getLogger(CmaesTrainer.class.getName());
	private static final int DIMENSION = WeightedFeature.values().length;

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

	private FeatureVector baseline;
	private FeatureVector bestSoFar;
	private double bestWinRate = 0.0;
	private int evaluationCounter = 0;

	public CmaesTrainer(int generations, int populationSize, int matchupsPerEval, int gamesPerMatchup,
	                     List<GameDeck> decks, FitnessEvaluator evaluator, MlflowReporter mlflow, RedisQueue redis,
	                     Long seed, double sigmaInit) {
		this(generations, populationSize, matchupsPerEval, gamesPerMatchup,
				decks, evaluator, mlflow, redis, seed, sigmaInit, null);
	}

	public CmaesTrainer(int generations, int populationSize, int matchupsPerEval, int gamesPerMatchup,
	                     List<GameDeck> decks, FitnessEvaluator evaluator, MlflowReporter mlflow, RedisQueue redis,
	                     Long seed, double sigmaInit, double[] initialPoint) {
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

		this.baseline = FeatureVector.getFittest();
		this.bestSoFar = baseline.clone();
	}

	/**
	 * Runs the CMA-ES optimization. Returns the best FeatureVector found.
	 */
	public FeatureVector train() {
		WeightedFeature[] features = WeightedFeature.values();

		// Starting point
		double[] startPoint = new double[DIMENSION];
		if (initialPoint != null && initialPoint.length == DIMENSION) {
			System.arraycopy(initialPoint, 0, startPoint, 0, DIMENSION);
		} else {
			for (int i = 0; i < DIMENSION; i++) {
				startPoint[i] = baseline.get(features[i]);
			}
		}

		if (seed != null) {
			PRNG.setSeed(seed);
		}

		int maxEvaluations = generations * populationSize;

		LOG.info(String.format("Starting CMA-ES (MOEAFramework): %d dimensions, population=%d, max evaluations=%d, sigma=%.1f, seed=%s, startPoint=%s",
				DIMENSION, populationSize, maxEvaluations, sigmaInit,
				seed != null ? seed.toString() : "random",
				initialPoint != null ? "mlflow_best" : "getFittest()"));

		GsvbProblem problem = new GsvbProblem();

		CMAES optimizer = new CMAES(problem, populationSize, null, new NondominatedPopulation());

		// Configure via TypedProperties so CMA-ES handles auto-computation of cc/cs/damps
		TypedProperties props = new TypedProperties();
		props.setDouble("sigma", sigmaInit);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < startPoint.length; i++) {
			if (i > 0) sb.append(',');
			sb.append(startPoint[i]);
		}
		props.setString("initialSearchPoint", sb.toString());
		optimizer.applyConfiguration(props);

		optimizer.step(); // initialize

		while (optimizer.getNumberOfEvaluations() < maxEvaluations) {
			optimizer.step();
		}

		NondominatedPopulation result = optimizer.getResult();
		optimizer.terminate();

		if (!result.isEmpty()) {
			Solution best = result.get(0);
			double[] bestPoint = EncodingUtils.getReal(best);
			FeatureVector optimized = arrayToFeatureVector(bestPoint);
			double finalFitness = -best.getObjective(0);

			LOG.info(String.format("CMA-ES complete. Optimizer best: %.1f%%, tracked best: %.1f%%",
					finalFitness * 100, bestWinRate * 100));

			return bestWinRate > finalFitness ? bestSoFar : optimized;
		}

		return bestSoFar;
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
				LOG.info(String.format("New best: %.1f%% win rate (eval %d)", winRate * 100, evaluationCounter));

				if (mlflow != null) {
					mlflow.logBestSoFar(bestSoFar, bestWinRate, evaluationCounter);
				}
			}

			if (winRate > 0.55) {
				baseline = candidate.clone();
				LOG.info("Baseline updated to new candidate");
			}

			// CMA-ES minimizes, so negate win rate
			solution.setObjective(0, -winRate);
		}
	}

	private double evaluateLocal(String candidateId, FeatureVector candidate) {
		EvalResult result = evaluator.evaluate(candidateId, candidate, baseline, decks, matchupsPerEval, gamesPerMatchup);
		return result.winRate();
	}

	private double evaluateDistributed(String candidateId, FeatureVector candidate) {
		EvalRequest request = new EvalRequest(
				candidateId,
				evaluationCounter,
				FitnessEvaluator.toWeightMap(candidate),
				FitnessEvaluator.toWeightMap(baseline),
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
