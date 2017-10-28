package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * A remote procedure call client. Its methods, {@link #sync()}, {@link #uncheckedSync()} and {@link #async(Handler)}
 * provides three different convenient ways for you to make calls to other microservices using the same interface {@link
 * T}.
 * <p>
 * Internally, this is proxy of a Vertx event bus communicating service.
 *
 * @param <T> The service class proxied.
 */
public interface RpcClient<T> {
	long DEFAULT_TIMEOUT = 8000;

	/**
	 * Make an RPC call with an idiomatically asynchronous coding convention, like what you would expect in Node.
	 * <p>
	 * You <b>CANNOT</b> reuse this proxy for more than one method call. You must always call {@link #async(Handler)}
	 * for subsequent usage of the proxy.
	 * <p>
	 * As an example:
	 * <p>
	 * <pre>
	 *     {@code
	 *     // Notice that unusually, the handler comes before the actual method call, and accepts any type as its
	 * argument.
	 *     accounts.async((AsyncResult<UserRecord> response) -> {
	 *          // The handler. Do something with the response.
	 *          if (response.failed()
	 *          || response.result() == null) {
	 *              resultHandler.handle(Future.failedFuture(response.cause()));
	 *              return;
	 *          }
	 *          resultHandler.handle(Future.succeededFuture(response.result()));
	 *     }).getWithToken(authInfo.getString("token"));
	 *     }
	 * </pre>
	 * <p>
	 * Use {@link #async(Handler)} in contexts like a conventional {@link Handler} which isn't running in a {@link
	 * co.paralleluniverse.fibers.Fiber}. Otherwise, you're strongly encouraged to use {@link #sync()}, which is
	 * idiomatically cleaner.
	 * <p>
	 * Internally, this retrieves a proxy of the service configured for async calls. Pass the handler for the result
	 * first. The methods called on the proxy will <b>not</b> return their values, they will return null. The call will
	 * go across the {@link io.vertx.core.eventbus.EventBus} and arrive to your handler.
	 *
	 * @param handler The handler of the async call. Fully specify the handler to get the right type for the method you
	 *                subsequently call on the proxy.
	 * @param <R>     The return type of the method you will call on the proxy. This is typically a Response object.
	 * @return A proxy whose methods will return null.
	 */
	default <R> T async(Handler<AsyncResult<R>> handler) {
		return this.async(handler, DEFAULT_TIMEOUT);
	}

	/**
	 * Calls the {@link #async(Handler)} idiom to return a promise.
	 *
	 * @param callHere A place to actually call the service. Should return the response type you desire.
	 * @param <Res>    The response type of the method to call.
	 * @return A {@link Future} whose completer is passed to {@link #async(Handler)}
	 */
	default <Res> Future<Res> promise(SuspendableFunction<T, Res> callHere) {
		Future<Res> future = Future.future();
		try {
			callHere.apply(async(future.completer()));
		} catch (SuspendExecution | InterruptedException ignore) {
			// Ignored, since this is just around to ensure fiber stuff gets fiber'ed
		}
		return future;
	}

	/**
	 * Executes {@link #async(Handler)} with the specified timeout.
	 *
	 * @param handler A handler for this function,.
	 * @param timeout The timeout, in milliseconds, until no reply is assuemed.
	 * @param <R>     The result time.
	 * @return A proxy whose methods will return null.
	 * @see #async(Handler) for more complete documentation.
	 */
	@SuppressWarnings("unchecked")
	<R> T async(Handler<AsyncResult<R>> handler, long timeout);

	/**
	 * Gets ready to make an idiomatically synchronous (in the sense of {@link co.paralleluniverse.fibers.Fiber}) call
	 * to an API.
	 * <p>
	 * Internally, retrieves a proxy of the service configured for Fibers "sync" calls. You must be inside a Fiber
	 * (e.g., a handler wrapped in {@link Sync#suspendableHandler(SuspendableAction1)}) to use this proxy. The methods
	 * called on the proxy will return their values, even though the call with go across the event bus.
	 * <p>
	 * You <b>can</b> reuse this proxy for more than one sync method call.
	 *
	 * @return {T} A proxy whose methods will return the actual values.
	 */
	@Suspendable
	T sync() throws SuspendExecution, InterruptedException;

	/**
	 * Gets ready to make an idiomatically synchronous (in the sense of {@link co.paralleluniverse.fibers.Fiber}) call
	 * to an API. Does not throw {@link SuspendExecution}, so it can be used in code that is known to be running in a
	 * {@link co.paralleluniverse.fibers.Fiber} but not enforced to be.
	 * <p>
	 * Internally, retrieves a proxy of the service configured for Fibers "sync" calls. You must be inside a Fiber
	 * (e.g., a handler wrapped in {@link Sync#suspendableHandler(SuspendableAction1)}) to use this proxy. The methods
	 * called on the proxy will return their values, even though the call with go across {@link
	 * io.vertx.core.eventbus.EventBus}.
	 * <p>
	 * You can reuse this proxy for more than one sync method call.
	 *
	 * @return {T} A proxy whose methods will return the actual values.
	 */
	@Suspendable
	T uncheckedSync();
}
