package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;

import java.lang.ref.WeakReference;

/**
 * A timer for keeping track of a client's activity. Uses a {@link Scheduler} (typically implemented from vertx) for
 * timers. Calls a handler after {@code noActivityTimeout} of not getting {@link #activity()} called.
 */
public class ActivityMonitor {
	private final long noActivityTimeout;
	private final WeakReference<Scheduler> scheduler;
	private final String gameId;
	private final SuspendableAction1<ActivityMonitor> onTimeout;
	private Long lastTimerId;

	public ActivityMonitor(Scheduler scheduler, long noActivityTimeout, SuspendableAction1<ActivityMonitor> onTimeout, String gameId) {
		this.gameId = gameId;
		this.scheduler = new WeakReference<>(scheduler);
		this.noActivityTimeout = noActivityTimeout;
		this.onTimeout = onTimeout;
	}

	private void handleTimeout(long t) throws InterruptedException, SuspendExecution {
		onTimeout.call(this);
	}

	public void activity() {
		var scheduler = this.scheduler.get();
		if (scheduler == null) {
			return;
		}

		cancel();

		lastTimerId = scheduler.setTimer(noActivityTimeout, io.vertx.ext.sync.Sync.fiber(this::handleTimeout));
	}

	@Suspendable
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
