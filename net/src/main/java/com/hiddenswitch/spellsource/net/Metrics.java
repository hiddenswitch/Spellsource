package com.hiddenswitch.spellsource.net;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.vertx.micrometer.backends.BackendRegistries;

public interface Metrics {
	static void defaultMetrics() {
		var registry = BackendRegistries.getDefaultNow();

		new ClassLoaderMetrics().bindTo(registry);
		new JvmMemoryMetrics().bindTo(registry);
		new JvmGcMetrics().bindTo(registry);
		new ProcessorMetrics().bindTo(registry);
		new JvmThreadMetrics().bindTo(registry);

		// Observe verticles that we support
	}
}
