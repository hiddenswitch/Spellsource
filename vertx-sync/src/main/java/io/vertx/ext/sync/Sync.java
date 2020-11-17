package io.vertx.ext.sync;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.strands.SuspendableAction1;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.channels.Channel;
import com.google.common.base.Throwables;
import io.vertx.core.*;
import io.vertx.ext.sync.impl.AsyncAdaptor;
import io.vertx.ext.sync.impl.HandlerAdaptor;
import io.vertx.ext.sync.impl.HandlerReceiverAdaptorImpl;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class contains various static methods to allowing events and asynchronous results to be accessed in a
 * synchronous way.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Sync {
	protected static final String FIBER_SCHEDULER_CONTEXT_KEY = "__vertx-sync.fiberScheduler";
	public static final int DEFAULT_STACK_SIZE = 32;

	/**
	 * Invoke an asynchronous operation and obtain the result synchronous. The fiber will be blocked until the result is
	 * available. No kernel thread is blocked.
	 *
	 * @param consumer this should encapsulate the asynchronous operation. The handler is passed to it.
	 * @param <T>      the type of the result
	 * @return the result
	 */
	@Suspendable
	public static <T> T await(Consumer<Handler<AsyncResult<T>>> consumer) {
		try {
			return new AsyncAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					super.requestAsync();
					consumer.accept(this);
				}
			}.run();
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	@Suspendable
	public static <T> T await(Future<T> future) {
		try {
			return new AsyncAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					super.requestAsync();
					future.onComplete(this);
				}
			}.run();
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	@Suspendable
	public static <T> T awaitPromise(SuspendableAction1<Promise<T>> consumesPromise) {
		try {
			return new AsyncAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					super.requestAsync();
					var p = Promise.<T>promise();
					try {
						consumesPromise.call(p);
					} catch (SuspendExecution | InterruptedException suspendExecution) {
						p.fail(suspendExecution);
					}
					p.future().onComplete(this);
				}
			}.run();
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	/**
	 * Awaits a result. Does not die due to an interrupt. Very dangerous.
	 *
	 * @param consumer
	 * @param <T>
	 * @return
	 */
	@Suspendable
	public static <T> T awaitResultUninterruptibly(Consumer<Handler<AsyncResult<T>>> consumer) {
		try {
			return new AsyncAdaptor<T>() {
				@Override
				protected void checkInterrupted() throws InterruptedException {
				}

				@Override
				@Suspendable
				protected void requestAsync() {
					super.requestAsync();
					consumer.accept(this);
				}
			}.run();
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	/**
	 * Invoke an asynchronous operation and obtain the result synchronous. The fiber will be blocked until the result is
	 * available. No kernel thread is blocked. The consumer will be called inside a Fiber.
	 *
	 * @param consumer this should encapsulate the asynchronous operation. The handler is passed to it.
	 * @param <T>      the type of the result
	 * @return the result
	 */
	@Suspendable
	public static <T> T awaitFiber(Consumer<Handler<AsyncResult<T>>> consumer) {
		try {
			return new AsyncAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					if (Fiber.isCurrentFiber()) {
						try {
							super.requestAsync();
							consumer.accept(this);
						} catch (Exception e) {
							throw makeSafe(e);
						}
					} else {
						fiberHandler((Handler<Void> ignored) -> {
							super.requestAsync();
							consumer.accept(this);
						}).handle(null);
					}
				}
			}.run();
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	private static RuntimeException makeSafe(Throwable exception) {
		Throwable res = Throwables.getRootCause(exception);
		if (!(res instanceof RuntimeException)) {
			return new RuntimeException(res);
		} else {
			// Append the current stack so we see what called await fiber
			res.setStackTrace(concatAndFilterStackTrace(res, new Throwable()));
			return (RuntimeException) res;
		}
	}

	private static StackTraceElement[] concatAndFilterStackTrace(Throwable... throwables) {
		var length = 0;
		for (var i = 0; i < throwables.length; i++) {
			length += throwables[i].getStackTrace().length;
		}
		var newStack = new ArrayList<StackTraceElement>(length);
		for (Throwable throwable : throwables) {
			var stack = throwable.getStackTrace();
			for (var i = 0; i < stack.length; i++) {
				if (stack[i].getClassName().startsWith("co.paralleluniverse.fibers.") ||
						stack[i].getClassName().startsWith("io.vertx.ext.sync.") ||
						stack[i].getClassName().startsWith("io.netty.") ||
						stack[i].getClassName().startsWith("io.vertx.core.impl.") ||
						stack[i].getClassName().startsWith("sun.nio.") ||
						stack[i].getClassName().startsWith("java.base/java.util.concurrent") ||
						stack[i].getClassName().startsWith("java.util.concurrent")) {
					continue;
				}
				newStack.add(stack[i]);
			}
		}
		return newStack.toArray(new StackTraceElement[0]);
	}

	/**
	 * Invoke an asynchronous operation and obtain the result synchronous. The fiber will be blocked until the result is
	 * available. No kernel thread is blocked.
	 *
	 * @param consumer this should encapsulate the asynchronous operation. The handler is passed to it.
	 * @param timeout  In milliseconds when to cancel the awaited result
	 * @param <T>      the type of the result
	 * @return the result or null in case of a time out
	 */
	@Suspendable
	public static <T> T await(Consumer<Handler<AsyncResult<T>>> consumer, long timeout) {
		try {
			return new AsyncAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					try {
						super.requestAsync();
						consumer.accept(this);
					} catch (Exception e) {
						throw new VertxException(e);
					}
				}
			}.run(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException to) {
			return null;
		} catch (Throwable t) {
			throw new VertxException(t);
		}
	}

	/**
	 * Receive a single event from a handler synchronously. The fiber will be blocked until the event occurs. No kernel
	 * thread is blocked.
	 *
	 * @param consumer this should encapsulate the setting of the handler to receive the event. The handler is passed to
	 *                 it.
	 * @param <T>      the type of the event
	 * @return the event
	 */
	@Suspendable
	public static <T> T awaitEvent(Consumer<Handler<T>> consumer) {
		try {
			return new HandlerAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					try {
						consumer.accept(this);
					} catch (Exception e) {
						throw makeSafe(e);
					}
				}
			}.run();
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	/**
	 * Receive a single event from a handler synchronously. The fiber will be blocked until the event occurs. No kernel
	 * thread is blocked.
	 *
	 * @param consumer this should encapsulate the setting of the handler to receive the event. The handler is passed to
	 *                 it.
	 * @param timeout  In milliseconds when to cancel the awaited event
	 * @param <T>      the type of the event
	 * @return the event
	 */
	@Suspendable
	public static <T> T awaitEvent(Consumer<Handler<T>> consumer, long timeout) {
		try {
			return new HandlerAdaptor<T>() {
				@Override
				@Suspendable
				protected void requestAsync() {
					try {
						consumer.accept(this);
					} catch (Exception e) {
						throw makeSafe(e);
					}
				}
			}.run(timeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException to) {
			return null;
		} catch (Throwable t) {
			throw makeSafe(t);
		}
	}

	/**
	 * Convert a standard handler to a handler which runs on a fiber. This is necessary if you want to do fiber blocking
	 * synchronous operations in your handler.
	 *
	 * @param handler the standard handler
	 * @param <T>     the event type of the handler
	 * @return a wrapped handler that runs the handler on a fiber
	 */
	@Suspendable
	public static <T> Handler<T> fiberHandler(Handler<T> handler) {
		FiberScheduler scheduler = getContextScheduler();
		return p -> new Fiber<Void>(null, scheduler, DEFAULT_STACK_SIZE, () -> handler.handle(p)).start();
	}

	/**
	 * Create an adaptor that converts a stream of events from a handler into a receiver which allows the events to be
	 * received synchronously.
	 *
	 * @param <T> the type of the event
	 * @return the adaptor
	 */
	@Suspendable
	public static <T> HandlerReceiverAdaptor<T> streamAdaptor() {
		return new HandlerReceiverAdaptorImpl<>(getContextScheduler());
	}

	/**
	 * Like {@link #streamAdaptor()} but using the specified Quasar `Channel` instance. This is useful if you want to
	 * fine-tune the behaviour of the adaptor.
	 *
	 * @param channel the Quasar channel
	 * @param <T>     the type of the event
	 * @return the adaptor
	 */
	@Suspendable
	public static <T> HandlerReceiverAdaptor<T> streamAdaptor(Channel<T> channel) {
		return new HandlerReceiverAdaptorImpl<>(getContextScheduler(), channel);
	}

	/**
	 * Get the `FiberScheduler` for the current context. There should be only one instance per context.
	 *
	 * @return the scheduler
	 */
	@Suspendable
	public static FiberScheduler getContextScheduler() {
		Context context = Vertx.currentContext();
		return getContextScheduler(context);
	}

	@Suspendable
	public static FiberScheduler getContextScheduler(Context context) {
		if (context == null) {
			throw new IllegalStateException("Not in context");
		}
		if (!context.isEventLoopContext()) {
			throw new IllegalStateException("Not on event loop");
		}
		// We maintain one scheduler per context
		FiberScheduler scheduler = context.get(FIBER_SCHEDULER_CONTEXT_KEY);
		if (scheduler == null) {
			Thread eventLoop = Thread.currentThread();
			scheduler = new FiberExecutorScheduler("vertx.contextScheduler", command -> {
				if (Thread.currentThread() != eventLoop) {
					context.runOnContext(v -> command.run());
				} else {
					// Just run directly
					command.run();
				}
			});
			context.put(FIBER_SCHEDULER_CONTEXT_KEY, scheduler);
		}
		return scheduler;
	}

	/**
	 * Remove the scheduler for the current context
	 */
	@Suspendable
	public static void removeContextScheduler() {
		Context context = Vertx.currentContext();
		if (context != null) {
			context.remove(FIBER_SCHEDULER_CONTEXT_KEY);
		}
	}

	/**
	 * Wraps a void Vert.x handler.
	 */
	public static <T> BiConsumer<T, Throwable> voidHandler(Handler<AsyncResult<Void>> handler, Context context) {
		checkNotNull(handler, "handler cannot be null");
		checkNotNull(context, "context cannot be null");
		return (result, error) -> {
			if (error == null) {
				context.runOnContext(v -> Future.<Void>succeededFuture().onComplete(handler));
			} else {
				context.runOnContext(v -> Future.<Void>failedFuture(error).onComplete(handler));
			}
		};
	}

	/**
	 * Wraps a Vert.x handler.
	 */
	public static <T> BiConsumer<T, Throwable> resultHandler(Handler<AsyncResult<T>> handler, Context context) {
		checkNotNull(handler, "handler cannot be null");
		checkNotNull(context, "context cannot be null");
		return (result, error) -> {
			if (error == null) {
				context.runOnContext(v -> Future.succeededFuture(result).onComplete(handler));
			} else {
				context.runOnContext(v -> Future.<T>failedFuture(error).onComplete(handler));
			}
		};
	}

	/**
	 * Converts a return value.
	 */
	public static <T, U> BiConsumer<T, Throwable> convertHandler(Handler<AsyncResult<U>> handler, Function<T, U> converter, Context context) {
		return (result, error) -> {
			if (error == null) {
				context.runOnContext(v -> Future.succeededFuture(converter.apply(result)).onComplete(handler));
			} else {
				context.runOnContext(v -> Future.<U>failedFuture(error).onComplete(handler));
			}
		};
	}

	/**
	 * Defer a call by queueing it to be executed on the current {@link Context}
	 *
	 * @param handler The code to execute at a later time.
	 */
	@Suspendable
	public static void defer(SuspendableAction1<Void> handler) {
		Vertx.currentContext().runOnContext(fiber(handler));
	}

	@Suspendable
	public static <T> Handler<T> fiber(SuspendableAction1<T> handler) {
		var scheduler = getContextScheduler();
		return fiber(scheduler, handler);
	}

	@Suspendable
	public static <V> Future<V> fiber(SuspendableCallable<V> context) {
		var promise = Promise.<V>promise();
		var fiber = new Fiber<>(getContextScheduler(), () -> {
			try {
				promise.complete(context.run());
			} catch (Throwable t) {
				promise.fail(t);
			}
		});
//		fiber.setUncaughtExceptionHandler((f, e) -> Tracing.error(e));
		fiber.start();
		return promise.future();
	}

	@Suspendable
	public static void fiber(SuspendableRunnable context) {
		var fiber = new Fiber<Void>(getContextScheduler(), context::run);
//		fiber.setUncaughtExceptionHandler((f, e) -> Tracing.error(e));
		fiber.start();
	}

	@Suspendable
	public static <T> Handler<T> fiber(FiberScheduler scheduler, SuspendableAction1<T> handler) {
		return v -> {
			var fiber = new Fiber<Void>(scheduler, () -> handler.call(v));
//			fiber.setUncaughtExceptionHandler((f, e) -> Tracing.error(e));
			fiber.start();
		};
	}

	@Suspendable
	public static <R> R invoke(Supplier<R> func0) {
		return Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func0.get());
		}, false, h));
	}

	@Suspendable
	public static <R> R invoke(ThrowingSupplier<R> func0) {
		return Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func0.get());
		}, false, h));
	}

	@Suspendable
	public static <T> void invoke0(Consumer<T> func1, T arg1) {
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			Void res = Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
				func1.accept(arg1);
				done.complete();
			}, false, h));
		} else {
			func1.accept(arg1);
		}
	}

	@Suspendable
	public static void invoke0(NoArgs func0) {
		Void res = Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
			func0.apply();
			done.complete();
		}, false, h));
	}

	@Suspendable
	public static void invoke0(ThrowingNoArgs func0) {
		Void res = Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
			func0.apply();
			done.complete();
		}, false, h));
	}

	@Suspendable
	public static <T, R> R invoke(Function<T, R> func1, T arg1) {
		return Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func1.apply(arg1));
		}, false, h));
	}

	@Suspendable
	public static <T, R> R invoke(ThrowingFunction<T, R> func1, T arg1) {
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			return Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
				done.complete(func1.apply(arg1));
			}, false, h));
		} else {
			return func1.apply(arg1);
		}
	}

	@Suspendable
	public static <T1, T2, R> R invoke(BiFunction<T1, T2, R> func2, T1 arg1, T2 arg2) {
		return Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func2.apply(arg1, arg2));
		}, false, h));
	}

	@Suspendable
	public static <T1, T2, R> R invoke(ThrowingBiFunction<T1, T2, R> func2, T1 arg1, T2 arg2) {
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			return Sync.await(h -> Vertx.currentContext().executeBlocking(done -> {
				done.complete(func2.apply(arg1, arg2));
			}, false, h));
		} else {
			return func2.apply(arg1, arg2);
		}
	}

	@Suspendable
	public static <R> R invoke1(Consumer<Handler<AsyncResult<R>>> func) {
		return Sync.await(func);
	}

	@Suspendable
	public static <T1, R> R invoke(BiConsumer<T1, Handler<AsyncResult<R>>> func, T1 arg1) {
		return Sync.await(h -> func.accept(arg1, h));
	}

	@Suspendable
	public static <T1, T2, R> R invoke(TriConsumer<T1, T2, Handler<AsyncResult<R>>> func, T1 arg1, T2 arg2) {
		return Sync.await(h -> func.accept(arg1, arg2, h));
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
		return Sync.await(h -> future.whenComplete(resultHandler(h, Vertx.currentContext())));
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

	@FunctionalInterface
	public static interface TriConsumer<T1, T2, T3> {

		@Suspendable
		void accept(T1 arg1, T2 arg2, T3 arg3);
	}

	@FunctionalInterface
	public static interface NoArgs {
		@Suspendable
		void apply();
	}
}
