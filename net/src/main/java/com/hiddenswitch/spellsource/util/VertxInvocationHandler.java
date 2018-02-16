package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static io.vertx.ext.sync.Sync.awaitFiber;

/**
 * This invocation handler provides the infrastructure for calling a service method in a synchronous or asynchronous
 * way.
 *
 * @param <T> The service to which this invocation handler makes calls.
 * @see InvocationHandler for more about proxies.
 * @see java.lang.reflect.Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler) for more about proxies.
 */
class VertxInvocationHandler<T> implements InvocationHandler, Serializable {
	final String deploymentId;
	final String name;
	final EventBus eb;
	final boolean sync;
	final Handler<AsyncResult<Object>> next;
	long timeout;

	VertxInvocationHandler(String deploymentId, EventBus eb, boolean sync, Handler<AsyncResult<Object>> next, String name) {
		this.deploymentId = deploymentId;
		this.name = name;
		this.eb = eb;
		this.sync = sync;
		this.next = next;
		this.timeout = RpcClient.DEFAULT_TIMEOUT;
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public Object invoke(Object proxy, Method method, Object[] args) throws VertxException, IllegalAccessException, InvocationTargetException {
		// Call default methods normally
		if (Objects.equals(method.getName(), "toString")) {
			return "Proxy Object for " + name;
		}

		if (method.isDefault()) {
			// Invoked with the proxy instance, which shouldn't matter for anything that is a default method on an interface
			return method.invoke(proxy, args);
		}

		final boolean sync = this.sync;
		final Handler<AsyncResult<Object>> next = this.next;

		final String methodName = method.getName();
		if (next == null
				&& !sync) {
			throw new RuntimeException();
		}

		if (eb == null) {
			throw new RuntimeException();
		}
		Object result = null;
		final DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(timeout);
		RpcOptions options = method.getAnnotation(RpcOptions.class);
		RpcOptions.Serialization serialization = Rpc.defaultSerialization();

		if (options != null) {
			deliveryOptions.setSendTimeout(options.sendTimeoutMS());
			serialization = options.serialization();
		}

		if (sync) {
			final RpcOptions.Serialization finalSerialization = serialization;
			result = awaitFiber(done -> {
				call(methodName, args, deliveryOptions, finalSerialization, done, method);
			});
		} else {
			call(methodName, args, deliveryOptions, serialization, next, method);
		}

		return result;
	}

	@Suspendable
	private void call(String methodName, Object[] args, final DeliveryOptions deliveryOptions, RpcOptions.Serialization serialization, Handler<AsyncResult<Object>> next, Method method) {
		Object message = null;

		String address = name + "::" + methodName;
		if (deploymentId != null) {
			address = deploymentId + "::" + address;
		}

		Handler<AsyncResult<Message<Object>>> handler;

		if (serialization == RpcOptions.Serialization.JAVA) {
			final Buffer result = Buffer.buffer(512);

			try {
				Serialization.serialize(args[0], new VertxBufferOutputStream(result));
			} catch (IOException e) {
				next.handle(Future.failedFuture(e));
				return;
			}

			message = result;
			handler = new ReplyHandler(next);
		} else if (serialization == RpcOptions.Serialization.JSON) {
			message = Serialization.serialize(args[0]);
			handler = new JsonReplyHandler(next, method.getReturnType());
		} else {
			throw new RuntimeException("Unspecified serialization option in invocation.");
		}

		eb.send(address, message, deliveryOptions, Sync.fiberHandler(handler));
	}
}
