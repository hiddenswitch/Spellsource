package com.hiddenswitch.proto3;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.minionate.Minionate;
import com.hiddenswitch.proto3.net.Accounts;
import com.hiddenswitch.proto3.net.ClusterTest;
import com.hiddenswitch.proto3.net.Decks;
import com.hiddenswitch.proto3.net.Migrations;
import com.hiddenswitch.proto3.net.client.models.Account;
import com.hiddenswitch.proto3.net.client.models.InventoryCollection;
import com.hiddenswitch.proto3.net.impl.MigrationsImpl;
import com.hiddenswitch.proto3.net.models.DeckDeleteRequest;
import com.hiddenswitch.proto3.net.models.MigrateToRequest;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import com.hiddenswitch.proto3.net.util.Mongo;
import com.hiddenswitch.proto3.net.util.RPC;
import com.hiddenswitch.proto3.net.util.RpcClient;
import com.hiddenswitch.proto3.net.util.UnityClient;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.hiddenswitch.proto3.net.util.QuickJson.json;

/**
 * Note that the collection names should all be different because I'm lazy with the collection removal.
 */
@RunWith(VertxUnitRunner.class)
public class MigrationTest {
	private static Vertx vertx = Vertx.vertx();

	@BeforeClass
	public static void startEmbeddedMongo() {
		Mongo.mongo().startEmbedded().connect(vertx, "mongodb://localhost:27017/production");
	}

	@After
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
					if (then.failed()) {
						context.fail(then.cause());
					}
					Mongo.mongo().client().count("testcollection", json(), context.asyncAssertSuccess(then3 -> {
						context.assertEquals(then3, 2L);
					}));
				}));
	}

	@Test
	public void testInOrder(final TestContext context) {
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

	public void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	@Test
	public void testAllMigrations(final TestContext context) {
		setLoggingLevel(Level.ERROR);

		// Download production database. Requires a working mongodump url
		vertx.executeBlocking(done -> {
			Process mongodump = null;
			try {
				mongodump = new ProcessBuilder("mongodump", "--username=spellsource1", "--password=9AD3uubaeIf71a4M11lPVAV2mJcbPzV1EC38Y4WF26M", "--host=aws-us-east-1-portal.9.dblayer.com", "--port=20276", "--db=production", "--ssl", "--sslAllowInvalidCertificates", "--sslAllowInvalidHostnames").start();
				int waitFor1 = mongodump.waitFor();
				Process mongorestore = new ProcessBuilder("mongorestore", "--host=localhost", "--port=27017", "dump").start();
				int waitFor2 = mongorestore.waitFor();
				done.handle((waitFor1 == 0 && waitFor2 == 0) ? Future.succeededFuture() : Future.failedFuture("Didn't restore or dump correctly."));
				return;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			done.handle(Future.failedFuture("MongoDump failed."));
		}, context.asyncAssertSuccess(then -> {
			// Then deploy locally with production
			Minionate.minionate().migrate(vertx, context.asyncAssertSuccess(then2 -> {
				// Assert a game still works
				Minionate.minionate().deployAll(vertx, context.asyncAssertSuccess(then3 -> {
					// Query for existing decks and assert


					vertx.executeBlocking(done -> {
						new UnityClient(context).createUserAccount(null).matchmakeAndPlayAgainstAI(null).waitUntilDone();
						done.handle(Future.succeededFuture());
					}, context.asyncAssertSuccess());
				}));
			}));
		}));


	}
}
