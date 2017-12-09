package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.common.Recursive;
import com.hiddenswitch.spellsource.impl.MigrationsImpl;
import com.hiddenswitch.spellsource.models.MigrateToRequest;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.RpcClient;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitEvent;

/**
 * Note that the collection names should all be different because I'm lazy with the collection removal.
 */
@RunWith(VertxUnitRunner.class)
public class MigrationTest {
	private Vertx vertx;

	@Before
	public void startEmbeddedMongo(TestContext context) {
		vertx = Vertx.vertx();
		Mongo.mongo().startEmbedded().connectWithEnvironment(vertx);
		Async async = context.async();
		vertx.runOnContext(cleanupContext -> {
			cleanup(Future.future().setHandler(context.asyncAssertSuccess(then -> {
				async.complete();
			})));
		});
	}

	public void cleanup(final Future done) {
		Mongo.mongo().client().getCollections(collections -> {
			CompositeFuture cf = CompositeFuture.all(collections.result().stream().map(collection -> {
				Future<Void> future = Future.future();
				Mongo.mongo().client().dropCollection(collection, future.completer());
				return future;
			}).collect(Collectors.toList())).setHandler(then2 -> {
				Spellsource.spellsource().close();
				Mongo.mongo().close();
				done.complete();
			});
		});
	}

	@After
	public void done(TestContext context) {
		vertx.runOnContext(cleanupContext -> {
			cleanup(Future.future().setHandler(context.asyncAssertSuccess(then -> {
				vertx.close(context.asyncAssertSuccess());
			})));
		});
	}

	@Test
	public void testDeployAndMigrate(final TestContext context) {
		MigrationsImpl migrations = new MigrationsImpl();

		vertx.deployVerticle(migrations, context.asyncAssertSuccess(then -> {
			RpcClient<Migrations> migrator = Rpc.connect(Migrations.class, vertx.eventBus());

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

	@Test
	public void testLongMigration(final TestContext context) {
		Migrations.migrate(vertx)
				.add(new MigrationRequest()
						.withVersion(1)
						.withUp(thisVertx -> {
							Mongo.mongo().createCollection("testCollection88");
							Mongo.mongo().insert("testCollection88", json("_id", "test", "value", 1));
							Long r = awaitEvent(h -> thisVertx.setTimer(Duration.of(32, ChronoUnit.SECONDS).toMillis(), h));
						}))
				.migrateTo(1, context.asyncAssertSuccess(then -> {
					Mongo.mongo().client().findOne("testCollection88", json("_id", "test"), json(), context.asyncAssertSuccess(then2 -> {
						context.assertEquals(then2.getInteger("value"), 1);
					}));
				}));
	}

	public void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	@Test
	@Ignore
	public void testAllMigrations(final TestContext context) {
		setLoggingLevel(Level.ERROR);

		// Download production database. Requires a working mongodump url
		vertx.executeBlocking(done -> {
			Process mongodump = null;
			try {
				// The passwords that used to be here are invalid anyway.
				mongodump = new ProcessBuilder("mongodump", "--host=localhost", "--port=20276", "--db=production", "--ssl", "--sslAllowInvalidCertificates", "--sslAllowInvalidHostnames").start();
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
			Spellsource.spellsource().migrate(vertx, context.asyncAssertSuccess(then2 -> {
				// Assert a game still works
				Spellsource.spellsource().deployAll(vertx, context.asyncAssertSuccess(then3 -> {
					Mongo.mongo().client().findOne(MigrationsImpl.MIGRATIONS, json(), json(), context.asyncAssertSuccess(doc -> {
						context.assertNotNull(doc.getBoolean("locked"));
						context.assertFalse(doc.getBoolean("locked"));
						context.assertEquals(doc.getInteger("version"), 3);
						vertx.executeBlocking(done -> {
							new UnityClient(context).createUserAccount(null).matchmakeAndPlayAgainstAI(null).waitUntilDone();
							done.handle(Future.succeededFuture());
						}, context.asyncAssertSuccess(andThen -> {
							undeploy(context);
						}));
					}));
				}));
			}));
		}));
	}

	public void undeploy(TestContext context) {
		// Recursively undeploy the currently deployed verticles.
		Recursive<Consumer<String>> r = new Recursive<>();
		r.func = (String deploymentId) -> {
			vertx.undeploy(deploymentId, context.asyncAssertSuccess(then -> {
				Set<String> moreDeployments = vertx.deploymentIDs();
				if (moreDeployments.size() != 0) {
					r.func.accept(moreDeployments.iterator().next());
				}
			}));
		};

		if (vertx.deploymentIDs().size() == 0) {
			return;
		} else {
			final String next = vertx.deploymentIDs().iterator().next();
			r.func.accept(next);
		}
	}
}
