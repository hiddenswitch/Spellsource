package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.AbstractService;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.lang.reflect.Proxy;

/**
 * Created by bberman on 6/7/17.
 */
class LocalRpcClient<T extends AbstractService<T>> implements RpcClient<T> {
	private final Class<?> thisClass;
	private final T service;

	public LocalRpcClient(Class<?> thisClass, T service) {
		this.thisClass = thisClass;
		this.service = service;
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public <R> T async(Handler<AsyncResult<R>> handler) {
		return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{thisClass}, (proxy, method, args) -> {
			try {
				Object result = method.invoke(service, args);
				handler.handle(Future.succeededFuture((R) result));
			} catch (Throwable e) {
				handler.handle(Future.failedFuture(e));
			}
			return null;
		});
	}

	@Override
	@Suspendable
	public T sync() throws SuspendExecution, InterruptedException {
		return service;
	}

	@Override
	@Suspendable
	public T uncheckedSync() {
		return service;
	}
}
