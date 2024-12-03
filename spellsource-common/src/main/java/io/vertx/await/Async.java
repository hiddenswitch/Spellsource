package io.vertx.await;


import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.vertx.core.impl.Utils.throwAsUnchecked;

public class Async {
	private static final ThreadLocal<WeakReference<ContextInternal>> stickyContext = new ThreadLocal<>();

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
			} catch (InterruptedException | ExecutionException e) {
				throwAsUnchecked(e);
			}
		}
		return io.vertx.core.Future.await(fut);
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
					prom.complete(null);
				});
			} else {
				context = ref.get();
				Objects.requireNonNull(context);
			}
		}

		context.runOnContext(v -> {
			try {
				promise.complete(handler.call());
			} catch (Throwable e) {
				promise.fail(e);
			}
		});
		return promise.future();
	}
}
