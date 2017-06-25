package com.hiddenswitch.proto3;

import com.hiddenswitch.proto3.net.Migrations;
import com.hiddenswitch.proto3.net.impl.MigrationsImpl;
import com.hiddenswitch.proto3.net.models.MigrateToRequest;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import com.hiddenswitch.proto3.net.util.Mongo;
import com.hiddenswitch.proto3.net.util.RPC;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.stream.Collectors;

import static com.hiddenswitch.proto3.net.util.QuickJson.json;

/**
 * Note that the collection names should all be different because I'm lazy with the collection removal.
 */
@RunWith(VertxUnitRunner.class)
public class MigrationTest {
	private Vertx vertx = Vertx.vertx();

	@Before
	public void cleanup(TestContext context) {
		Mongo.mongo().connectWithEnvironment(vertx).client().getCollections(context.asyncAssertSuccess(then -> {
			CompositeFuture cf = CompositeFuture.all(then.stream().map(collection -> {
				Future<MongoClientDeleteResult> future = Future.future();
				Mongo.mongo().client().removeDocuments(collection, json(), future.completer());
				return future;
			}).collect(Collectors.toList())).setHandler(context.asyncAssertSuccess());
		}));
	}

	@Test
	public void testDeployAndMigrate(final TestContext context) {

		MigrationsImpl migrations = new MigrationsImpl();

		vertx.deployVerticle(migrations, context.asyncAssertSuccess(then -> {
			RpcClient<Migrations> migrator = RPC.connect(Migrations.class, vertx.eventBus());

			CompositeFuture waterfall = CompositeFuture.join(
					migrator.promise(service ->
							service.add(new MigrationRequest()
									.withVersion(2)
									.withUp((thisVertx) -> {
										Mongo.mongo().insert("testcollection3", json("_id", "test2"));
									}))),
					migrator.promise(service ->
							service.add(new MigrationRequest()
									.withVersion(1)
									.withUp((thisVertx) -> {
										Mongo.mongo().createCollection("testcollection3");
										Mongo.mongo().insert("testcollection3", json("_id", "test"));
									}))),
					migrator.promise(service -> service.migrateTo(new MigrateToRequest().withLatest(true)))
			);

			waterfall.setHandler(context.asyncAssertSuccess(then2 -> {
				Mongo.mongo().client().count("testcollection3", json(), context.asyncAssertSuccess(then3 -> {
					context.assertEquals(then3, 2L);
				}));
			}));
		}));
	}

	@Test
	public void testAndMigrateWithAPI(final TestContext context) {
		Migrations.migrate(vertx)
				.add(new MigrationRequest()
						.withVersion(1)
						.withUp((thisVertx) -> {
							Mongo.mongo().createCollection("testcollection");
							Mongo.mongo().insert("testcollection", json("_id", "test"));
						}))
				.add(new MigrationRequest()
						.withVersion(2)
						.withUp((thisVertx) -> {
							Mongo.mongo().insert("testcollection", json("_id", "test2"));
						}))
				.migrateTo(2, context.asyncAssertSuccess(then -> {
					Mongo.mongo().client().count("testcollection", json(), context.asyncAssertSuccess(then3 -> {
						context.assertEquals(then3, 2L);
					}));
				}));
	}

	@Test
	public void testInOrder(final TestContext context) {
		final Vertx vertx = Vertx.vertx();

		Migrations.migrate(vertx)
				.add(new MigrationRequest()
						.withVersion(3)
						.withUp(thisVertx -> {
							Mongo.mongo().updateCollection("testCollection1", json("_id", "test"), json("$set", json("value", 3)));
						}))
				.add(new MigrationRequest()
						.withVersion(4)
						.withUp(thisVertx -> {
							Mongo.mongo().updateCollection("testCollection1", json("_id", "test"), json("$set", json("value", 4)));
						}))
				.add(new MigrationRequest()
						.withVersion(2)
						.withUp(thisVertx -> {
							Mongo.mongo().updateCollection("testCollection1", json("_id", "test"), json("$set", json("value", 2)));
						}))
				.add(new MigrationRequest()
						.withVersion(1)
						.withUp(thisVertx -> {
							Mongo.mongo().createCollection("testCollection1");
							Mongo.mongo().insert("testCollection1", json("_id", "test", "value", 1));
						}))
				.migrateTo(4, context.asyncAssertSuccess(then -> {
					Mongo.mongo().client().findOne("testCollection1", json("_id", "test"), json(), context.asyncAssertSuccess(then2 -> {
						context.assertEquals(then2.getInteger("value"), 4);
					}));
				}));
	}
}
