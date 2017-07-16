package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * Created by bberman on 12/6/16.
 */
public class ActivityMonitor {
	private static Logger logger = LoggerFactory.getLogger(ActivityMonitor.class);
	private final String gameId;
	private final long noActivityTimeout;
	private final WeakReference<Vertx> vertx;
	private long lastTimerId = Long.MIN_VALUE;
	private final SuspendableAction1<String> onTimeout;

	public ActivityMonitor(Vertx vertx, String gameId, long noActivityTimeout, SuspendableAction1<String> onTimeout) {
		this.vertx = new WeakReference<>(vertx);
		this.gameId = gameId;
		this.noActivityTimeout = noActivityTimeout;
		this.onTimeout = onTimeout;
	}

	private void handleTimeout(long t) throws InterruptedException, SuspendExecution {
		onTimeout.call(gameId);
	}

	@Suspendable
	public void activity() {
		final Vertx vertx = this.vertx.get();
		if (vertx == null) {
			return;
		}

		cancel();

		lastTimerId = vertx.setTimer(noActivityTimeout, suspendableHandler(this::handleTimeout));
	}

	@Suspendable
	public void cancel() {
		final Vertx vertx = this.vertx.get();

		if (vertx == null) {
			return;
		}

		if (lastTimerId != Long.MIN_VALUE) {
			vertx.cancelTimer(lastTimerId);
		}
	}
}
