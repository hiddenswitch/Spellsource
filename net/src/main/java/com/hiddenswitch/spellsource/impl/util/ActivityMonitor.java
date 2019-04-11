package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.TimerId;
import com.hiddenswitch.spellsource.impl.server.VertxScheduler;
import io.vertx.core.Vertx;

import java.lang.ref.WeakReference;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * Created by bberman on 12/6/16.
 */
public class ActivityMonitor {
	private final long noActivityTimeout;
	private final WeakReference<Scheduler> scheduler;
	private TimerId lastTimerId;
	private final SuspendableAction1<ActivityMonitor> onTimeout;
	private GameId gameId;

	public ActivityMonitor(Vertx vertx, long noActivityTimeout, SuspendableAction1<ActivityMonitor> onTimeout, GameId gameId) {
		this(new VertxScheduler(vertx), noActivityTimeout, onTimeout, gameId);
	}

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

		lastTimerId = scheduler.setTimer(noActivityTimeout, suspendableHandler(this::handleTimeout));
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

	public GameId getGameId() {
		return gameId;
	}
}
