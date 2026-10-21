package net.demilich.metastone.game.behaviour.heuristic;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Retrieves and validates {@link FeatureVector} weights published to a trusted MLflow run.
 * <p>
 * Supports:
 * <ul>
 *   <li>Explicit run ID via {@code SPELLSOURCE_MLFLOW_TRUSTED_RUN_ID} / {@code MLFLOW_TRUSTED_RUN_ID}, or</li>
 *   <li>Searching runs in the experiment ({@code SPELLSOURCE_MLFLOW_EXPERIMENT_NAME}, default
 *       {@code spellsource-gsvb-training}) tagged with {@code trusted = 'true'} and {@code published = 'true'},
 *       ordered by {@code metrics.best_win_rate DESC}.</li>
 *   <li>Authentication via Keycloak OIDC {@code client_credentials} (projected ServiceAccount JWT assertion or client
 *       secret), Bearer token ({@code MLFLOW_TRACKING_TOKEN}), or Basic auth.</li>
 *   <li>Validation and tactical polytope projection via {@link FeatureVectorConstraints#project(FeatureVector)}, with
 *       clean fallback to {@link FeatureVector#getFittest()} when offline or unconfigured.</li>
 * </ul>
 */
public final class MlflowFeatureVectorLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(MlflowFeatureVectorLoader.class);

	public static final String DEFAULT_EXPERIMENT_NAME = "spellsource-gsvb-training";
	public static final String ALT_EXPERIMENT_NAME = "spellsource-gsvb-heuristic";
	private static final String DEFAULT_OIDC_ISSUER_URL = "https://login.appmana.com/realms/appmana";
	private static final String DEFAULT_OIDC_CLIENT_ID = "spellsource-mlflow-trainer";
	private static final String JWT_BEARER_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private static volatile FeatureVector cachedPublished;
	private static volatile boolean loadAttempted;

	private MlflowFeatureVectorLoader() {
	}

	/**
	 * Returns the published {@link FeatureVector} from MLflow if {@code SPELLSOURCE_MLFLOW_TRACKING_URI} or
	 * {@code MLFLOW_TRACKING_URI} is configured, caching the result in memory; otherwise falls back to
	 * {@link FeatureVector#getFittest()}.
	 */
	public static FeatureVector getOrLoadPublished() {
		FeatureVector current = cachedPublished;
		if (current != null) {
			return current.clone();
		}
		if (!loadAttempted) {
			synchronized (MlflowFeatureVectorLoader.class) {
				if (!loadAttempted) {
					loadAttempted = true;
					String trackingUri = getConfig("SPELLSOURCE_MLFLOW_TRACKING_URI", "MLFLOW_TRACKING_URI", null);
					if (trackingUri != null && !trackingUri.isBlank()) {
						String expName = getConfig("SPELLSOURCE_MLFLOW_EXPERIMENT_NAME", "MLFLOW_EXPERIMENT_NAME", DEFAULT_EXPERIMENT_NAME);
						String runId = getConfig("SPELLSOURCE_MLFLOW_TRUSTED_RUN_ID", "MLFLOW_TRUSTED_RUN_ID", null);
						Optional<FeatureVector> loaded = loadFromTrustedMlflowRun(trackingUri, expName, runId);
						loaded.ifPresent(fv -> cachedPublished = fv);
					}
				}
			}
		}
		FeatureVector result = cachedPublished;
		return result != null ? result.clone() : FeatureVector.getFittest();
	}

	/**
	 * Clears the in-memory cached published {@link FeatureVector}.
	 */
	public static synchronized void clearCache() {
		cachedPublished = null;
		loadAttempted = false;
	}

	/**
	 * Loads a {@link FeatureVector} from a trusted/published MLflow run using configured credentials.
	 */
	public static Optional<FeatureVector> loadFromTrustedMlflowRun(String trackingUri, String experimentName, String trustedRunId) {
		try {
			String authHeader = resolveAuthHeader();
			return loadFromTrustedMlflowRun(trackingUri, experimentName, trustedRunId, authHeader);
		} catch (Exception e) {
			LOGGER.warn("Failed to load FeatureVector from MLflow ({}): {}", trackingUri, e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Loads a {@link FeatureVector} from a trusted/published MLflow run using an explicit {@code Authorization} header.
	 */
	public static Optional<FeatureVector> loadFromTrustedMlflowRun(
			String trackingUri, String experimentName, String trustedRunId, String authHeader) {
		if (trackingUri == null || trackingUri.isBlank()) {
			return Optional.empty();
		}
		String baseUri = trackingUri.endsWith("/") ? trackingUri.substring(0, trackingUri.length() - 1) : trackingUri;
		String expName = (experimentName != null && !experimentName.isBlank()) ? experimentName : DEFAULT_EXPERIMENT_NAME;

		try {
			String experimentId = null;
			String resolvedRunId = (trustedRunId != null && !trustedRunId.isBlank()) ? trustedRunId.trim() : null;

			if (resolvedRunId != null) {
				// Fetch run metadata to verify it exists and obtain its experiment_id
				JsonObject runResp = httpGetJson(baseUri + "/api/2.0/mlflow/runs/get?run_id="
						+ URLEncoder.encode(resolvedRunId, StandardCharsets.UTF_8), authHeader);
				if (runResp != null && runResp.containsKey("run")) {
					JsonObject info = runResp.getJsonObject("run").getJsonObject("info");
					if (info != null) {
						experimentId = info.getString("experiment_id");
					}
				}
			} else {
				// Resolve experiment by name (try configured name, then fallback name)
				for (String candidateExp : List.of(expName, DEFAULT_EXPERIMENT_NAME, ALT_EXPERIMENT_NAME)) {
					JsonObject expResp = httpGetJson(baseUri + "/api/2.0/mlflow/experiments/get-by-name?experiment_name="
							+ URLEncoder.encode(candidateExp, StandardCharsets.UTF_8), authHeader);
					if (expResp != null && expResp.containsKey("experiment")) {
						experimentId = expResp.getJsonObject("experiment").getString("experiment_id");
						if (experimentId != null) {
							break;
						}
					}
				}
				if (experimentId == null) {
					LOGGER.info("No MLflow experiment '{}' found at {}", expName, baseUri);
					return Optional.empty();
				}

				// Search for runs tagged trusted='true' AND published='true', ordered by best_win_rate DESC
				JsonObject searchBody = new JsonObject()
						.put("experiment_ids", new JsonArray().add(experimentId))
						.put("filter", "tags.trusted = 'true' AND tags.published = 'true'")
						.put("max_results", 5)
						.put("order_by", new JsonArray().add("metrics.best_win_rate DESC").add("attributes.start_time DESC"));
				JsonObject searchResp = httpPostJson(baseUri + "/api/2.0/mlflow/runs/search", searchBody.encode(), authHeader);
				if (searchResp != null && searchResp.containsKey("runs")) {
					JsonArray runs = searchResp.getJsonArray("runs");
					if (runs != null && !runs.isEmpty()) {
						JsonObject topRun = runs.getJsonObject(0);
						resolvedRunId = topRun.getJsonObject("info").getString("run_id");
					}
				}
			}

			if (resolvedRunId == null) {
				LOGGER.info("No trusted & published MLflow run found in experiment {}", experimentId);
				return Optional.empty();
			}

			// Download best_weights.json (or feature_vector.json) from the artifact proxy
			List<String> candidatePaths = experimentId != null
					? List.of(
							baseUri + "/api/2.0/mlflow-artifacts/artifacts/" + experimentId + "/" + resolvedRunId + "/artifacts/best_weights.json",
							baseUri + "/api/2.0/mlflow-artifacts/artifacts/" + experimentId + "/" + resolvedRunId + "/artifacts/feature_vector.json",
							baseUri + "/api/2.0/mlflow-artifacts/artifacts/" + resolvedRunId + "/best_weights.json")
					: List.of(
							baseUri + "/api/2.0/mlflow-artifacts/artifacts/" + resolvedRunId + "/best_weights.json");

			for (String artifactUrl : candidatePaths) {
				String rawJson = httpGetString(artifactUrl, authHeader);
				if (rawJson != null && !rawJson.isBlank()) {
					FeatureVector parsed = parseWeightsJson(rawJson, true);
					if (parsed != null) {
						LOGGER.info("Loaded trusted MLflow FeatureVector from run {} (experiment {})", resolvedRunId, experimentId);
						cachedPublished = parsed.clone();
						return Optional.of(parsed);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Error loading trusted MLflow run from {}: {}", baseUri, e.getMessage());
		}
		return Optional.empty();
	}

	/**
	 * Parses a JSON weight artifact (`{"win_rate": ..., "weights": {...}}` or flat `{...}`) into a {@link FeatureVector}
	 * and projects it onto {@link FeatureVectorConstraints} when {@code applyConstraints} is true.
	 */
	public static FeatureVector parseWeightsJson(String rawJson, boolean applyConstraints) {
		try {
			JsonObject root = new JsonObject(rawJson);
			JsonObject weightsObj = root.containsKey("weights") && root.getValue("weights") instanceof JsonObject
					? root.getJsonObject("weights")
					: root;

			FeatureVector fv = FeatureVector.getFittest().clone();
			int matched = 0;
			for (WeightedFeature feature : WeightedFeature.values()) {
				if (weightsObj.containsKey(feature.name())) {
					Number num = weightsObj.getNumber(feature.name());
					if (num != null && Double.isFinite(num.doubleValue())) {
						fv.set(feature, num.doubleValue());
						matched++;
					}
				}
			}
			if (matched == 0) {
				return null;
			}
			return applyConstraints ? FeatureVectorConstraints.project(fv) : fv;
		} catch (Exception e) {
			LOGGER.warn("Failed to parse MLflow FeatureVector JSON: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Resolves an HTTP {@code Authorization} header from environment variables or system properties, supporting
	 * Keycloak OIDC {@code client_credentials} (JWT client assertion or client secret), Bearer token, or Basic auth.
	 */
	public static String resolveAuthHeader() {
		String bearerToken = getConfig("SPELLSOURCE_MLFLOW_TRACKING_TOKEN", "MLFLOW_TRACKING_TOKEN", null);
		if (bearerToken != null && !bearerToken.isBlank()) {
			return "Bearer " + bearerToken.trim();
		}

		String authMode = getConfig("SPELLSOURCE_MLFLOW_TRACKING_AUTH", "MLFLOW_TRACKING_AUTH", null);
		String assertionFile = getConfig("SPELLSOURCE_MLFLOW_OIDC_ASSERTION_FILE", "MLFLOW_OIDC_CLIENT_ASSERTION_FILE", null);
		String clientSecret = getConfig("SPELLSOURCE_MLFLOW_KEYCLOAK_CLIENT_SECRET", "MLFLOW_KEYCLOAK_CLIENT_SECRET", null);

		if ("oidc-client-credentials".equalsIgnoreCase(authMode)
				|| (assertionFile != null && !assertionFile.isBlank() && Files.exists(Path.of(assertionFile)))
				|| (clientSecret != null && !clientSecret.isBlank())) {
			String issuerUrl = getConfig("SPELLSOURCE_MLFLOW_OIDC_ISSUER_URL", "MLFLOW_OIDC_ISSUER_URL", DEFAULT_OIDC_ISSUER_URL);
			String clientId = getConfig("SPELLSOURCE_MLFLOW_OIDC_CLIENT_ID", "MLFLOW_OIDC_CLIENT_ID", DEFAULT_OIDC_CLIENT_ID);
			String token = mintOidcToken(issuerUrl, clientId, assertionFile, clientSecret);
			if (token != null) {
				return "Bearer " + token;
			}
		}

		String username = getConfig("SPELLSOURCE_MLFLOW_TRACKING_USERNAME", "MLFLOW_TRACKING_USERNAME", null);
		String password = getConfig("SPELLSOURCE_MLFLOW_TRACKING_PASSWORD", "MLFLOW_TRACKING_PASSWORD", null);
		if (username != null && password != null) {
			return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		}
		return null;
	}

	public static String mintOidcToken(String issuerUrl, String clientId, String assertionFile, String clientSecret) {
		try {
			String normalizedIssuer = issuerUrl.endsWith("/") ? issuerUrl.substring(0, issuerUrl.length() - 1) : issuerUrl;
			URI tokenEndpoint = URI.create(normalizedIssuer + "/protocol/openid-connect/token");

			StringBuilder form = new StringBuilder("grant_type=client_credentials")
					.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
			if (assertionFile != null && !assertionFile.isBlank() && Files.exists(Path.of(assertionFile))) {
				String assertion = Files.readString(Path.of(assertionFile), StandardCharsets.UTF_8).trim();
				form.append("&client_assertion_type=").append(URLEncoder.encode(JWT_BEARER_ASSERTION_TYPE, StandardCharsets.UTF_8))
						.append("&client_assertion=").append(URLEncoder.encode(assertion, StandardCharsets.UTF_8));
			} else if (clientSecret != null && !clientSecret.isBlank()) {
				form.append("&client_secret=").append(URLEncoder.encode(clientSecret, StandardCharsets.UTF_8));
			} else {
				return null;
			}

			HttpRequest request = HttpRequest.newBuilder(tokenEndpoint)
					.timeout(Duration.ofSeconds(10))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
					.build();

			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				JsonObject obj = new JsonObject(response.body());
				return obj.getString("access_token");
			}
		} catch (Exception e) {
			LOGGER.warn("OIDC token exchange failed: {}", e.getMessage());
		}
		return null;
	}

	private static JsonObject httpGetJson(String url, String authHeader) {
		String body = httpGetString(url, authHeader);
		if (body == null || body.isBlank()) {
			return null;
		}
		return new JsonObject(body);
	}

	private static String httpGetString(String url, String authHeader) {
		try {
			HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.GET();
			if (authHeader != null && !authHeader.isBlank()) {
				builder.header("Authorization", authHeader);
			}
			HttpResponse<String> resp = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				return resp.body();
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static JsonObject httpPostJson(String url, String jsonBody, String authHeader) {
		try {
			HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
			if (authHeader != null && !authHeader.isBlank()) {
				builder.header("Authorization", authHeader);
			}
			HttpResponse<String> resp = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				return new JsonObject(resp.body());
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static String getConfig(String propOrEnv1, String env2, String fallback) {
		String v = System.getProperty(propOrEnv1);
		if (v != null && !v.isBlank()) {
			return v;
		}
		v = System.getenv(propOrEnv1);
		if (v != null && !v.isBlank()) {
			return v;
		}
		if (env2 != null) {
			v = System.getProperty(env2);
			if (v != null && !v.isBlank()) {
				return v;
			}
			v = System.getenv(env2);
			if (v != null && !v.isBlank()) {
				return v;
			}
		}
		return fallback;
	}
}
