package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.common.Tracing;
import io.atomix.vertx.VertxFutures;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;

import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Contains utilities for:
 * <ul>
 * <li>Converting {@link Thread}-blocking calls into fiber suspendable calls using {@link #invoke(Supplier)}
 * methods</li>
 * <li>Creating handlers for vertx callbacks that support throwing {@link co.paralleluniverse.fibers.SuspendExecution}
 * (i.e. checked exception that ensures your fiber will be instrumented) using {@link
 * #suspendableHandler(SuspendableAction1)}</li>
 * <li>Calling a fiber-synchronous method at a later time using {@link #defer(SuspendableAction1)}.</li>
 * </ul>
 */
public class Sync {
	/**
	 * Defer a call by queueing it to be executed on the current {@link io.vertx.core.Context}
	 *
	 * @param handler The code to execute at a later time.
	 */
	@Suspendable
	public static void defer(SuspendableAction1<Void> handler) {
		Vertx.currentContext().runOnContext(suspendableHandler(handler));
	}

	@Suspendable
	public static <T> Handler<T> suspendableHandler(SuspendableAction1<T> handler) {
		FiberScheduler scheduler = io.vertx.ext.sync.Sync.getContextScheduler();
		return suspendableHandler(scheduler, handler);
	}

	@NotNull
	@Suspendable
	public static <T> Handler<T> suspendableHandler(FiberScheduler scheduler, SuspendableAction1<T> handler) {
		return p -> {
			Fiber<Void> voidFiber = new Fiber<>(scheduler, () -> handler.call(p));
			voidFiber.setUncaughtExceptionHandler((f, e) -> {
				Tracing.error(e);
			});
			voidFiber.start();
		};
	}

	@Suspendable
	public static <R> R invoke(Supplier<R> func0) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func0.get());
		}, false, h));
	}

	@Suspendable
	public static <R> R invoke(ThrowingSupplier<R> func0) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func0.get());
		}, false, h));
	}

	@Suspendable
	public static <T> void invoke0(Consumer<T> func1, T arg1) {
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			Void res = awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
				func1.accept(arg1);
				done.complete();
			}, false, h));
		} else {
			func1.accept(arg1);
		}
	}

	@Suspendable
	public static void invoke0(NoArgs func0) {
		Void res = awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			func0.apply();
			done.complete();
		}, false, h));
	}

	@Suspendable
	public static void invoke0(ThrowingNoArgs func0) {
		Void res = awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			func0.apply();
			done.complete();
		}, false, h));
	}

	@Suspendable
	public static <T, R> R invoke(Function<T, R> func1, T arg1) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func1.apply(arg1));
		}, false, h));
	}

	@Suspendable
	public static <T, R> R invoke(ThrowingFunction<T, R> func1, T arg1) {
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
				done.complete(func1.apply(arg1));
			}, false, h));
		} else {
			return func1.apply(arg1);
		}
	}

	@Suspendable
	public static <T1, T2, R> R invoke(BiFunction<T1, T2, R> func2, T1 arg1, T2 arg2) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func2.apply(arg1, arg2));
		}, false, h));
	}

	@Suspendable
	public static <T1, T2, R> R invoke(ThrowingBiFunction<T1, T2, R> func2, T1 arg1, T2 arg2) {
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
				done.complete(func2.apply(arg1, arg2));
			}, false, h));
		} else {
			return func2.apply(arg1, arg2);
		}
	}

	@Suspendable
	public static <R> R invoke1(Consumer<Handler<AsyncResult<R>>> func) {
		return awaitResult(func);
	}

	@Suspendable
	public static <T1, R> R invoke(BiConsumer<T1, Handler<AsyncResult<R>>> func, T1 arg1) {
		return awaitResult(h -> func.accept(arg1, h));
	}

	@Suspendable
	public static <T1, T2, R> R invoke(TriConsumer<T1, T2, Handler<AsyncResult<R>>> func, T1 arg1, T2 arg2) {
		return awaitResult(h -> func.accept(arg1, arg2, h));
	}


	@FunctionalInterface
	public interface ThrowingSupplier<R> extends Supplier<R> {
		@Override
		default R get() {
			try {
				return getThrows();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		R getThrows() throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R> extends Function<T, R> {

		@Override
		default R apply(T t) {
			try {
				return applyThrows(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		R applyThrows(T t) throws Exception;
	}

	@FunctionalInterface
	public interface ThrowingNoArgs extends NoArgs {
		@Override
		default void apply() {
			try {
				applyThrows();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		void applyThrows() throws Exception;
	}


	@FunctionalInterface
	public interface ThrowingBiFunction<T1, T2, R> extends BiFunction<T1, T2, R> {

		@Override
		default R apply(T1 t1, T2 t2) {
			try {
				return applyThrows(t1, t2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		R applyThrows(T1 t1, T2 t2) throws Exception;
	}

	/**
	 * Converts a completable future into a suspendable await. Uses a Vertx context.
	 *
	 * @param future
	 * @param <T>
	 * @return
	 */
	@Suspendable
	public static <T> T get(CompletableFuture<T> future) {
		return awaitResult(h -> future.whenComplete(VertxFutures.resultHandler(h, Vertx.currentContext())));
	}
}
