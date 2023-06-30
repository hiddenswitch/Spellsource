package com.hiddenswitch.framework.impl;

import io.vertx.core.impl.ContextInternal;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class DelegatedScheduledFuture<V> implements ScheduledFuture<V> {

	private final CompletableFuture<V> delegate;
	private final long delay;
	private final TimeUnit unit;
	private final ContextInternal context;
	private final long timerId;
	private boolean cancelled;


	public DelegatedScheduledFuture(CompletableFuture<V> delegate, long delay, @NotNull TimeUnit unit, ContextInternal context, long timerId) {
		this.delegate = delegate;
		this.delay = delay;
		this.unit = unit;
		this.context = context;
		this.timerId = timerId;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay, this.unit);
	}

	@Override
	public int compareTo(Delayed o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		this.cancelled = context.owner().cancelTimer(timerId);
		return this.cancelled;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public boolean isDone() {
		return delegate.isDone();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return delegate.get();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return delegate.get(timeout, unit);
	}
}
