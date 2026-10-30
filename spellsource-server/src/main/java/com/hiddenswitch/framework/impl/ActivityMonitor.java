package com.hiddenswitch.framework.impl;

import io.vertx.core.Vertx;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A timer for keeping track of a client's activity. Calls {@code onTimeout} after {@code noActivityTimeout}
 * milliseconds without {@link #activity()} being called. When {@code noActivityTimeout} is {@code 0},
 * monitoring is disabled and {@link #activity()} / {@link #cancel()} are no-ops.
 */
public class ActivityMonitor {
	private final Vertx vertx;
	private final long noActivityTimeout;
	private final Consumer<ActivityMonitor> onTimeout;
	private Long lastTimerId;

	public ActivityMonitor(Vertx vertx, long noActivityTimeout, Consumer<ActivityMonitor> onTimeout) {
		if (noActivityTimeout < 0L) {
			throw new IllegalArgumentException("noActivityTimeout must be non-negative");
		}
		this.vertx = Objects.requireNonNull(vertx, "vertx");
		this.noActivityTimeout = noActivityTimeout;
		this.onTimeout = Objects.requireNonNull(onTimeout, "onTimeout");
	}

	public void activity() {
		if (noActivityTimeout == 0L) {
			return;
		}
		cancel();
		lastTimerId = vertx.setTimer(noActivityTimeout, v -> {
			lastTimerId = null;
			onTimeout.accept(this);
		});
	}

	public void cancel() {
		if (lastTimerId != null) {
			vertx.cancelTimer(lastTimerId);
			lastTimerId = null;
		}
	}
}
