package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GamesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.SqlClient;
import org.junit.jupiter.api.Test;

import static com.hiddenswitch.framework.schema.spellsource.Tables.GAMES;

public class EnvironmentTests extends FrameworkTestBase {

	@Test
	public void testPgPool(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var pool = Environment.sqlPoolAkaDaoDelegate();
			pool.getConnection().compose(SqlClient::close).onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testPgPool2(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
			dao.insert(new Games()).map((Void) null).onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testPgPool3(VertxTestContext vertxTestContext) {
		var vertx = Vertx.vertx();
		var promise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
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
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
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
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
			var transaction = dao.queryExecutor().beginTransaction();
			transaction.onSuccess(res -> res.execute(dsl -> dsl.insertInto(GAMES).defaultValues()));
		});
		vertx.runOnContext(v -> {
			var dao = new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
			var transaction = dao.queryExecutor().beginTransaction();
			transaction.onSuccess(res -> res.execute(dsl -> dsl.insertInto(GAMES).defaultValues()))
					.compose(v2 -> Environment.sleep(vertx, 8000L))
					.onComplete(promise);
		});
		promise.future().compose(v -> vertx.close()).onComplete(vertxTestContext.succeedingThenComplete());
	}
}
