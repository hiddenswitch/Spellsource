package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Application;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GamesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static com.hiddenswitch.framework.schema.spellsource.Tables.GAMES;

public class EnvironmentTests extends FrameworkTestBase {

	@Test
	public void testPgPool(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var pool = Environment.pgPoolAkaDaoDelegate();
			pool.close().onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testPgPool2(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.pgPoolAkaDaoDelegate());
			dao.insert(new Games()).map((Void) null).onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testPgPool3(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.pgPoolForTransactionsAkaDaoDelegate());
			var transaction = dao.queryExecutor().beginTransaction();
			transaction.compose(res -> res.execute(dsl -> dsl.insertInto(GAMES).defaultValues()))
					.compose(v1 -> transaction.result().commit())
					.map((Void) null)
					.onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testPgPool4(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.pgPoolForTransactionsAkaDaoDelegate());
			var transaction = dao.queryExecutor().beginTransaction();
			transaction.onSuccess(res -> res.execute(dsl -> dsl.insertInto(GAMES).defaultValues()))
					.compose(v2 -> Environment.sleep(vertx, 8000L))
					.onComplete(promise);

		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testPgPool5(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.pgPoolAkaDaoDelegate());
			var transaction = dao.queryExecutor().beginTransaction();
			transaction.onSuccess(res -> res.execute(dsl -> dsl.insertInto(GAMES).defaultValues()));
		});
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.pgPoolAkaDaoDelegate());
			var transaction = dao.queryExecutor().beginTransaction();
			transaction.onSuccess(res -> res.execute(dsl -> dsl.insertInto(GAMES).defaultValues()))
					.compose(v2 -> Environment.sleep(vertx, 8000L))
					.onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testReadinessProbes(Vertx vertx, VertxTestContext vertxTestContext) {
		var application = new Application() {
			@Override
			protected Future<Vertx> getVertx() {
				return Future.succeededFuture(vertx);
			}
		};
		application.deploy()
				.compose(v -> {
					var webClient = WebClient.create(vertx);
					return CompositeFuture.all(webClient.get(8080, Environment.getHostIpAddress(), "/liveness")
									.timeout(900)
									.send(),
							webClient.get(8080, Environment.getHostIpAddress(), "/readiness")
									.timeout(900)
									.send()).map(v);
				})
				.onComplete(v -> v.result().close())
				.onComplete(vertxTestContext.succeedingThenComplete());
	}
}
