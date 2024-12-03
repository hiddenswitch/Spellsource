package io.vertx.await;

import io.vertx.core.Future;

public class FailedFutureException extends RuntimeException {
	private final Future<?> fut;

	public <T> FailedFutureException(Future<T> fut) {
		this.fut = fut;
	}

	public Future<?> fut() {
		return fut;
	}
}
