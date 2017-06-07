package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

class NetworkedRpcClient<T> implements RpcClient<T> {
	private final T proxy;
	Handler next;
	boolean sync;

	NetworkedRpcClient(T proxy) {
		this.proxy = proxy;
	}

	@Override
	public <R> T async(Handler<AsyncResult<R>> handler) {
		next = handler;
		sync = false;
		return proxy;
	}

	@Override
	@Suspendable
	public T sync() throws SuspendExecution, InterruptedException {
		next = null;
		sync = true;
		return proxy;
	}

	@Override
	@Suspendable
	public T uncheckedSync() {
		next = null;
		sync = true;
		return proxy;
	}
}
