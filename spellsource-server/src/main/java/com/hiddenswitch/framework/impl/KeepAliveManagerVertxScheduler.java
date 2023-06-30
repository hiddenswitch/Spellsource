package com.hiddenswitch.framework.impl;

import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class KeepAliveManagerVertxScheduler implements ScheduledExecutorService {
	private final ContextInternal context;

	public KeepAliveManagerVertxScheduler(Context context) {
		this.context = (ContextInternal) context;
	}

	@NotNull
	@Override
	public ScheduledFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
		var promise = Promise.<Void>promise();
		var timerId = context.setTimer(Math.max(1, unit.toMillis(delay)), v -> {
			try {
				command.run();
			} catch (Throwable t) {
				promise.fail(t);
				return;
			}
			promise.complete();
		});

		var scheduledFuture = new DelegatedScheduledFuture<>(promise.future().toCompletionStage().toCompletableFuture(), delay, unit, context, timerId);
		return scheduledFuture;
	}

	@NotNull
	@Override
	public <V> ScheduledFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTerminated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public <T> Future<T> submit(@NotNull Callable<T> task) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public <T> Future<T> submit(@NotNull Runnable task, T result) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Future<?> submit(@NotNull Runnable task) {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void execute(@NotNull Runnable command) {
		throw new UnsupportedOperationException();
	}
}
