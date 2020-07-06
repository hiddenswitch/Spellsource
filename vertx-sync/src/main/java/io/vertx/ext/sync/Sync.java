package io.vertx.ext.sync;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import com.google.common.base.Throwables;
import io.vertx.core.*;
import io.vertx.ext.sync.impl.AsyncAdaptor;
import io.vertx.ext.sync.impl.HandlerAdaptor;
import io.vertx.ext.sync.impl.HandlerReceiverAdaptorImpl;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

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
	public static <T> T awaitResult(Consumer<Handler<AsyncResult<T>>> consumer) {
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
	public static <T> T awaitResult(Consumer<Handler<AsyncResult<T>>> consumer, long timeout) {
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
}
