package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Fiber;
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

public interface Tracing {
	Logger LOGGER = LoggerFactory.getLogger(Tracing.class);

	static void initializeGlobal(Vertx vertx) {
		GlobalTracer.registerIfAbsent(initialize("spellsource"));
		vertx.exceptionHandler(Tracing::error);
		Fiber.setDefaultUncaughtExceptionHandler((a, b) -> error(b));
	}

	static void error(Throwable throwable, Span span) {
		error(throwable, span, true);
	}

	static void error(Throwable throwable, Span span, boolean finish) {
		if (span instanceof NoopSpan || span == null) {
			LOGGER.error(throwable.getMessage(), throwable);
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

	static void error(Throwable throwable) {
		error(throwable, GlobalTracer.get().activeSpan(), true);
	}

	static Tracer initialize(String serviceName) {
		return initialize(serviceName, ConstSampler.TYPE, 1);
	}

	static Tracer initialize(String serviceName, String samplerType, Number samplerParameter) {
		Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
				.withType(samplerType)
				.withParam(samplerParameter);

		Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
				.withLogSpans(true)
				.withSender(Configuration.SenderConfiguration.fromEnv()
						.withAgentHost("localhost")
						.withAgentPort(6831));

		Map<String, String> map = new HashMap<>();
		map.put("version", Version.version());

		Configuration config = new Configuration(serviceName)
				.withTracerTags(map)
				.withSampler(samplerConfig)
				.withReporter(reporterConfig);

		return config.getTracer();
	}
}
