package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.core.Handler;

/**
 * Created by bberman on 2/15/17.
 */
public class Sync {
	@Suspendable
	public static <T> Handler<T> suspendableHandler(SuspendableAction1<T> handler) {
		FiberScheduler scheduler = io.vertx.ext.sync.Sync.getContextScheduler();
		return p -> new Fiber<Void>(scheduler, () -> handler.call(p)).start();
	}
}
