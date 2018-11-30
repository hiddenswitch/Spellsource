package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;

import java.lang.reflect.Proxy;
import java.util.NoSuchElementException;

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
		VertxInvocationHandler<T> invocationHandler = new VertxInvocationHandler<T>(null, bus, false, (Handler) handler, serviceInterface.getName());
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
		return getProxy(null);
	}

	@Override
	public T sync(String deploymentId) throws SuspendExecution, InterruptedException, NoSuchElementException {
		return getProxy(deploymentId);
	}

	@Override
	@Suspendable
	public T uncheckedSync() {
		return getProxy(null);
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	private T getProxy(String deploymentId) {
		final VertxInvocationHandler<T> invocationHandler = new VertxInvocationHandler<>(deploymentId, bus, true, null, serviceInterface.getName());

		return (T) Proxy.newProxyInstance(
				serviceInterface.getClassLoader(),
				new Class[]{serviceInterface},
				invocationHandler
		);
	}
}
