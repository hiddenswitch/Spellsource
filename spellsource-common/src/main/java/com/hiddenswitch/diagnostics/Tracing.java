package com.hiddenswitch.diagnostics;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.spellsource.core.Version;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.LogData;
import io.jaegertracing.internal.Reference;
import io.jaegertracing.internal.metrics.NoopMetricsFactory;
import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.Reporter;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.noop.NoopSpan;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;

public class Tracing {
	private static final Logger LOGGER = LoggerFactory.getLogger(Tracing.class);
	private static final Set<EventBus> INSTRUMENTED_BUSES = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
	private static final int MAX_RECORDED_SPANS = 4096;
	private static final Deque<RecordedSpan> RECORDED_SPANS = new ConcurrentLinkedDeque<>();
	private static final ThreadLocal<Boolean> SUPPRESS_JAEGER_RECORDING = ThreadLocal.withInitial(() -> false);

	public record RecordedReference(String type, String traceId, String spanId) {
	}

	public record RecordedSpan(
			String traceId,
			String spanId,
			String parentSpanId,
			String operationName,
			long startTimeMicros,
			long durationMicros,
			Map<String, Object> tags,
			List<Map<String, Object>> logs,
			List<RecordedReference> references) {
	}

	static {
		tracing();
	}

	public static Tracer tracing() {
		// calls static constructor as a side effect
		GlobalTracer.registerIfAbsent(() -> initialize("spellsource"));
		return GlobalTracer.get();
	}

	public static Tracer tracing(Vertx vertx) {
		tracing();
		if (vertx != null) {
			eventBus(vertx);
			vertx.exceptionHandler(Tracing::error);
		}
		try {
			Thread.setDefaultUncaughtExceptionHandler((a, b) -> error(b));
		} catch (NoClassDefFoundError compileOnlyDependency) {
		}
		return GlobalTracer.get();
	}

	public static List<RecordedSpan> getRecordedSpans() {
		return List.copyOf(RECORDED_SPANS);
	}

	public static void clearRecordedSpans() {
		RECORDED_SPANS.clear();
	}

	private static void addRecordedSpan(RecordedSpan recordedSpan) {
		RECORDED_SPANS.addLast(recordedSpan);
		while (RECORDED_SPANS.size() > MAX_RECORDED_SPANS) {
			RECORDED_SPANS.pollFirst();
		}
	}

	private static void recordJaegerSpan(JaegerSpan span) {
		if (Boolean.TRUE.equals(SUPPRESS_JAEGER_RECORDING.get())) {
			return;
		}
		var ctx = span.context();
		var traceId = ctx.getTraceId();
		var spanId = Long.toHexString(ctx.getSpanId());
		var parentSpanId = ctx.getParentId() == 0L ? "0" : Long.toHexString(ctx.getParentId());
		var tags = span.getTags() != null ? new LinkedHashMap<>(span.getTags()) : Map.<String, Object>of();
		var logs = new ArrayList<Map<String, Object>>();
		if (span.getLogs() != null) {
			for (LogData logData : span.getLogs()) {
				var map = new LinkedHashMap<String, Object>();
				map.put("timestampMicros", logData.getTime());
				if (logData.getFields() != null) {
					map.putAll(logData.getFields());
				} else if (logData.getMessage() != null) {
					map.put(Fields.MESSAGE, logData.getMessage());
				}
				logs.add(Collections.unmodifiableMap(map));
			}
		}
		var refs = new ArrayList<RecordedReference>();
		if (span.getReferences() != null) {
			for (Reference ref : span.getReferences()) {
				if (ref.getSpanContext() instanceof JaegerSpanContext refCtx) {
					refs.add(new RecordedReference(
							ref.getType(),
							refCtx.getTraceId(),
							Long.toHexString(refCtx.getSpanId())));
				}
			}
		}
		addRecordedSpan(new RecordedSpan(
				traceId,
				spanId,
				parentSpanId,
				span.getOperationName(),
				span.getStart(),
				span.getDuration(),
				Collections.unmodifiableMap(tags),
				Collections.unmodifiableList(logs),
				Collections.unmodifiableList(refs)));
	}

	public static EventBus eventBus(Vertx vertx) {
		var eb = vertx.eventBus();
		if (INSTRUMENTED_BUSES.add(eb)) {
			eb.addOutboundInterceptor(ctx -> {
				var active = GlobalTracer.get().activeSpan();
				if (active != null) {
					var headers = ctx.message().headers();
					GlobalTracer.get().inject(active.context(), Format.Builtin.TEXT_MAP, new TextMap() {
						@Override
						public Iterator<Map.Entry<String, String>> iterator() {
							return Collections.emptyIterator();
						}

						@Override
						public void put(String key, String value) {
							headers.set(key, value);
						}
					});
				}
				ctx.next();
			});
		}
		return eb;
	}

	private static final class TracedMessageHandler<T> implements Handler<Message<T>> {
		private final String address;
		private final Handler<Message<T>> delegate;

		private TracedMessageHandler(String address, Handler<Message<T>> delegate) {
			this.address = address;
			this.delegate = delegate;
		}

		@Override
		public void handle(Message<T> msg) {
			var tracer = tracing();
			var headers = msg.headers();
			var parent = headers.isEmpty() ? null : extractFromHeaders(headers.entries());
			if (parent == null && tracer.activeSpan() != null) {
				parent = tracer.activeSpan().context();
			}
			var span = tracer.buildSpan("EventBus/" + address).asChildOf(parent).start();
			try (var ignored = tracer.activateSpan(span)) {
				delegate.handle(msg);
			} finally {
				span.finish();
			}
		}
	}

	public static <T> Handler<Message<T>> wrapMessageHandler(String address, Handler<Message<T>> handler) {
		if (handler == null || handler instanceof TracedMessageHandler<?>) {
			return handler;
		}
		return new TracedMessageHandler<>(address, handler);
	}

	public static <T> MessageConsumer<T> consumer(EventBus eb, String address, Handler<Message<T>> handler) {
		return eb.consumer(address, wrapMessageHandler(address, handler));
	}

	public static <T> MessageConsumer<T> handler(MessageConsumer<T> consumer, Handler<Message<T>> handler) {
		return consumer.handler(wrapMessageHandler(consumer.address(), handler));
	}

	public static Scope enterSpan(String operationName, SpanContext fallbackParent) {
		var tracer = tracing();
		var active = tracer.activeSpan();
		var parent = active != null ? active.context() : fallbackParent;
		var span = tracer.buildSpan(operationName).asChildOf(parent).start();
		var scope = tracer.activateSpan(span);
		return () -> {
			try {
				span.finish();
			} finally {
				scope.close();
			}
		};
	}

	public static Scope enterSpanWithLinks(String operationName, Collection<SpanContext> linkedContexts) {
		var tracer = tracing();
		var builder = tracer.buildSpan(operationName);
		SpanContext primaryParent = null;
		if (linkedContexts != null) {
			for (var ctx : linkedContexts) {
				if (ctx != null) {
					if (primaryParent == null) {
						primaryParent = ctx;
						builder.asChildOf(ctx);
					}
					builder.addReference(References.FOLLOWS_FROM, ctx);
				}
			}
		}
		if (primaryParent == null && tracer.activeSpan() != null) {
			builder.asChildOf(tracer.activeSpan().context());
		}
		var span = builder.start();
		var scope = tracer.activateSpan(span);
		return () -> {
			try {
				span.finish();
			} finally {
				scope.close();
			}
		};
	}

	public static SpanContext activeSpanContext() {
		var active = GlobalTracer.get().activeSpan();
		return active != null ? active.context() : null;
	}

	public static String injectToString(SpanContext context) {
		if (context == null) {
			return "";
		}
		var map = new HashMap<String, String>(2);
		tracing().inject(context, Format.Builtin.TEXT_MAP, new TextMap() {
			@Override
			public Iterator<Map.Entry<String, String>> iterator() {
				return Collections.emptyIterator();
			}

			@Override
			public void put(String key, String value) {
				map.put(key.toLowerCase(Locale.ROOT), value);
			}
		});
		return map.getOrDefault("uber-trace-id", "");
	}

	public static void injectToHeaders(SpanContext context, BiConsumer<String, String> headerSetter) {
		if (context == null || headerSetter == null) {
			return;
		}
		tracing().inject(context, Format.Builtin.HTTP_HEADERS, new TextMap() {
			@Override
			public Iterator<Map.Entry<String, String>> iterator() {
				return Collections.emptyIterator();
			}

			@Override
			public void put(String key, String value) {
				headerSetter.accept(key, value);
			}
		});
	}

	public static SpanContext extractFromString(String serialized) {
		if (serialized == null || serialized.isBlank()) {
			return null;
		}
		var trimmed = serialized.trim();
		String uberTraceId = trimmed;
		if (!trimmed.contains(":") && trimmed.contains("-")) {
			uberTraceId = traceparentToUberTraceId(trimmed);
			if (uberTraceId == null) {
				return null;
			}
		}
		var finalUberTraceId = uberTraceId;
		return tracing().extract(Format.Builtin.TEXT_MAP, new TextMap() {
			@Override
			public Iterator<Map.Entry<String, String>> iterator() {
				return Map.of("uber-trace-id", finalUberTraceId).entrySet().iterator();
			}

			@Override
			public void put(String key, String value) {
			}
		});
	}

	public static SpanContext extractFromHeaders(Iterable<Map.Entry<String, String>> headers) {
		if (headers == null) {
			return null;
		}
		var normalized = new HashMap<String, String>();
		for (var entry : headers) {
			if (entry.getKey() != null && entry.getValue() != null) {
				normalized.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
			}
		}
		if (normalized.isEmpty()) {
			return null;
		}
		if (!normalized.containsKey("uber-trace-id") && normalized.containsKey("traceparent")) {
			var converted = traceparentToUberTraceId(normalized.get("traceparent"));
			if (converted != null) {
				normalized.put("uber-trace-id", converted);
			}
		}
		return tracing().extract(Format.Builtin.HTTP_HEADERS, new TextMap() {
			@Override
			public Iterator<Map.Entry<String, String>> iterator() {
				return normalized.entrySet().iterator();
			}

			@Override
			public void put(String key, String value) {
			}
		});
	}

	private static String traceparentToUberTraceId(String traceparent) {
		if (traceparent == null) {
			return null;
		}
		var parts = traceparent.trim().split("-");
		if (parts.length < 3) {
			return null;
		}
		var traceId = stripLeadingZeros(parts[1]);
		var spanId = stripLeadingZeros(parts[2]);
		var flags = parts.length >= 4 ? stripLeadingZeros(parts[3]) : "1";
		return traceId + ":" + spanId + ":0:" + flags;
	}

	private static String stripLeadingZeros(String hex) {
		int i = 0;
		while (i < hex.length() - 1 && hex.charAt(i) == '0') {
			i++;
		}
		return hex.substring(i).toLowerCase(Locale.ROOT);
	}

	public static String normalizeTraceId(String rawTraceId) {
		if (rawTraceId == null || rawTraceId.isBlank()) {
			return "";
		}
		var extracted = extractFromString(stripLeadingZeros(rawTraceId.trim()) + ":1:0:1");
		if (extracted instanceof JaegerSpanContext jaegerCtx) {
			return jaegerCtx.getTraceId();
		}
		return stripLeadingZeros(rawTraceId.trim());
	}

	public static String normalizeSpanId(String rawSpanId) {
		if (rawSpanId == null || rawSpanId.isBlank()) {
			return "0";
		}
		return stripLeadingZeros(rawSpanId.trim());
	}

	/**
	 * Ingests a batch of client OpenTracing spans (e.g. from the Unity client via {@code POST /traces})
	 * into the server's OpenTracing collector and recorded span store.
	 */
	public static int ingestClientSpans(JsonObject payload) {
		if (payload == null) {
			return 0;
		}
		JsonArray spansArray = payload.getJsonArray("spans");
		if (spansArray == null) {
			if (payload.containsKey("operationName") && payload.containsKey("traceId")) {
				spansArray = new JsonArray().add(payload);
			} else {
				return 0;
			}
		}
		var tracer = tracing();
		int count = 0;
		for (int i = 0; i < spansArray.size(); i++) {
			var item = spansArray.getValue(i);
			if (!(item instanceof JsonObject spanJson)) {
				continue;
			}
			var rawTraceId = spanJson.getString("traceId", "");
			var rawSpanId = spanJson.getString("spanId", "0");
			var rawParentSpanId = spanJson.getString("parentSpanId", "0");
			var operationName = spanJson.getString("operationName", "Client/span");
			if (rawTraceId.isBlank()) {
				continue;
			}
			var traceId = normalizeTraceId(rawTraceId);
			var spanId = normalizeSpanId(rawSpanId);
			var parentSpanId = normalizeSpanId(rawParentSpanId);
			long startTimeMicros = spanJson.getLong("startTimeMicros", System.currentTimeMillis() * 1000L);
			long durationMicros = Math.max(0L, spanJson.getLong("durationMicros", 0L));

			var tagsMap = new LinkedHashMap<String, Object>();
			tagsMap.put("component", "unity-client");
			var tagsJson = spanJson.getJsonObject("tags");
			if (tagsJson != null) {
				tagsMap.putAll(tagsJson.getMap());
			}

			var logsList = new ArrayList<Map<String, Object>>();
			var logsJson = spanJson.getJsonArray("logs");
			if (logsJson != null) {
				for (int j = 0; j < logsJson.size(); j++) {
					if (logsJson.getValue(j) instanceof JsonObject logObj) {
						var logEntry = new LinkedHashMap<String, Object>();
						logEntry.put("timestampMicros", logObj.getLong("timestampMicros", startTimeMicros));
						var fieldsObj = logObj.getJsonObject("fields");
						if (fieldsObj != null) {
							logEntry.putAll(fieldsObj.getMap());
						}
						logsList.add(Collections.unmodifiableMap(logEntry));
					}
				}
			}

			var refsList = new ArrayList<RecordedReference>();
			var refsJson = spanJson.getJsonArray("references");
			if (refsJson != null) {
				for (int j = 0; j < refsJson.size(); j++) {
					if (refsJson.getValue(j) instanceof JsonObject refObj) {
						refsList.add(new RecordedReference(
								refObj.getString("type", References.CHILD_OF),
								normalizeTraceId(refObj.getString("traceId", traceId)),
								normalizeSpanId(refObj.getString("spanId", parentSpanId))));
					}
				}
			} else if (!"0".equals(parentSpanId)) {
				refsList.add(new RecordedReference(References.CHILD_OF, traceId, parentSpanId));
			}

			addRecordedSpan(new RecordedSpan(
					traceId,
					spanId,
					parentSpanId,
					operationName,
					startTimeMicros,
					durationMicros,
					Collections.unmodifiableMap(tagsMap),
					Collections.unmodifiableList(logsList),
					Collections.unmodifiableList(refsList)));

			// Also report to the underlying Jaeger tracer for external collector export
			SUPPRESS_JAEGER_RECORDING.set(true);
			try {
				var parentCtx = extractFromString(traceId + ":" + ("0".equals(parentSpanId) ? spanId : parentSpanId) + ":0:1");
				var builder = tracer.buildSpan(operationName)
						.withStartTimestamp(startTimeMicros);
				if (parentCtx != null) {
					builder.asChildOf(parentCtx);
				}
				var span = builder.start();
				for (var tagEntry : tagsMap.entrySet()) {
					var val = tagEntry.getValue();
					if (val instanceof Boolean b) {
						span.setTag(tagEntry.getKey(), b);
					} else if (val instanceof Number n) {
						span.setTag(tagEntry.getKey(), n);
					} else if (val != null) {
						span.setTag(tagEntry.getKey(), val.toString());
					}
				}
				for (var logEntry : logsList) {
					long ts = logEntry.get("timestampMicros") instanceof Number n ? n.longValue() : startTimeMicros;
					span.log(ts, logEntry);
				}
				span.finish(startTimeMicros + durationMicros);
			} finally {
				SUPPRESS_JAEGER_RECORDING.set(false);
			}
			count++;
		}
		return count;
	}

	static void error(Throwable throwable, Span span) {
		error(throwable, span, true);
	}

	public static void error(Throwable throwable, Span span, boolean finish) {
		if (span instanceof NoopSpan || span == null) {
			LOGGER.error("{}\n{}", throwable.getMessage(), Throwables.getStackTraceAsString(throwable));
			return;
		}

		Tags.ERROR.set(span, true);
		Tags.SAMPLING_PRIORITY.set(span, 1);
		span.log(ImmutableMap.of(
				Fields.EVENT, "error",
				Fields.ERROR_OBJECT, throwable,
				Fields.MESSAGE, throwable.getMessage() == null ? "(no message)" : throwable.getMessage(),
				Fields.STACK, Throwables.getStackTraceAsString(throwable)));
		if (finish) {
			span.finish();
		}
	}

	public static void error(Throwable throwable) {
		error(throwable, GlobalTracer.get().activeSpan(), true);
	}

	static Tracer initialize(String serviceName) {
		return initialize(serviceName, ConstSampler.TYPE, 1);
	}

	static Tracer initialize(String serviceName, String samplerType, Number samplerParameter) {
		var samplerConfig = Configuration.SamplerConfiguration.fromEnv()
				.withType(samplerType)
				.withParam(samplerParameter);

		var senderConfiguration = Configuration.SenderConfiguration.fromEnv();
		if (senderConfiguration.getAgentHost() == null) {
			senderConfiguration.withAgentHost("localhost");
		}
		if (senderConfiguration.getAgentPort() == null) {
			senderConfiguration.withAgentPort(6831);
		}
		var reporterConfig = Configuration.ReporterConfiguration.fromEnv()
				.withLogSpans(true)
				.withSender(senderConfiguration);

		Map<String, String> map = new HashMap<>();
		map.put("version", Version.version());

		var config = new Configuration(serviceName)
				.withTracerTags(map)
				.withSampler(samplerConfig)
				.withReporter(reporterConfig);

		var compositeReporter = new CompositeReporter(
				new io.jaegertracing.internal.reporters.LoggingReporter(),
				new io.jaegertracing.internal.reporters.RemoteReporter.Builder()
						.withSender(senderConfiguration.getSender())
						.build(),
				new Reporter() {
					@Override
					public void report(JaegerSpan span) {
						recordJaegerSpan(span);
					}

					@Override
					public void close() {
					}
				});

		return config.getTracerBuilder()
				.withReporter(compositeReporter)
				.build();
	}
}
