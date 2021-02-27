package com.hiddenswitch.framework.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.protos.Serialization;
import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;

import static com.hiddenswitch.protos.Serialization.configureSerialization;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
public class ConfigurationTests {

	@Test
	public void testConfigurationLoadsFromEnvironment(EnvironmentVariables variables) {
		configureSerialization();
		variables.set("FAILS_XXX_YYY", "fails");
		var configuration = Hiddenswitch.ServerConfiguration.newBuilder()
				.setRealtime(Hiddenswitch.ServerConfiguration.RealtimeConfiguration.newBuilder().setUri("realtime://other").build())
				.setApplication(Hiddenswitch.ServerConfiguration.ApplicationConfiguration.newBuilder().setUseBroadcaster(false).build())
				.build();
		assertThrows(IllegalArgumentException.class, () -> {
			ModelConversions.fromStringMap(configuration, "fails", "_", System.getenv());
		}, "fail with invalid environment variable (misconfiguration)");
		variables.set("SPELLSOURCE_APPLICATION_USEBROADCASTER", "true");
		variables.set("SPELLSOURCE_REDIS_HOSTPORTUSER_PORT", "10000");
		variables.set("SPELLSOURCE_REALTIME_URI", "http://123.com");
		var result = ModelConversions.fromStringMap(configuration, "spellsource", "_", System.getenv());
		assertTrue(result.getApplication().getUseBroadcaster());
		assertEquals(10000, result.getRedis().getHostPortUser().getPort());
		assertEquals("http://123.com", result.getRealtime().getUri());
		assertFalse(result.hasGrpcConfiguration());
	}

	@Test
	public void testDecodesToProtobuf() throws JsonProcessingException {
		Serialization.configureSerialization();
		var yaml = "pg:\n" +
				"  host: \"postgres-postgresql-ha-pgpool.default.svc.cluster.local\"\n" +
				"  user: \"spellsource\"\n" +
				"  password: \"password\"\n" +
				"keycloak:\n" +
				"  # keycloak port is 8080 by default on bitnami images, and we override it to 9090\n" +
				"  authUrl: \"http://keycloak.default.svc.cluster.local:9090/auth/admin\"\n" +
				"  adminUsername: \"admin\"\n" +
				"  adminPassword: \"password\"\n" +
				"redis:\n" +
				"  uri: \"redis://redis-master.default.svc.cluster.local:6379\"";

		var json = DatabindCodec.mapper().writeValueAsString(Environment.defaultConfiguration());
		var configMap = Serialization.yamlMapper().readValue(yaml, Map.class);
		var config1 = Serialization.yamlMapper().readValue(yaml, Hiddenswitch.ServerConfiguration.class);
		var config2 = DatabindCodec.mapper().convertValue(configMap, Hiddenswitch.ServerConfiguration.class);
		Assertions.assertEquals("http://keycloak.default.svc.cluster.local:9090/auth/admin", ((Map) configMap.get("keycloak")).get("authUrl"));
		for (var config : new Hiddenswitch.ServerConfiguration[] {config1, config2}) {
			Assertions.assertEquals("http://keycloak.default.svc.cluster.local:9090/auth/admin", config.getKeycloak().getAuthUrl());
		}
	}
}
