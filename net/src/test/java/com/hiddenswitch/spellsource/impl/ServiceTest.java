package com.hiddenswitch.spellsource.impl;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableRunnable;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public abstract class ServiceTest<T extends AbstractService<T>> {
	public static TestContext getContext() {
		return new com.hiddenswitch.spellsource.impl.Assert();
	}

	static TestContext wrappedContext;
	Logger logger = LoggerFactory.getLogger(ServiceTest.class);
	protected Vertx vertx;
	protected T service;

	public void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	@Before
	public void loadCards(TestContext context) {
		vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(20).setWorkerPoolSize(20));
		vertx.exceptionHandler(h -> {
			getContext().fail(h.getCause());
		});

		CardCatalogue.loadCardsFromPackage();

		Mongo.mongo().startEmbedded().connect(vertx);

		deployServices(vertx, context.asyncAssertSuccess(i -> {
			service = i;

			Async async = context.async();

			if (service != null
					&& service.getMongo() != null) {

				service.getMongo().getCollections(collections -> {
					final List<Future> futures = collections.result().stream().map(collection -> {
						Future<MongoClientDeleteResult> thisFuture = Future.future();
						service.getMongo().removeDocuments(collection, new JsonObject(), thisFuture.completer());
						return thisFuture;
					}).collect(Collectors.toList());

					CompositeFuture.join(futures).setHandler(then -> {
						async.complete();
					});
				});
			} else {
				async.complete();
			}
		}));
	}

	public abstract void deployServices(Vertx vertx, Handler<AsyncResult<T>> done);

	@Suspendable
	protected void wrapSync(TestContext context, SuspendableRunnable code) {
		ServiceTest.wrappedContext = context;
		final Async async = context.async();

		// Create a verticle on the fly to run sync stuff in, then tear down the verticle
		TestSyncVerticle testVerticle = new TestSyncVerticle(code);
		vertx.deployVerticle(testVerticle, getContext().asyncAssertSuccess(fut -> {
			vertx.undeploy(fut, then -> {
				ServiceTest.wrappedContext = null;
				async.complete();
			});
		}));
	}

	private static class TestSyncVerticle extends SyncVerticle {
		private final SuspendableRunnable code;

		public TestSyncVerticle(SuspendableRunnable code) {
			this.code = code;
		}

		@Override
		@Suspendable
		public void start() throws SuspendExecution, InterruptedException {
			code.run();
		}
	}

	protected void deploy(List<Verticle> dependencies, T thisService, Handler<AsyncResult<T>> handler) {
		final List<Future> verticles = dependencies.stream().map(verticle -> {
			Future<String> future = Future.future();
			vertx.deployVerticle(verticle, future.completer());
			return (Future) future;
		}).collect(Collectors.toList());

		CompositeFuture.join(verticles).setHandler(then -> {
			vertx.deployVerticle(thisService, then2 -> {
				handler.handle(Future.succeededFuture(thisService));
			});
		});
	}

	protected void wrap(TestContext context) {
		ServiceTest.wrappedContext = context;
	}

	protected void unwrap() {
		ServiceTest.wrappedContext = null;
	}

	@After
	public void destroyVertx(TestContext context) {
//		Async async = context.async();
		List<UnityClient> clients = context.get("clients");

		if (clients != null) {
			Iterator<UnityClient> iterator = clients.iterator();
			while (iterator.hasNext()) {
				iterator.next().disconnect();
				iterator.remove();
			}
		}

		vertx.close(context.asyncAssertSuccess(then -> {
			Mongo.mongo().stopEmbedded();
			Mongo.mongo().close();
			Spellsource.spellsource().close();
//			async.complete();
		}));
	}
}
