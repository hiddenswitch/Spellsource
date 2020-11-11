package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.impl.TimerId;

import java.lang.ref.WeakReference;

import static io.vertx.ext.sync.Sync.fiber;

/**
 * A timer for keeping track of a client's activity. Uses a {@link Scheduler} (typically implemented from vertx) for
 * timers. Calls a handler after {@code noActivityTimeout} of not getting {@link #activity()} called.
 */
public class ActivityMonitor {
	private final long noActivityTimeout;
	private final WeakReference<Scheduler> scheduler;
	private TimerId lastTimerId;
	private final SuspendableAction1<ActivityMonitor> onTimeout;
	private GameId gameId;

	public ActivityMonitor(Scheduler scheduler, long noActivityTimeout, SuspendableAction1<ActivityMonitor> onTimeout, GameId gameId) {
		this.gameId = gameId;
		this.scheduler = new WeakReference<>(scheduler);
		this.noActivityTimeout = noActivityTimeout;
		this.onTimeout = onTimeout;
	}

	private void handleTimeout(long t) throws InterruptedException, SuspendExecution {
		onTimeout.call(this);
	}

	public void activity() {
		Scheduler scheduler = this.scheduler.get();
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

	public GameId getGameId() {
		return gameId;
	}
}
