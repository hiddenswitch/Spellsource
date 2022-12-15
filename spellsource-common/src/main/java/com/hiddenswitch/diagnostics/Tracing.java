package com.hiddenswitch.diagnostics;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.spellsource.core.Version;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.noop.NoopSpan;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Tracing {
	private static Logger LOGGER = LoggerFactory.getLogger(Tracing.class);

	static {
		tracing();
	}

	public synchronized static Tracer tracing() {
		// calls static constructor as a side effect
		GlobalTracer.registerIfAbsent(() -> initialize("spellsource"));
		return GlobalTracer.get();
	}

	public static synchronized Tracer tracing(Vertx vertx) {
		tracing();
		vertx.exceptionHandler(Tracing::error);
		try {
			Thread.setDefaultUncaughtExceptionHandler((a, b) -> error(b));
		} catch (NoClassDefFoundError compileOnlyDependency) {
		}
		return GlobalTracer.get();
	}

	static void error(Throwable throwable, Span span) {
		error(throwable, span, true);
	}

	public static void error(Throwable throwable, Span span, boolean finish) {
		if (span instanceof NoopSpan || span == null) {
			LOGGER.error("An exception was reported to the tracer");
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

		return config.getTracer();
	}
}
