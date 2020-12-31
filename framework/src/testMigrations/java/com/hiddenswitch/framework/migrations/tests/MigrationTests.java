package com.hiddenswitch.framework.migrations.tests;

import com.hiddenswitch.containers.MongoDBContainer;
import com.hiddenswitch.framework.Gateway;
import com.hiddenswitch.framework.tests.applications.StandaloneApplication;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.Testcontainers.exposeHostPorts;

@ExtendWith(VertxExtension.class)
@Testcontainers
public class MigrationTests {

	@Container
	public static final MongoDBContainer MONGO = new MongoDBContainer()
			.withDatabaseName("metastone")
			.withFileSystemBind(System.getProperty("mongo.dbpath", ".mongo/"), "/data/db")
			.withReuse(false);

	@Test
	public void testLegacyMigration(Vertx vertx, VertxTestContext vertxTestContext) {
		System.getProperties().put("mongo.url", MONGO.getReplicaSetUrl());
		if (!StandaloneApplication.defaultConfigurationAndServices()) {
			return;
		}

		exposeHostPorts(Gateway.grpcPort());
		assertTrue(MONGO.isRunning(), "mongo successfully started");
		vertxTestContext.completeNow();
	}
}
