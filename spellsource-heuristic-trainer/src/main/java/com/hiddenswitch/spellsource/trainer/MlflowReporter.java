package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import org.mlflow.api.proto.Service;
import org.mlflow.tracking.MlflowClient;
import org.mlflow.tracking.creds.BasicMlflowHostCreds;

import java.util.*;
import java.util.logging.Logger;

/**
 * Logs training progress to MLflow tracking server.
 * Each island is a single run with step metrics over time.
 * Loads previous best weights from metrics on startup.
 */
public class MlflowReporter implements AutoCloseable {
	private static final Logger LOG = Logger.getLogger(MlflowReporter.class.getName());
	private static final String EXPERIMENT_NAME = "spellsource-gsvb-training";

	private final MlflowClient client;
	private final String experimentId;
	private String runId;

	private double bestWinRate = 0.0;
	private double worstWinRate = 1.0;
	private double totalWinRate = 0.0;
	private int evalCount = 0;

	public MlflowReporter(String trackingUri) {
		String username = System.getenv("MLFLOW_TRACKING_USERNAME");
		String password = System.getenv("MLFLOW_TRACKING_PASSWORD");
		if (username != null && password != null) {
			this.client = new MlflowClient(new BasicMlflowHostCreds(trackingUri, username, password));
		} else {
			this.client = new MlflowClient(trackingUri);
		}

		// Get or create experiment
		String expId = null;
		try {
			expId = client.getExperimentByName(EXPERIMENT_NAME)
					.orElseThrow()
					.getExperimentId();
		} catch (Exception e) {
			expId = client.createExperiment(EXPERIMENT_NAME);
		}
		this.experimentId = expId;
		LOG.info("MLflow experiment: " + EXPERIMENT_NAME + " (id=" + experimentId + ")");
	}

	/**
	 * Starts the single run for this island.
	 */
	public void startTrainingRun(int generations, int populationSize, int matchupsPerEval, int gamesPerMatchup, Long seed) {
		Service.RunInfo runInfo = client.createRun(experimentId);
		this.runId = runInfo.getRunId();

		client.logParam(runId, "generations", String.valueOf(generations));
		client.logParam(runId, "population_size", String.valueOf(populationSize));
		client.logParam(runId, "matchups_per_eval", String.valueOf(matchupsPerEval));
		client.logParam(runId, "games_per_matchup", String.valueOf(gamesPerMatchup));
		if (seed != null) {
			client.logParam(runId, "seed", String.valueOf(seed));
			client.setTag(runId, "mlflow.runName", "island_seed_" + seed);
		}

		LOG.info("Started MLflow training run: " + runId);
	}

	/**
	 * Logs a candidate evaluation as step metrics on the single run.
	 */
	public void logCandidate(String candidateId, FeatureVector weights, double winRate, int step) {
		if (runId == null) {
			return;
		}
		try {
			evalCount++;
			totalWinRate += winRate;
			if (winRate > bestWinRate) {
				bestWinRate = winRate;
			}
			if (winRate < worstWinRate) {
				worstWinRate = winRate;
			}

			long timestamp = System.currentTimeMillis();
			client.logMetric(runId, "win_rate", winRate, timestamp, step);
			client.logMetric(runId, "best_win_rate", bestWinRate, timestamp, step);
			client.logMetric(runId, "worst_win_rate", worstWinRate, timestamp, step);
			client.logMetric(runId, "avg_win_rate", totalWinRate / evalCount, timestamp, step);
		} catch (Exception e) {
			LOG.warning("Failed to log metrics to MLflow: " + e.getMessage());
		}
	}

	/**
	 * Logs the best-so-far weights as metrics.
	 */
	public void logBestSoFar(FeatureVector best, double winRate, int step) {
		if (runId == null) {
			return;
		}
		try {
			long timestamp = System.currentTimeMillis();
			for (WeightedFeature feature : WeightedFeature.values()) {
				client.logMetric(runId, "best_" + feature.name(), best.get(feature), timestamp, step);
			}
		} catch (Exception e) {
			LOG.warning("Failed to log best-so-far to MLflow: " + e.getMessage());
		}
	}

	/**
	 * Loads the best weights from any previous completed run in the experiment.
	 * Reads the best_&lt;feature&gt; metrics from the run with the highest best_win_rate.
	 * Returns null if no previous run with weights exists.
	 */
	public double[] loadBestWeights() {
		try {
			// Search for completed runs ordered by best_win_rate descending
			var runsPage = client.searchRuns(
					List.of(experimentId),
					"attributes.status = 'FINISHED'",
					Service.ViewType.ACTIVE_ONLY,
					1,
					List.of("metrics.best_win_rate DESC")
			);

			List<Service.Run> runs = runsPage.getItems();
			if (runs.isEmpty()) {
				LOG.info("No previous completed runs found in MLflow");
				return null;
			}

			Service.Run bestRun = runs.get(0);
			String bestRunId = bestRun.getInfo().getRunId();

			// Read the best_<feature> metrics from this run
			Map<String, Double> metricMap = new HashMap<>();
			double bestMetric = 0.0;
			for (Service.Metric metric : bestRun.getData().getMetricsList()) {
				metricMap.put(metric.getKey(), metric.getValue());
				if ("best_win_rate".equals(metric.getKey())) {
					bestMetric = metric.getValue();
				}
			}

			WeightedFeature[] features = WeightedFeature.values();
			double[] weights = new double[features.length];
			int found = 0;
			for (int i = 0; i < features.length; i++) {
				Double val = metricMap.get("best_" + features[i].name());
				if (val != null) {
					weights[i] = val;
					found++;
				}
			}

			if (found < features.length) {
				LOG.warning(String.format("Best run %s only has %d/%d weight metrics", bestRunId, found, features.length));
				if (found == 0) {
					return null;
				}
			}

			LOG.info(String.format("Loaded weights from previous best run %s (%.1f%% win rate, %d/%d features)",
					bestRunId, bestMetric * 100, found, features.length));
			return weights;
		} catch (Exception e) {
			LOG.warning("Failed to load best weights from MLflow: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Ends the training run.
	 */
	public void endTrainingRun() {
		if (runId != null) {
			try {
				client.setTerminated(runId);
				LOG.info("Ended MLflow training run: " + runId);
			} catch (Exception e) {
				LOG.warning("Failed to end MLflow run: " + e.getMessage());
			}
		}
	}

	@Override
	public void close() {
		endTrainingRun();
	}
}
