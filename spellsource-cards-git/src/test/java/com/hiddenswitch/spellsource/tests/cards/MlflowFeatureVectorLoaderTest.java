package com.hiddenswitch.spellsource.tests.cards;

import com.sun.net.httpserver.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVectorConstraints;
import net.demilich.metastone.game.behaviour.heuristic.MlflowFeatureVectorLoader;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MlflowFeatureVectorLoaderTest {

	@AfterEach
	public void tearDown() {
		System.clearProperty("SPELLSOURCE_MLFLOW_TRACKING_URI");
		System.clearProperty("SPELLSOURCE_MLFLOW_EXPERIMENT_NAME");
		System.clearProperty("SPELLSOURCE_MLFLOW_TRUSTED_RUN_ID");
		System.clearProperty("SPELLSOURCE_MLFLOW_TRACKING_TOKEN");
		MlflowFeatureVectorLoader.clearCache();
	}

	@Test
	public void testFeatureVectorConstraintsEnforcesPolarityAndInvariants() {
		FeatureVector pathological = new FeatureVector();
		// Deliberately set wrong signs and broken invariants
		pathological.set(WeightedFeature.OWN_HP_FACTOR, -50.0);
		pathological.set(WeightedFeature.OPPONENT_HP_FACTOR, 30.0);
		pathological.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, -40.0);
		pathological.set(WeightedFeature.CURSED_FACTOR, 20.0);
		pathological.set(WeightedFeature.OWN_CARD_COUNT, 10.0);
		pathological.set(WeightedFeature.MINION_INTRINSIC_VALUE, 10.0);
		pathological.set(WeightedFeature.CORPSE_COUNT_VALUE, 100.0);
		pathological.set(WeightedFeature.OWN_LOCATION_VALUE, -25.0);
		pathological.set(WeightedFeature.IMBUE_VALUE, -10.0);
		pathological.set(WeightedFeature.RED_MODIFIER, -34.931);
		pathological.set(WeightedFeature.YELLOW_MODIFIER, -71.119);
		pathological.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, -20.423);
		pathological.set(WeightedFeature.MINION_RUSH_MODIFIER, -45.326);
		pathological.set(WeightedFeature.WEAPON_VALUE, 90.0);
		pathological.set(WeightedFeature.HAND_BUFF_VALUE, 95.339);
		pathological.set(WeightedFeature.HAND_MANA_REDUCTION_VALUE, 90.670);

		FeatureVectorConstraints.project(pathological);

		assertTrue(pathological.get(WeightedFeature.OWN_HP_FACTOR) > 0.0);
		assertTrue(pathological.get(WeightedFeature.OPPONENT_HP_FACTOR) < 0.0);
		assertTrue(pathological.get(WeightedFeature.MINION_RED_TAUNT_MODIFIER) >= pathological.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER));
		assertTrue(pathological.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER) >= pathological.get(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER));
		assertTrue(pathological.get(WeightedFeature.CURSED_FACTOR) < 0.0);
		assertTrue(pathological.get(WeightedFeature.OWN_LOCATION_VALUE) > 0.0);
		assertTrue(pathological.get(WeightedFeature.IMBUE_VALUE) > 0.0);
		assertTrue(pathological.get(WeightedFeature.RED_MODIFIER) <= pathological.get(WeightedFeature.YELLOW_MODIFIER));
		assertTrue(pathological.get(WeightedFeature.MINION_DEATHRATTLE_MODIFIER) > 0.0);
		assertTrue(pathological.get(WeightedFeature.MINION_RUSH_MODIFIER) > 0.0);
	}

	@Test
	public void testLoadFromTrustedMlflowRunAndFallback() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		try {
			server.createContext("/api/2.0/mlflow/experiments/get-by-name", exchange -> {
				String resp = new JsonObject()
						.put("experiment", new JsonObject().put("experiment_id", "3").put("name", "spellsource-gsvb-training"))
						.encode();
				byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, bytes.length);
				try (OutputStream os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			});

			server.createContext("/api/2.0/mlflow/runs/search", exchange -> {
				String resp = new JsonObject()
						.put("runs", new JsonArray().add(new JsonObject()
								.put("info", new JsonObject().put("run_id", "trusted_run_123").put("experiment_id", "3"))))
						.encode();
				byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, bytes.length);
				try (OutputStream os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			});

			server.createContext("/api/2.0/mlflow-artifacts/artifacts/3/trusted_run_123/artifacts/best_weights.json", exchange -> {
				String auth = exchange.getRequestHeaders().getFirst("Authorization");
				if (!"Bearer test-oidc-token".equals(auth)) {
					exchange.sendResponseHeaders(403, -1);
					return;
				}
				JsonObject weights = new JsonObject();
				FeatureVector base = FeatureVector.getFittest();
				for (WeightedFeature wf : WeightedFeature.values()) {
					weights.put(wf.name(), base.get(wf));
				}
				weights.put(WeightedFeature.IMBUE_VALUE.name(), 44.5);
				weights.put(WeightedFeature.OWN_LOCATION_VALUE.name(), 51.25);

				String resp = new JsonObject()
						.put("win_rate", 0.8125)
						.put("weights", weights)
						.encode();
				byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, bytes.length);
				try (OutputStream os = exchange.getResponseBody()) {
					os.write(bytes);
				}
			});

			server.start();
			String baseUri = "http://127.0.0.1:" + server.getAddress().getPort();

			System.setProperty("SPELLSOURCE_MLFLOW_TRACKING_URI", baseUri);
			System.setProperty("SPELLSOURCE_MLFLOW_TRACKING_TOKEN", "test-oidc-token");

			Optional<FeatureVector> loaded = FeatureVector.loadFromTrustedMlflowRun(baseUri, "spellsource-gsvb-training", null);
			assertTrue(loaded.isPresent(), "Should load trusted MLflow FeatureVector");
			assertEquals(44.5, loaded.get().get(WeightedFeature.IMBUE_VALUE), 1e-6);
			assertEquals(51.25, loaded.get().get(WeightedFeature.OWN_LOCATION_VALUE), 1e-6);

			FeatureVector published = FeatureVector.getPublished();
			assertEquals(44.5, published.get(WeightedFeature.IMBUE_VALUE), 1e-6);
		} finally {
			server.stop(0);
		}

		// Verify clean fallback when unconfigured
		tearDown();
		FeatureVector fallback = FeatureVector.getPublished();
		assertEquals(FeatureVector.getFittest().get(WeightedFeature.IMBUE_VALUE), fallback.get(WeightedFeature.IMBUE_VALUE), 1e-6);
	}
}
