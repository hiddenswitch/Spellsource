package com.hiddenswitch.framework.impl;

import io.vertx.core.Handler;

public interface Scheduler {
	Long setTimer(long delay, Handler<Long> handler);

	Long setInterval(long delay, Handler<Long> handler);

	boolean cancelTimer(Long id);
}
