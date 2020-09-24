package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hiddenswitch.framework.Realtime;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class RealtimeClient implements AutoCloseable {
	private static final LoadingCache<String, Source> sources = CacheBuilder.newBuilder()
			.weakValues()
			.build(new CacheLoader<>() {
				@Override
				public Source load(String key) throws IOException {
					var bundle = RealtimeClient.class.getClassLoader().getResource(key);
					return Source.newBuilder("js", bundle).build();
				}
			});
	private static final ThreadLocal<Context> context = ThreadLocal.withInitial(() -> {
		try {
			var newContext = Context.newBuilder("js")
					.allowExperimentalOptions(true)
					.option("js.nashorn-compat", "true")
					.allowHostClassLookup(className -> true)
					.allowHostAccess(HostAccess.newBuilder()
							.allowPublicAccess(true)
							.allowAllImplementations(true)
							.allowArrayAccess(true)
							.allowListAccess(true)
							.targetTypeMapping(JsonObject.class, Value.class, obj -> true, input -> input == null ? null : Value.asValue(input.getMap()))
							.targetTypeMapping(Value.class, JsonObject.class,
									v -> v.hasMembers() || v.hasArrayElements(),
									input -> JsonObject.mapFrom(input.as(Map.class)))
							.build())
					.build();

			for (var javascript : new String[]{"polyfills.js", "realtime-js.js"}) {
				newContext.eval(sources.get(javascript));
			}
			return newContext;

		} catch (ExecutionException executionException) {
			throw new RuntimeException(executionException);
		}
	});

	private Value socket;
	private String realtimeUrl;
	private CompletableFuture<RealtimeClient> future;
	private Promise<RealtimeClient> socketPromise;

	public CompletableFuture<RealtimeClient> getFuture() {
		return future;
	}

	public RealtimeClient(String realtimeUrl) {
		this.realtimeUrl = realtimeUrl;
	}

	public Future<RealtimeClient> connect() {
		if (socket != null) {
			var future = socketPromise.future();
			if (future.isComplete()) {
				return future.succeeded() ? Future.succeededFuture(this) : Future.failedFuture(future.cause());
			}
			return socketPromise.future();
		}
		var currentContext = context.get();
		var constructorFuture = new CompletableFuture<JavascriptWebSocket>();
		JavascriptWebSocket.setConstructorFuture(constructorFuture);
		socket = currentContext.getBindings("js")
				.getMember("Socket")
				.newInstance(realtimeUrl, currentContext.eval("js", "{transport: WebSocket}"));
		future = constructorFuture.thenApply(ws -> this);
		socketPromise = Promise.<RealtimeClient>promise();
		var socket = this;
		socket.onOpen(() -> socketPromise.complete(socket));
		socket.onError(error -> socketPromise.fail(error.toString()));
		try {
			this.socket.getMember("connect").executeVoid();
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
		getFuture().whenComplete((socket1, throwable) -> {
			if (throwable != null) {
				socketPromise.fail(throwable);
			}
		});
		return socketPromise.future();
	}

	public Channel channel(String topic) {
		return new Channel(socket.getMember("channel").execute(topic));
	}

	public Channel channel(String schema, String table) {
		return channel("realtime:" + schema + ":" + table);
	}

	public Channel channel(String schema, String table, String whereColumn, String equalsValue) {
		return channel("realtime:" + schema + ":" + table + "." + whereColumn + "=eq." + equalsValue);
	}

	public void onOpen(Runnable callback) {
		socket.getMember("onOpen").executeVoid(callback);
	}

	public void onClose(Runnable callback) {
		socket.getMember("onClose").executeVoid(callback);
	}

	public void onError(ValueHandler callback) {
		socket.getMember("onError").executeVoid(callback);
	}

	public void onMessage(ValueHandler callback) {
		socket.getMember("onMessage").executeVoid(callback);
	}

	@Override
	public void close() {
		socket.getMember("reconnectTimer").getMember("reset").executeVoid();
		socket.getMember("disconnect").execute((Runnable) () -> {
			socket = null;
		});
	}

	public static class Push {
		public final Value push;

		protected Push(Value push) {
			this.push = push;
		}

		public Push receive(String event, ValueHandler callback) {
			push.getMember("receive").executeVoid(event, callback);
			return this;
		}
	}

	public enum PayloadType {
		INSERT,
		UPDATE,
		DELETE,
		TRUNCATED,
		TRANSACTION
	}

	@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static final class ColumnRelation {
		private String name;
		private String type;
		private long type_modifier;

		public String getName() {
			return name;
		}

		public ColumnRelation setName(String name) {
			this.name = name;
			return this;
		}

		public String getType() {
			return type;
		}

		public ColumnRelation setType(String type) {
			this.type = type;
			return this;
		}

		public long getTypeModifier() {
			return type_modifier;
		}

		public ColumnRelation setTypeModifier(long type_modifier) {
			this.type_modifier = type_modifier;
			return this;
		}
	}

	@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static final class RealtimeChanges implements Realtime.Changes {
		private Map<String, Object> record;
		private Map<String, Object> old_record;
		private String commit_timestamp;
		private String schema;
		private String table;
		private PayloadType type;
		private Object columns;

		@Override
		public Map<String, Object> getOldRecord() {
			return old_record;
		}

		public Realtime.Changes setOldRecord(Map<String, Object> old_record) {
			this.old_record = old_record;
			return this;
		}

		@Override
		public Map<String, Object> getRecord() {
			return record;
		}

		public Realtime.Changes setRecord(Map<String, Object> record) {
			this.record = record;
			return this;
		}

		public String getCommitTimestamp() {
			return commit_timestamp;
		}

		public Realtime.Changes setCommitTimestamp(String commit_timestamp) {
			this.commit_timestamp = commit_timestamp;
			return this;
		}

		@Override
		public String getSchema() {
			return schema;
		}

		public Realtime.Changes setSchema(String schema) {
			this.schema = schema;
			return this;
		}

		@Override
		public String getTable() {
			return table;
		}

		public Realtime.Changes setTable(String table) {
			this.table = table;
			return this;
		}

		@Override
		public PayloadType getType() {
			return type;
		}

		public Realtime.Changes setType(PayloadType type) {
			this.type = type;
			return this;
		}

		@Override
		public Object getColumns() {
			return columns;
		}

		public Realtime.Changes setColumns(Object columns) {
			this.columns = columns;
			return this;
		}
	}

	public static final class Channel implements AutoCloseable {
		public static final String ANY = "*";
		public static final String INSERT = "INSERT";
		public static final String UPDATE = "UPDATE";
		public static final String DELETE = "DELETE";
		private final Value channel;
		private Promise<Channel> promise;

		protected Channel(Value channel) {
			this.channel = channel;
		}

		public synchronized Future<Channel> joined() {
			if (promise != null) {
				var future = promise.future();
				if (future.isComplete()) {
					return future.succeeded() ? Future.succeededFuture(future.result()) : Future.failedFuture(future.cause());
				}
				return future;
			}
			promise = Promise.promise();
			join().receive("ok", ignored -> promise.complete(this))
					.receive("error", message -> promise.fail(new RuntimeException(message == null ? "(null)" : message.as(JsonObject.class).encodePrettily())));
			return promise.future();
		}

		public Push join() {
			return new Push(channel.getMember("join").execute());
		}

		public Channel on(String event, ChannelEventHandler handler) {
			channel.getMember("on").executeVoid(event, handler);
			return this;
		}

		public Channel inserted(ChannelTypedEventHandler handler) {
			var handler1 = (ChannelEventHandler)handler;
			channel.getMember("on").executeVoid(INSERT, handler1);
			return this;
		}

		public Channel updated(ChannelTypedEventHandler handler) {
			var handler1 = (ChannelEventHandler)handler;
			channel.getMember("on").executeVoid(UPDATE, handler1);
			return this;
		}

		public Channel deleted(ChannelTypedEventHandler handler) {
			var handler1 = (ChannelEventHandler)handler;
			channel.getMember("on").executeVoid(DELETE, handler1);
			return this;
		}

		public Channel off(String event) {
			channel.getMember("off").executeVoid(event);
			return this;
		}

		public Channel leave() {
			channel.getMember("leave").executeVoid();
			return this;
		}

		@Override
		public void close() throws Exception {
			leave();
		}
	}

	@FunctionalInterface
	public interface ValueHandler extends Handler<Value> {
		@Override
		void handle(Value event);
	}

	@FunctionalInterface
	public interface ChannelEventHandler extends BiConsumer<JsonObject, Value> {

		@Override
		void accept(JsonObject handledPayload, Value ref);
	}

	@FunctionalInterface
	public interface ChannelTypedEventHandler extends ChannelEventHandler {

		@Override
		default void accept(JsonObject handledPayload, Value ref) {
			var changes = handledPayload.mapTo(RealtimeClient.RealtimeChanges.class);
			var record = new JsonObject(changes.getRecord());
			onAccept(changes, record);
		}

		void onAccept(Realtime.Changes changes, JsonObject record);
	}

	/**
	 * Implements the Web Sockets API specified in https://html.spec.whatwg.org/multipage/web-sockets.html The class is a
	 * wrapper around the Java 9 HttpClient API
	 */
	public static class JavascriptWebSocket implements WebSocket.Listener {
		private static CompletableFuture<JavascriptWebSocket> constructorFuture;
		private static HttpClient httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.build();

		public static CompletableFuture<JavascriptWebSocket> getConstructorFuture() {
			return constructorFuture;
		}

		public static void setConstructorFuture(CompletableFuture<JavascriptWebSocket> constructorFuture) {
			JavascriptWebSocket.constructorFuture = constructorFuture;
		}

		private CompletableFuture<WebSocket> wsFuture;
		private WebSocket webSocket = null;
		private String url;

		// ready state
		public static final int CONNECTING = 0;
		public static final int OPEN = 1;
		public static final int CLOSING = 2;
		public static final int CLOSED = 3;
		private int readyState = CLOSED;
		private long bufferedAmount = 0; // TODO: not implemented yet

		// networking
		public Consumer<Event> onopen;
		public Consumer<Event> onerror;
		public Consumer<Event> onclose;
		private String extensions = ""; // TODO: not implemented yet
		private String protocol = "";

		// messaging
		public Consumer<Event> onmessage;
		private String binaryType = BinaryType.BLOB;

		public JavascriptWebSocket(String url, String... protocols) {
			this.url = url;
			WebSocket.Builder wsBuilder = httpClient.newWebSocketBuilder();

			if (protocols.length > 0) {
				wsBuilder.subprotocols(protocols[0], protocols);
			}
			readyState = CONNECTING;
			var constructorFuture = getConstructorFuture();
			setConstructorFuture(null);
			wsFuture = wsBuilder.buildAsync(URI.create(url), this)
					.whenComplete((sock, throwable) -> {
						if (throwable != null) {
							constructorFuture.completeExceptionally(throwable);
							throwable.printStackTrace();
							readyState = CLOSED;
							JavascriptWebSocket.this.onError(sock, throwable);
						} else {
							constructorFuture.complete(JavascriptWebSocket.this);
						}
					});
		}

		public String getUrl() {
			return url;
		}

		public int getReadyState() {
			return readyState;
		}

		public String getExtensions() {
			return extensions;
		}

		public String getProtocol() {
			return protocol;
		}

		public long getBufferedAmount() {
			return bufferedAmount;
		}

		public void close() {
			if (webSocket == null) {
				wsFuture.cancel(true);
			} else if (readyState != CLOSED && readyState != CLOSING) {
				webSocket.sendClose(1000, "");
			}
			readyState = CLOSING;
		}

		public void close(String reason) {
			checkCloseReason(reason);
			if (webSocket == null) {
				wsFuture.cancel(true);
			} else if (readyState != CLOSED && readyState != CLOSING) {
				webSocket.sendClose(1000, reason);
			}
			readyState = CLOSING;
		}

		/**
		 * @param closeCode see https://tools.ietf.org/html/rfc6455#section-11.7
		 */
		public void close(int closeCode) {
			checkCloseCode(closeCode);
			if (webSocket == null) {
				wsFuture.cancel(true);
			} else if (readyState != CLOSED && readyState != CLOSING) {
				webSocket.sendClose(closeCode, "");
			}
			readyState = CLOSING;
		}

		/**
		 * @param closeCode see https://tools.ietf.org/html/rfc6455#section-11.7
		 */
		public void close(int closeCode, String reason) {
			checkCloseCode(closeCode);
			checkCloseReason(reason);
			if (webSocket == null) {
				wsFuture.cancel(true);
			} else if (readyState != CLOSED && readyState != CLOSING) {
				webSocket.sendClose(closeCode, reason);
			}
			readyState = CLOSING;
		}

		// Only sending text is supported at the moment
		// TODO: consider adding support for the following overloads (i.e., binary data):
		// void send(Blob data);
		// void send(ArrayBuffer data);
		// void send(ArrayBufferView data);
		public void send(Value data) {
			if (readyState == CONNECTING || webSocket == null) {
				throw new RuntimeException("InvalidStateError");
			}

			if (data.isString()) {
				webSocket.sendText(data.asString(), true);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		public Consumer<Event> getOnopen() {
			return onopen;
		}

		public void setOnopen(Consumer<Event> onopen) {
			this.onopen = onopen;

		}

		public Consumer<Event> getOnerror() {
			return onerror;
		}

		public void setOnerror(Consumer<Event> onerror) {
			this.onerror = onerror;
		}

		public Consumer<Event> getOnclose() {
			return onclose;
		}

		public void setOnclose(Consumer<Event> onclose) {
			this.onclose = onclose;
		}

		public Consumer<Event> getOnmessage() {
			return onmessage;
		}

		public void setOnmessage(Consumer<Event> onmessage) {
			this.onmessage = onmessage;
		}

		public String getBinaryType() {
			return binaryType;
		}

		public void setBinaryType(String binaryType) {
			if (!BinaryType.BLOB.equals(binaryType) && !BinaryType.ARRAY_BUFFER.equals(binaryType)) {
				throw new RuntimeException("BinaryType must be either " + BinaryType.BLOB + " or " + BinaryType.ARRAY_BUFFER);
			}
			this.binaryType = binaryType;
		}

		@Override
		public void onOpen(WebSocket webSocket) {
			readyState = OPEN;
			this.webSocket = webSocket;
			protocol = webSocket.getSubprotocol();
			webSocket.request(1);

			if (onopen != null) {
				Event event = new Event(this, this, "open");
				onopen.accept(event);
			}
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			readyState = CLOSED;
			if (onclose != null) {
				Event event = new CloseEvent(this, this, "close", statusCode, reason, true);
				onclose.accept(event);
			}

			return null;
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
			webSocket.request(1);
			if (readyState == OPEN && onmessage != null) {
				Event event = new MessageEvent(this, this, "message", message.toString(), url);
				onmessage.accept(event);
			}
			return null;
		}

		@Override
		public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
			webSocket.request(1);
			throw new UnsupportedOperationException("binary");
		}


		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			if (onerror != null) {
				error.printStackTrace();
				Event event = new Event(this, this, "error");
				onerror.accept(event);
			}
		}

		private void checkCloseCode(int closeCode) {
			// See https://html.spec.whatwg.org/multipage/web-sockets.html#dom-websocket-close
			if (closeCode != 1000 && (closeCode < 3000 || closeCode > 4999)) {
				throw new RuntimeException("InvalidAccessError");
			}
		}

		private void checkCloseReason(String reason) {
			try {
				if (reason.getBytes("UTF-8").length > 123) {
					throw new RuntimeException("SyntaxError");
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("WebSocket polyfill internal error");
			}
		}

		public final class BinaryType {
			public static final String BLOB = "blob";
			public static final String ARRAY_BUFFER = "arraybuffer";

			private BinaryType() {
			}
		}

		public static class MessageEvent extends Event {
			private String data;
			private String origin;

			public MessageEvent() {
			}

			public MessageEvent(JavascriptWebSocket target, JavascriptWebSocket currentTarget, String type, String data, String origin) {
				super(target, currentTarget, type);
				this.data = data;
				this.origin = origin;
			}

			public String getData() {
				return data;
			}

			public void setData(String data) {
				this.data = data;
			}

			public String getOrigin() {
				return origin;
			}

			public void setOrigin(String origin) {
				this.origin = origin;
			}
		}

		public static class CloseEvent extends Event {
			private int code;
			private String reason;
			private boolean wasClean;

			public CloseEvent() {
			}

			public CloseEvent(JavascriptWebSocket target, JavascriptWebSocket currentTarget, String type, int code, String reason, boolean wasClean) {
				super(target, currentTarget, type);
				this.code = code;
				this.reason = reason;
				this.wasClean = wasClean;
			}

			public int getCode() {
				return code;
			}

			public void setCode(int code) {
				this.code = code;
			}

			public String getReason() {
				return reason;
			}

			public void setReason(String reason) {
				this.reason = reason;
			}

			public boolean isWasClean() {
				return wasClean;
			}

			public void setWasClean(boolean wasClean) {
				this.wasClean = wasClean;
			}
		}

		public static class Event {
			private JavascriptWebSocket target;
			private JavascriptWebSocket currentTarget;
			private String type;

			public Event() {
			}

			public Event(JavascriptWebSocket target, JavascriptWebSocket currentTarget, String type) {
				this.target = target;
				this.currentTarget = currentTarget;
				this.type = type;
			}

			public JavascriptWebSocket getTarget() {
				return target;
			}

			public void setTarget(JavascriptWebSocket target) {
				this.target = target;
			}

			public JavascriptWebSocket getCurrentTarget() {
				return currentTarget;
			}

			public void setCurrentTarget(JavascriptWebSocket currentTarget) {
				this.currentTarget = currentTarget;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}
		}
	}
}
