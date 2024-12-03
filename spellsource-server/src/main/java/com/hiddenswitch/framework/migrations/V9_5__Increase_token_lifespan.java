package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import io.vertx.await.Async;
import io.vertx.core.Vertx;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static io.vertx.await.Async.await;

public class V9_5__Increase_token_lifespan extends BaseJavaMigration {
	private static final int ONE_YEAR_IN_SECONDS = 365 * 24 * 60 * 60;  // 365 days in seconds
	private static Logger LOGGER = LoggerFactory.getLogger(V9_5__Increase_token_lifespan.class);

	@Override
	public void migrate(Context context) throws Exception {
		var vertx = Vertx.vertx();
		vertx.exceptionHandler(t -> LOGGER.error(t.toString(), t));
		var latch = new CountDownLatch(1);

		Async.vt(vertx, () -> {
			var realm = await(Accounts.realm());
			var realmRepr = realm.toRepresentation();

			// Set token lifespans
			realmRepr.setAccessTokenLifespan(ONE_YEAR_IN_SECONDS);
			realmRepr.setSsoSessionIdleTimeout(ONE_YEAR_IN_SECONDS);
			realmRepr.setSsoSessionMaxLifespan(ONE_YEAR_IN_SECONDS);
			realmRepr.setOfflineSessionIdleTimeout(ONE_YEAR_IN_SECONDS);
			realmRepr.setOfflineSessionMaxLifespan(ONE_YEAR_IN_SECONDS);

			realm.update(realmRepr);

			// First, find and disable RSA-OAEP key provider
			realm.components().query()
					.stream()
					.filter(component -> "org.keycloak.keys.KeyProvider".equals(component.getProviderType())
							&& "rsa-enc-generated".equals(component.getProviderId()))
					.forEach(component -> {
						component.getConfig().putSingle("enabled", "false");
						component.getConfig().putSingle("active", "false");
						realm.components().component(component.getId()).update(component);
					});

			// Create or update the RS256 provider if it doesn't exist
			var rs256Provider = new ComponentRepresentation();
			rs256Provider.setName("rsa-generated");
			rs256Provider.setProviderType("org.keycloak.keys.KeyProvider");
			rs256Provider.setProviderId("rsa-generated");
			rs256Provider.setConfig(new MultivaluedHashMap<>());
			rs256Provider.getConfig().putSingle("algorithm", "RS256");
			rs256Provider.getConfig().putSingle("enabled", "true");
			rs256Provider.getConfig().putSingle("active", "true");
			rs256Provider.getConfig().putSingle("priority", "100");

			// Try to create, if fails due to existence then update
			try {
				realm.components().add(rs256Provider);
			} catch (Exception e) {
				// If component already exists, find and update it
				realm.components().query()
						.stream()
						.filter(component -> "org.keycloak.keys.KeyProvider".equals(component.getProviderType())
								&& "rsa-generated".equals(component.getProviderId()))
						.forEach(component -> {
							component.getConfig().putAll(rs256Provider.getConfig());
							realm.components().component(component.getId()).update(component);
						});
			}

			// Update the realm

			latch.countDown();
			return null;
		});

		latch.await();
		vertx.close();
	}
}
