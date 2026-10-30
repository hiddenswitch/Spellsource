package io.vertx.await;


import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadFactory;

import static io.vertx.core.impl.Utils.throwAsUnchecked;

public class Async {
	private static final ThreadLocal<WeakReference<ContextInternal>> stickyContext = new ThreadLocal<>();

	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T> {
		void accept(T value) throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R> {
		R apply(T value) throws Exception;
	}

	public static <T> T await(Future<T> fut) {
		if (fut == null) {
			throw new IllegalArgumentException("Future cannot be null");
		}
		if (fut.isComplete()) {
			if (fut.succeeded()) {
				return fut.result();
			} else if (fut.cause() != null) {
				throwAsUnchecked(fut.cause());
			} else {
				throw new FailedFutureException(fut);
			}
		}
		var ctx = Vertx.currentContext();
		if (ctx == null) {
			try {
				return fut.toCompletionStage().toCompletableFuture().get();
			} catch (ExecutionException e) {
				throwAsUnchecked(e.getCause() != null ? e.getCause() : e);
				return null;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throwAsUnchecked(e);
				return null;
			}
		}
		return io.vertx.core.Future.await(fut);
	}

	public static void awaitAll(Future<?>... futures) {
		awaitAll(List.of(futures));
	}

	public static void awaitAll(Collection<? extends Future<?>> futures) {
		if (!futures.isEmpty()) {
			await(Future.all(List.copyOf(futures)));
		}
	}

	public static ThreadFactory virtualThreadFactory() {
		return virtualThreadFactory(null);
	}

	public static ThreadFactory virtualThreadFactory(Vertx vertx) {
		var resolvedVertx = vertx;
		if (resolvedVertx == null) {
			var ctx = Vertx.currentContext();
			if (ctx != null) {
				resolvedVertx = ctx.owner();
			}
		}
		var vertxInternal = (VertxInternal) resolvedVertx;
		var baseCtx = vertxInternal != null ? vertxInternal.createEventLoopContext() : null;
		var factorySpan = io.opentracing.util.GlobalTracer.get().activeSpan();
		return runnable -> {
			var forkSpan = io.opentracing.util.GlobalTracer.get().activeSpan();
			var parentSpan = forkSpan != null ? forkSpan : factorySpan;
			return Thread.ofVirtual().unstarted(() -> {
				if (baseCtx == null) {
					try (var ignored = parentSpan != null ? io.opentracing.util.GlobalTracer.get().activateSpan(parentSpan) : null) {
						runnable.run();
					}
					return;
				}
				var childCtx = baseCtx.duplicate();
				var prev = childCtx.beginDispatch();
				try (var ignored = parentSpan != null ? io.opentracing.util.GlobalTracer.get().activateSpan(parentSpan) : null) {
					runnable.run();
				} finally {
					childCtx.endDispatch(prev);
				}
			});
		};
	}

	public static <T> StructuredTaskScope<T, Void> openScope() {
		return openScope(null, StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow());
	}

	public static <T> StructuredTaskScope<T, Void> openScope(Vertx vertx) {
		return openScope(vertx, StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow());
	}

	public static <T, R> StructuredTaskScope<T, R> openScope(StructuredTaskScope.Joiner<? super T, ? extends R> joiner) {
		return openScope(null, joiner);
	}

	public static <T, R> StructuredTaskScope<T, R> openScope(Vertx vertx, StructuredTaskScope.Joiner<? super T, ? extends R> joiner) {
		var factory = virtualThreadFactory(vertx);
		return StructuredTaskScope.open(joiner, cfg -> cfg.withThreadFactory(factory));
	}

	public static <T, R> R join(StructuredTaskScope<T, R> scope) {
		var executor = io.vertx.core.impl.WorkerExecutor.unwrapWorkerExecutor();
		var execution = executor != null ? executor.currentExecution() : null;
		var latch = execution != null ? execution.trySuspend() : null;
		boolean interrupted = false;
		try {
			return scope.join();
		} catch (StructuredTaskScope.FailedException e) {
			throwAsUnchecked(e.getCause() != null ? e.getCause() : e);
			return null;
		} catch (InterruptedException e) {
			interrupted = true;
			Thread.currentThread().interrupt();
			throwAsUnchecked(e);
			return null;
		} finally {
			if (execution != null && latch != null) {
				execution.resume();
				if (!interrupted) {
					try {
						latch.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throwAsUnchecked(e);
					}
				}
			}
		}
	}

	public static void runAll(ThrowingRunnable... tasks) {
		try (var scope = openScope()) {
			for (var task : tasks) {
				scope.fork(() -> {
					task.run();
					return null;
				});
			}
			join(scope);
		}
	}

	public static <T> void forEachParallel(Collection<T> items, ThrowingConsumer<? super T> action) {
		if (items.isEmpty()) {
			return;
		}
		try (var scope = openScope()) {
			for (var item : items) {
				scope.fork(() -> {
					action.accept(item);
					return null;
				});
			}
			join(scope);
		}
	}

	public static <T, R> List<R> mapParallel(Collection<T> items, ThrowingFunction<? super T, ? extends R> mapper) {
		if (items.isEmpty()) {
			return List.of();
		}
		try (var scope = openScope(StructuredTaskScope.Joiner.<R>allSuccessfulOrThrow())) {
			for (var item : items) {
				scope.fork(() -> mapper.apply(item));
			}
			return join(scope).map(StructuredTaskScope.Subtask::get).toList();
		}
	}

	public static <T> T await(CompletableFuture<T> fut) {
		if (fut.state() == java.util.concurrent.Future.State.SUCCESS) {
			return fut.resultNow();
		}
		if (fut.state() == java.util.concurrent.Future.State.FAILED) {
			throwAsUnchecked(fut.exceptionNow());
			return null;
		}
		try {
			return fut.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Future<T> vt(Vertx vertx, Callable<T> handler) {
		var promise = Promise.<T>promise();
		var vertxInternal = (VertxInternal) vertx;
		var ref = stickyContext.get();
		var context = vertxInternal.getContext();
		if (context == null || context.threadingModel() != ThreadingModel.VIRTUAL_THREAD) {
			if (ref == null || ref.get() == null || ref.get().closeFuture().isClosed()) {
				context = vertxInternal.createVirtualThreadContext();
				ref = new WeakReference<>(context);
				stickyContext.set(ref);
				var finalRef = ref;
				vertxInternal.addCloseHook(prom -> {
					finalRef.clear();
					prom.succeed();
				});
			} else {
				context = ref.get();
				Objects.requireNonNull(context);
			}
		}

		var parentSpan = io.opentracing.util.GlobalTracer.get().activeSpan();
		context.runOnContext(v -> {
			try (var ignored = parentSpan != null ? io.opentracing.util.GlobalTracer.get().activateSpan(parentSpan) : null) {
				promise.complete(handler.call());
			} catch (Throwable e) {
				promise.fail(e);
			}
		});
		return promise.future();
	}
}
