package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Environment;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.vertx.await.Async.await;

public abstract class RoutineCaller<T extends RoutineCaller<T>> {

	protected String userId;

	protected String role;

	protected abstract String getQuery();

	protected DSLContext dsl = Environment.jooqAkaDaoConfiguration().dsl();

	@SuppressWarnings("SqlSourceToSinkFlow")
	private <V> Future<Stream<V>> execute(Function<Row, V> mapper) {
		return Environment.withConnection(conn -> conn.begin().compose(tx -> {
			Future<?> setup = Future.succeededFuture();
			if (userId != null) {
				setup = setup.compose(_ -> conn.query(dsl.setLocal(DSL.name("user.id"), DSL.value(userId)).getSQL(ParamType.INLINED)).execute());
			}
			if (role != null) {
				setup = setup.compose(_ -> conn.query(dsl.setLocal(DSL.name("role"), DSL.value(role)).getSQL(ParamType.INLINED)).execute());
			}
			return setup.compose(_ -> conn.query(getQuery()).execute())
					.map(rows -> StreamSupport.stream(rows.spliterator(), false).map(mapper))
					.compose(
							result -> tx.commit().map(result),
							err -> tx.rollback().otherwiseEmpty().compose(_ -> Future.failedFuture(err))
					);
		}));
	}

	public T withUserId(String userId) {
		this.userId = userId;
		//noinspection unchecked
		return (T) this;
	}

	public T withRole(String role) {
		this.role = role;
		//noinspection unchecked
		return (T) this;
	}


	public static class ForField<T extends ForField<T, U>, U> extends RoutineCaller<T> {

		private final Field<U> field;
		private boolean selectAsterisk;

		public ForField(Field<U> field) {
			this.field = field;
		}

		@Override
		protected String getQuery() {
			return (selectAsterisk ? dsl.select(DSL.asterisk()).from(field.toString()) : dsl.select(field)).getSQL(ParamType.INLINED);
		}

		public <V> Future<V> execute(Function<Row, V> mapper) {
			selectAsterisk = true;

			return super.execute(mapper).map(rows -> rows.findFirst().orElseThrow());
		}

		public Future<U> execute() {
			return super.execute(row -> row.get(field.getType(), 0)).map(rows -> rows.findFirst().orElseThrow());
		}

	}

	public static class ForTable<T extends ForTable<T, U>, U extends Record> extends RoutineCaller<T> {

		private final Table<U> table;

		public ForTable(Table<U> table) {
			this.table = table;
		}

		@Override
		protected String getQuery() {
			return dsl.select(DSL.asterisk()).from(table).getSQL(ParamType.INLINED);
		}

		public <V> Future<List<V>> execute(Function<Row, V> mapper) {
			return super.execute(mapper).map(Stream::toList);
		}

	}

}