package com.hiddenswitch.framework;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hiddenswitch.framework.impl.RealtimeClient;
import com.hiddenswitch.framework.model.DiffContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectFinalStep;
import org.jooq.TableField;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class Realtime {

	private final static Map<String, PublicationImpl<?, ?>> publications = new ConcurrentSkipListMap<>();
	private final static LoadingCache<ChannelSpec, Future<RealtimeClient.Channel>> channels = CacheBuilder.newBuilder()
			.build(new CacheLoader<>() {
				@Override
				public Future<RealtimeClient.Channel> load(ChannelSpec topic) {
					var realtimeUri = Environment.cachedConfigurationOrGet().getRealtime().getUri();
					var socket = new RealtimeClient(realtimeUri);
					return socket.connect()
							.compose(client -> socket.channel(topic.getSchema(), topic.getTable(), topic.getWhereColumn(), topic.getEqualsValue()).joined());
				}
			});

	public static <V, R extends Record> Future<Publication<R>> publish(String name, Function<Session, Future<List<R>>> first, org.jooq.Table<R> table, TableField<R, V> whereColumn, Function<Session, V> equalsValue) {
		if (publications.containsKey(name)) {
			return Future.failedFuture(new ArrayStoreException(name + " already exists"));
		}
		var value = new PublicationImpl<>(name, first, table, whereColumn, equalsValue);
		publications.put(name, value);
		return Future.succeededFuture(value);
	}

	public static <R extends Record, V> Future<Subscription<R>> subscribe(Session session, String name, Class<? extends R> recordClass, Class<V> joinKey) {
		if (!publications.containsKey(name)) {
			return Future.failedFuture(new NullPointerException(name + " does not exist"));
		}

		@SuppressWarnings("unchecked")
		var publication = (PublicationImpl<R, V>) publications.get(name);
		Future<RealtimeClient.Channel> joinChannel;
		try {
			joinChannel = channels.get(new ChannelSpecImpl(publication.getTable().getName(),
					publication.getTable().getSchema().getName(),
					publication.getWhereColumn().getName(),
					publication.getEqualsValue().apply(session).toString()));
		} catch (ExecutionException executionException) {
			return Future.failedFuture(executionException);
		}

		var table = session.table(recordClass);

		var subscription = new SubscriptionImpl<R>();

		return joinChannel
				.compose(channel -> {
					channel
							.inserted((changes, record) -> {
								table.added(table.getKeyer().apply(record), record);
							})
							.updated((changes, record) -> {
								table.possiblyChanged(table.getKeyer().apply(record), new JsonObject(changes.getOldRecord()), record);
							})
							.deleted((changes, record) -> {
								table.removed(table.getKeyer().apply(record));
							});

					return Future.succeededFuture();
				})
				.compose(ignored -> Future.succeededFuture(subscription));
	}

	public interface LocalTable<RECORD extends Record> extends DiffContext<JsonObject, String> {

	}

	public interface Session {
		String userId();

		<R extends Record> LocalTable<R> table(Class<? extends R> recordType);

		static Session forUserId(String userId) {
			return new SessionImpl(userId);
		}
	}

	private static class ChannelSpecImpl implements ChannelSpec {
		private final String table;
		private final String schema;
		private final String whereEquals;
		private final String whereColumn;

		public ChannelSpecImpl(String table, String schema, String whereColumn, String whereEquals) {
			this.table = table;
			this.schema = schema;
			this.whereEquals = whereEquals;
			this.whereColumn = whereColumn;
		}

		@Override
		public String getTable() {
			return table;
		}

		@Override
		public String getSchema() {
			return schema;
		}

		@Override
		public String getEqualsValue() {
			return whereEquals;
		}

		@Override
		public String getWhereColumn() {
			return whereColumn;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ChannelSpecImpl)) return false;
			ChannelSpec that = (ChannelSpec) o;
			return getTable().equals(that.getTable()) &&
					getSchema().equals(that.getSchema()) &&
					getEqualsValue().equals(that.getEqualsValue()) &&
					getWhereColumn().equals(that.getWhereColumn());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getTable(), getSchema(), getEqualsValue(), getWhereColumn());
		}
	}

	static class SessionImpl implements Session {

		private final String userId;

		public SessionImpl(String userId) {
			this.userId = userId;
		}

		@Override
		public String userId() {
			return userId;
		}

		@Override
		public <R extends Record> LocalTable<R> table(Class<? extends R> recordType) {
			return null;
		}
	}

	public interface Subscription<R extends Record> {


	}

	public interface Publication<R extends Record> {

	}

	static class SubscriptionImpl<R extends Record> implements Subscription<R> {

	}

	static class PublicationImpl<R extends Record, V> implements Publication<R> {

		private final String name;
		private final Function<Session, Future<List<R>>> first;
		private final org.jooq.Table<R> table;
		private final TableField<R, V> whereColumn;
		private final Function<Session, V> equalsValue;

		public PublicationImpl(String name, Function<Session, Future<List<R>>> first, org.jooq.Table<R> table, TableField<R, V> whereColumn, Function<Session, V> equalsValue) {
			this.name = name;
			this.first = first;
			this.table = table;
			this.whereColumn = whereColumn;
			this.equalsValue = equalsValue;
		}

		public String getName() {
			return name;
		}

		public Function<Session, Future<List<R>>> getFirst() {
			return first;
		}

		public org.jooq.Table<R> getTable() {
			return table;
		}

		public TableField<R, V> getWhereColumn() {
			return whereColumn;
		}

		public Function<Session, V> getEqualsValue() {
			return equalsValue;
		}
	}

	public static interface Changes {
		Map<String, Object> getOldRecord();

		Map<String, Object> getRecord();

		String getSchema();

		String getTable();

		RealtimeClient.PayloadType getType();

		Object getColumns();
	}

	@FunctionalInterface
	public interface QueryFunction<R extends Record> {
		SelectFinalStep<R> apply(DSLContext context, Session session);
	}

	public static interface ChannelSpec {
		String getTable();

		String getSchema();

		String getEqualsValue();

		String getWhereColumn();
	}
}
