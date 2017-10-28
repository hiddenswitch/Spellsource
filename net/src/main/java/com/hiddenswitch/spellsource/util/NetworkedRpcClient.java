package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;

import java.lang.reflect.Proxy;

class NetworkedRpcClient<T> implements RpcClient<T> {
	private final EventBus bus;
	private final Class<? extends T> serviceInterface;

	NetworkedRpcClient(EventBus bus, Class<? extends T> serviceInterface) {
		this.bus = bus;
		this.serviceInterface = serviceInterface;
	}


	@Override
	@SuppressWarnings("unchecked")
	public <R> T async(Handler<AsyncResult<R>> handler, long timeout) {
		VertxInvocationHandler<T> invocationHandler = new VertxInvocationHandler<T>(serviceInterface.getName(), bus, false, (Handler) handler);
		invocationHandler.timeout = timeout;
		return (T) Proxy.newProxyInstance(
				serviceInterface.getClassLoader(),
				new Class[]{serviceInterface},
				invocationHandler
		);
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public T sync() throws SuspendExecution, InterruptedException {
		return getProxy();
	}

	@Override
	@Suspendable
	public T uncheckedSync() {
		return getProxy();
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	private T getProxy() {
		final VertxInvocationHandler<T> invocationHandler = new VertxInvocationHandler<>(serviceInterface.getName(), bus, true, null);

		return (T) Proxy.newProxyInstance(
				serviceInterface.getClassLoader(),
				new Class[]{serviceInterface},
				invocationHandler
		);
	}
}
