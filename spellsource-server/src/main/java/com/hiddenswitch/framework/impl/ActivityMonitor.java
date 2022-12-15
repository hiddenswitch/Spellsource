package com.hiddenswitch.framework.impl;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import static com.hiddenswitch.framework.Environment.fiber;

/**
 * A timer for keeping track of a client's activity. Uses a {@link Scheduler} (typically implemented from vertx) for
 * timers. Calls a handler after {@code noActivityTimeout} of not getting {@link #activity()} called.
 */
public class ActivityMonitor {
	private final long noActivityTimeout;
	private final WeakReference<Scheduler> scheduler;
	private final String gameId;
	private final Consumer<ActivityMonitor> onTimeout;
	private Long lastTimerId;

	public ActivityMonitor(Scheduler scheduler, long noActivityTimeout, Consumer<ActivityMonitor> onTimeout, String gameId) {
		this.gameId = gameId;
		this.scheduler = new WeakReference<>(scheduler);
		this.noActivityTimeout = noActivityTimeout;
		this.onTimeout = onTimeout;
	}

	public void activity() {
		var scheduler = this.scheduler.get();
		if (scheduler == null) {
			return;
		}

		cancel();

		lastTimerId = scheduler.setTimer(noActivityTimeout, v -> fiber(() -> {
			onTimeout.accept(this);
			return (Void) null;
		}));
	}

	public void cancel() {
		Scheduler scheduler = this.scheduler.get();

		if (scheduler == null) {
			return;
		}

		if (lastTimerId != null) {
			scheduler.cancelTimer(lastTimerId);
		}
	}

	public String getGameId() {
		return gameId;
	}
}
