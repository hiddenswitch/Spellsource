package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import org.mlflow.api.proto.Service;
import org.mlflow.tracking.MlflowClient;
import org.mlflow.tracking.creds.BasicMlflowHostCreds;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Logs training progress to MLflow tracking server.
 * All islands share a single run with island-prefixed metrics.
 * Feature vectors and traces are stored as artifacts.
 * Tags are used only for small metadata (win rates, step offsets, status).
 */
public class MlflowReporter implements AutoCloseable {
	private static final Logger LOG = Logger.getLogger(MlflowReporter.class.getName());
	private static final String SESSION_TAG = "session_type";
	private static final String DEFAULT_SESSION_VALUE = "ipop_cmaes_hof_v2";
	private static final String DEFAULT_EXPERIMENT_NAME = "spellsource-gsvb-training";

	private final MlflowClient client;
	private final String experimentId;
	private final String sessionValue;
	private final String trackingUri;
	private final String authHeader;
	private String runId;
	private String islandPrefix;
	private int stepOffset;

	private double bestWinRate = 0.0;
	private double worstWinRate = 1.0;
	private double totalWinRate = 0.0;
	private int evalCount = 0;
	private double cachedGlobalBest = 0.0;

	public MlflowReporter(String trackingUri, String sessionName) {
		this.trackingUri = trackingUri;
		String username = System.getenv("MLFLOW_TRACKING_USERNAME");
		String password = System.getenv("MLFLOW_TRACKING_PASSWORD");
		if (username != null && password != null) {
			this.client = new MlflowClient(new BasicMlflowHostCreds(trackingUri, username, password));
			this.authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		} else {
			this.client = new MlflowClient(trackingUri);
			this.authHeader = null;
		}

		this.sessionValue = sessionName != null ? sessionName : DEFAULT_SESSION_VALUE;

		String experimentName = System.getenv("MLFLOW_EXPERIMENT_NAME");
		if (experimentName == null) {
			experimentName = DEFAULT_EXPERIMENT_NAME;
		}

		String expId = null;
		try {
			expId = client.getExperimentByName(experimentName)
					.orElseThrow()
					.getExperimentId();
		} catch (Exception e) {
			expId = client.createExperiment(experimentName);
		}
		this.experimentId = expId;
		LOG.info("MLflow experiment: " + experimentName + " (id=" + experimentId + ")");
	}

	/**
	 * Joins or creates the single shared training run.
	 * All islands log to the same run with prefixed metrics.
	 */
	public void joinTrainingSession(int generations, int populationSize,
	                                 int matchupsPerEval, int gamesPerMatchup, Long seed) {
		this.islandPrefix = seed != null ? "island_" + seed + "/" : "";

		// Look for an existing active training session
		var runsPage = client.searchRuns(
				List.of(experimentId),
				"tags." + SESSION_TAG + " = '" + sessionValue + "' AND attributes.status = 'RUNNING'",
				Service.ViewType.ACTIVE_ONLY,
				1,
				List.of("attributes.start_time DESC")
		);

		List<Service.Run> runs = runsPage.getItems();

		if (!runs.isEmpty()) {
			Service.Run existingRun = runs.get(0);
			this.runId = existingRun.getInfo().getRunId();

			// Restore state from tags (only small metadata)
			for (Service.RunTag tag : existingRun.getData().getTagsList()) {
				if (tag.getKey().equals(islandPrefix + "last_step")) {
					try {
						this.stepOffset = Integer.parseInt(tag.getValue());
					} catch (NumberFormatException e) {
						this.stepOffset = 0;
					}
				}
				if (tag.getKey().equals(islandPrefix + "best_win_rate")) {
					try {
						this.bestWinRate = Double.parseDouble(tag.getValue());
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				if (tag.getKey().equals("best_win_rate_value")) {
					try {
						this.cachedGlobalBest = Double.parseDouble(tag.getValue());
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}

			LOG.info(String.format("Joined existing training session: %s (step offset=%d, best=%.1f%%)",
					runId, stepOffset, bestWinRate * 100));
		} else {
			// Create new training session
			Service.RunInfo runInfo = client.createRun(experimentId);
			this.runId = runInfo.getRunId();
			client.setTag(runId, SESSION_TAG, sessionValue);
			client.setTag(runId, "mlflow.runName", "GSVB Training (HoF)");
			client.logParam(runId, "generations", String.valueOf(generations));
			client.logParam(runId, "population_size", String.valueOf(populationSize));
			client.logParam(runId, "matchups_per_eval", String.valueOf(matchupsPerEval));
			client.logParam(runId, "games_per_matchup", String.valueOf(gamesPerMatchup));
			client.logParam(runId, "eval_method", "hall_of_fame");
			client.logParam(runId, "islands", "10");
			LOG.info("Created new training session: " + runId);
		}

		// Tag this island as active
		if (seed != null) {
			client.setTag(runId, islandPrefix + "seed", String.valueOf(seed));
			client.setTag(runId, islandPrefix + "status", "running");
		}
	}

	/**
	 * Logs a candidate evaluation as step metrics.
	 * Tags are updated every 10 evals to reduce HTTP overhead.
	 */
	public void logCandidate(String candidateId, FeatureVector weights, double winRate, int localStep) {
		if (runId == null) {
			return;
		}
		try {
			int step = stepOffset + localStep;
			evalCount++;
			totalWinRate += winRate;
			if (winRate > bestWinRate) {
				bestWinRate = winRate;
			}
			if (winRate < worstWinRate) {
				worstWinRate = winRate;
			}

			long timestamp = System.currentTimeMillis();

			// Per-island metrics
			client.logMetric(runId, islandPrefix + "win_rate", winRate, timestamp, step);
			client.logMetric(runId, islandPrefix + "best_win_rate", bestWinRate, timestamp, step);

			// Save step offset for resume (only every 10 evals to reduce overhead)
			if (evalCount % 10 == 0) {
				client.setTag(runId, islandPrefix + "last_step", String.valueOf(step));
				client.setTag(runId, islandPrefix + "best_win_rate", String.valueOf(bestWinRate));
			}
		} catch (Exception e) {
			LOG.warning("Failed to log metrics to MLflow: " + e.getMessage());
		}
	}

	/**
	 * Logs the best-so-far weights as artifacts and updates global best if this is the best across all islands.
	 */
	public void logBestSoFar(FeatureVector best, double winRate, int localStep) {
		if (runId == null) {
			return;
		}
		try {
			int step = stepOffset + localStep;
			long timestamp = System.currentTimeMillis();

			// Store per-island best weights as artifact
			String weightsJson = featureVectorToJson(best, winRate);
			uploadArtifact(islandPrefix + "best_weights.json", weightsJson);

			// Update win rate tag (small metadata for discovery)
			client.setTag(runId, islandPrefix + "best_win_rate", String.valueOf(winRate));

			// Check if this is the global best
			if (winRate > cachedGlobalBest) {
				cachedGlobalBest = winRate;
				client.logMetric(runId, "best_win_rate", winRate, timestamp, step);

				// Store global best weights as artifact
				uploadArtifact("best_weights.json", weightsJson);
				client.setTag(runId, "best_win_rate_value", String.valueOf(winRate));
				client.setTag(runId, "best_source_island", islandPrefix);

				LOG.info(String.format("New global best: %.1f%% from %s", winRate * 100, islandPrefix));
			}
		} catch (Exception e) {
			LOG.warning("Failed to log best-so-far to MLflow: " + e.getMessage());
		}
	}

	/**
	 * Loads the global best weights from the training session artifacts.
	 */
	public double[] loadBestWeights() {
		try {
			// Find the active or most recent training session
			var runsPage = client.searchRuns(
					List.of(experimentId),
					"tags." + SESSION_TAG + " = '" + sessionValue + "'",
					Service.ViewType.ACTIVE_ONLY,
					1,
					List.of("attributes.start_time DESC")
			);

			List<Service.Run> runs = runsPage.getItems();
			if (runs.isEmpty()) {
				LOG.info("No previous training sessions found in MLflow");
				return null;
			}

			String sessionRunId = runs.get(0).getInfo().getRunId();
			String json = downloadArtifact(sessionRunId, "best_weights.json");
			if (json == null) {
				LOG.info("Training session has no best weights yet");
				return null;
			}

			double[] weights = jsonToWeightArray(json);
			if (weights == null) {
				return null;
			}

			// Extract win rate from JSON
			double rate = extractWinRate(json);
			LOG.info(String.format("Loaded global best weights (%.1f%% win rate, %d/%d features)",
					rate * 100, weights.length, WeightedFeature.values().length));
			return weights;
		} catch (Exception e) {
			LOG.warning("Failed to load best weights from MLflow: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Returns the best win rate restored from the previous session for this island.
	 */
	public double getRestoredBestWinRate() {
		return bestWinRate;
	}

	/**
	 * Loads this island's own best weights from artifacts.
	 * Falls back to global best if island-specific weights are not found.
	 */
	public double[] loadIslandBestWeights() {
		if (runId == null || islandPrefix == null || islandPrefix.isEmpty()) {
			return loadBestWeights();
		}
		try {
			String json = downloadArtifact(runId, islandPrefix + "best_weights.json");
			if (json == null) {
				LOG.info("No island-specific best weights found, falling back to global best");
				return loadBestWeights();
			}

			double[] weights = jsonToWeightArray(json);
			if (weights == null) {
				return loadBestWeights();
			}

			LOG.info(String.format("Loaded island best weights (%d/%d features, %.1f%% win rate)",
					weights.length, WeightedFeature.values().length, bestWinRate * 100));
			return weights;
		} catch (Exception e) {
			LOG.warning("Failed to load island best weights: " + e.getMessage());
			return loadBestWeights();
		}
	}

	/**
	 * Returns the shared run ID.
	 */
	public String getRunId() {
		return runId;
	}

	/**
	 * Loads the best weights from any other island in the shared run.
	 * Used for inter-island migration.
	 * Returns null if no other island has a better solution.
	 */
	public MigrationData loadBestFromOtherIslands(double currentBestWinRate) {
		try {
			Service.Run run = client.getRun(runId);
			Map<String, String> tagMap = new HashMap<>();
			for (Service.RunTag tag : run.getData().getTagsList()) {
				tagMap.put(tag.getKey(), tag.getValue());
			}

			// Find the island with the highest best_win_rate (excluding ourselves)
			String bestIslandPrefix = null;
			double bestMetric = currentBestWinRate;

			for (Map.Entry<String, String> entry : tagMap.entrySet()) {
				String key = entry.getKey();
				if (key.endsWith("/best_win_rate") && key.startsWith("island_") && !key.equals(islandPrefix + "best_win_rate")) {
					try {
						double rate = Double.parseDouble(entry.getValue());
						if (rate > bestMetric) {
							bestMetric = rate;
							bestIslandPrefix = key.replace("best_win_rate", "");
						}
					} catch (NumberFormatException e) {
						// skip
					}
				}
			}

			if (bestIslandPrefix == null) {
				return null;
			}

			// Load weights from that island's artifact
			String json = downloadArtifact(runId, bestIslandPrefix + "best_weights.json");
			if (json == null) {
				return null;
			}
			double[] weights = jsonToWeightArray(json);
			if (weights == null) {
				return null;
			}

			return new MigrationData(weights, bestMetric, bestIslandPrefix);
		} catch (Exception e) {
			LOG.warning("Migration check failed: " + e.getMessage());
			return null;
		}
	}

	public record MigrationData(double[] weights, double winRate, String sourceIsland) {}

	/**
	 * Loads best weights from all islands in the shared run.
	 * Used to seed the Hall of Fame on restart.
	 */
	public List<double[]> loadAllIslandBests() {
		if (runId == null) {
			return List.of();
		}
		try {
			Service.Run run = client.getRun(runId);
			Map<String, String> tagMap = new HashMap<>();
			for (Service.RunTag tag : run.getData().getTagsList()) {
				tagMap.put(tag.getKey(), tag.getValue());
			}

			List<double[]> results = new ArrayList<>();

			// Find all islands with best_win_rate tags
			Set<String> islandPrefixes = new HashSet<>();
			for (String key : tagMap.keySet()) {
				if (key.endsWith("/best_win_rate") && key.startsWith("island_")) {
					islandPrefixes.add(key.replace("best_win_rate", ""));
				}
			}

			for (String prefix : islandPrefixes) {
				if (prefix.equals(islandPrefix)) {
					continue; // skip ourselves
				}
				try {
					String json = downloadArtifact(runId, prefix + "best_weights.json");
					if (json != null) {
						double[] weights = jsonToWeightArray(json);
						if (weights != null) {
							results.add(weights);
						}
					}
				} catch (Exception e) {
					// skip this island
				}
			}

			LOG.info("Loaded " + results.size() + " island best weights for HoF seeding");
			return results;
		} catch (Exception e) {
			LOG.warning("Failed to load island bests: " + e.getMessage());
			return List.of();
		}
	}

	/**
	 * Logs a timed-out game trace as an artifact.
	 */
	public void logTimeoutTrace(String evalId, int gameIndex, String traceDump) {
		if (runId == null) {
			return;
		}
		uploadArtifact(islandPrefix + "timeouts/timeout_" + evalId + "_game" + gameIndex + ".json", traceDump);
	}

	// --- Artifact helpers ---

	private void uploadArtifact(String artifactPath, String content) {
		try {
			String url = trackingUri + "/api/2.0/mlflow-artifacts/artifacts/" + runId + "/" + artifactPath;

			HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/octet-stream");
			if (authHeader != null) {
				conn.setRequestProperty("Authorization", authHeader);
			}
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);

			byte[] data = content.getBytes(StandardCharsets.UTF_8);
			conn.setFixedLengthStreamingMode(data.length);
			try (var out = conn.getOutputStream()) {
				out.write(data);
			}

			int status = conn.getResponseCode();
			if (status != 200) {
				LOG.warning("Failed to upload artifact " + artifactPath + ": HTTP " + status);
			}
			conn.disconnect();
		} catch (Exception e) {
			LOG.warning("Failed to upload artifact " + artifactPath + ": " + e.getMessage());
		}
	}

	private String downloadArtifact(String targetRunId, String artifactPath) {
		try {
			String url = trackingUri + "/api/2.0/mlflow-artifacts/artifacts/" + targetRunId + "/" + artifactPath;

			HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
			conn.setRequestMethod("GET");
			if (authHeader != null) {
				conn.setRequestProperty("Authorization", authHeader);
			}
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);

			int status = conn.getResponseCode();
			if (status != 200) {
				return null;
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				String result = reader.lines().collect(Collectors.joining("\n"));
				conn.disconnect();
				return result;
			}
		} catch (Exception e) {
			return null;
		}
	}

	// --- JSON serialization for feature vectors ---

	private String featureVectorToJson(FeatureVector fv, double winRate) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("  \"win_rate\": ").append(winRate).append(",\n");
		sb.append("  \"weights\": {\n");
		WeightedFeature[] features = WeightedFeature.values();
		for (int i = 0; i < features.length; i++) {
			sb.append("    \"").append(features[i].name()).append("\": ").append(fv.get(features[i]));
			if (i < features.length - 1) {
				sb.append(",");
			}
			sb.append("\n");
		}
		sb.append("  }\n");
		sb.append("}");
		return sb.toString();
	}

	private double[] jsonToWeightArray(String json) {
		try {
			WeightedFeature[] features = WeightedFeature.values();
			double[] weights = new double[features.length];
			int found = 0;

			for (int i = 0; i < features.length; i++) {
				String key = "\"" + features[i].name() + "\":";
				int idx = json.indexOf(key);
				if (idx < 0) {
					key = "\"" + features[i].name() + "\": ";
					idx = json.indexOf(key);
				}
				if (idx >= 0) {
					int start = idx + key.length();
					// Skip whitespace
					while (start < json.length() && json.charAt(start) == ' ') start++;
					int end = start;
					while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-' || json.charAt(end) == 'E' || json.charAt(end) == 'e' || json.charAt(end) == '+')) {
						end++;
					}
					weights[i] = Double.parseDouble(json.substring(start, end));
					found++;
				}
			}

			return found > 0 ? weights : null;
		} catch (Exception e) {
			LOG.warning("Failed to parse weights JSON: " + e.getMessage());
			return null;
		}
	}

	private double extractWinRate(String json) {
		try {
			String key = "\"win_rate\":";
			int idx = json.indexOf(key);
			if (idx < 0) {
				key = "\"win_rate\": ";
				idx = json.indexOf(key);
			}
			if (idx >= 0) {
				int start = idx + key.length();
				while (start < json.length() && json.charAt(start) == ' ') start++;
				int end = start;
				while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
					end++;
				}
				return Double.parseDouble(json.substring(start, end));
			}
		} catch (Exception e) {
			// ignore
		}
		return 0.0;
	}

	/**
	 * Marks this island as finished but does not terminate the shared run.
	 */
	@Override
	public void close() {
		if (runId != null && islandPrefix != null) {
			try {
				client.setTag(runId, islandPrefix + "status", "finished");
			} catch (Exception e) {
				LOG.warning("Failed to update island status: " + e.getMessage());
			}
		}
	}
}
