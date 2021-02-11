package com.hiddenswitch.framework.migrations.tests;

import com.google.common.collect.Sets;
import com.hiddenswitch.containers.MongoDBContainer;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Gateway;
import com.hiddenswitch.framework.migrations.V8__Migrate_from_previous_server;
import com.hiddenswitch.framework.schema.keycloak.tables.daos.UserEntityDao;
import com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity;
import com.hiddenswitch.framework.tests.applications.StandaloneApplication;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.hiddenswitch.framework.schema.keycloak.Tables.USER_ENTITY;
import static com.hiddenswitch.framework.schema.spellsource.Tables.BOT_USERS;
import static com.hiddenswitch.framework.schema.spellsource.Tables.USER_ENTITY_ADDONS;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
		var mongoUri = V8__Migrate_from_previous_server.getMongoUrl();
		var mongoClient = MongoClient.create(vertx, new JsonObject().put("connection_string", mongoUri));
		exposeHostPorts(Gateway.grpcPort());
		assertTrue(MONGO.isRunning(), "mongo successfully started");
		Environment.queryExecutor().findOneRow(dsl -> dsl.selectCount()
				.from(USER_ENTITY)
				.join(USER_ENTITY_ADDONS)
				.onKey()
				.where(USER_ENTITY_ADDONS.MIGRATED.eq(true)))
				.compose(row -> {
					var sqlCount = row.getInteger(0);
					return mongoClient.count(V8__Migrate_from_previous_server.USERS,
							new JsonObject().put("bot", new JsonObject().put("$ne", true))
									.put("emails", new JsonObject()
											.put("$exists", true)
											.put("$ne", new JsonArray())))
							.compose(mongoCount -> {
								vertxTestContext.verify(() -> {
									assertEquals(mongoCount, (long) sqlCount);
								});
								return Future.succeededFuture();
							});
				})
				.onComplete(vertxTestContext.succeedingThenComplete());
	}
}
