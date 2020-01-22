package io.vertx.ext.sync.impl;

import co.paralleluniverse.fibers.FiberAsync;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AsyncAdaptor<T> extends FiberAsync<T, Throwable> implements Handler<AsyncResult<T>> {
	private volatile boolean executingConsumer;

	public AsyncAdaptor() {
		super(false);
	}

	@Override
	protected void requestAsync() {
		executingConsumer = true;
	}

	@Override
	@Suspendable
	public void handle(AsyncResult<T> res) {
		executingConsumer = false;
		if (res.succeeded()) {
			asyncCompleted(res.result());
		} else {
			asyncFailed(res.cause());
		}
	}

	public boolean isExecutingConsumer() {
		return executingConsumer;
	}
}
