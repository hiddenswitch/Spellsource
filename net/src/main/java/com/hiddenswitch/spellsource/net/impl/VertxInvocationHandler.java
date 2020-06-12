package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.util.Serialization;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static io.vertx.ext.sync.Sync.awaitFiber;

/**
 * This invocation handler provides the infrastructure for calling a service method in a synchronous or asynchronous
 * way.
 *
 * @param <T> The service to which this invocation handler makes calls.
 * @see InvocationHandler for more about proxies.
 */
final class VertxInvocationHandler<T> implements InvocationHandler, Serializable {
	private static Logger LOGGER = LoggerFactory.getLogger(VertxInvocationHandler.class);
	@Nullable
	private final String deploymentId;
	@NotNull
	private final String name;
	@NotNull
	private final EventBus eb;
	private final boolean sync;
	private final Handler<AsyncResult<Object>> next;
	long timeout;

	VertxInvocationHandler(@Nullable String deploymentId, @NotNull EventBus eb, boolean sync, Handler<AsyncResult<Object>> next, @NotNull String name) {
		this.deploymentId = deploymentId;
		this.name = name;
		this.eb = eb;
		this.sync = sync;
		this.next = next;
		this.timeout = RpcClient.DEFAULT_TIMEOUT;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("deploymentId", deploymentId)
				.append("name", name)
				.append("sync", sync)
				.append("next", next != null ? next.toString() : null)
				.append("timeout", timeout)
				.toString();
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public Object invoke(Object proxy, Method method, Object[] args) throws VertxException, IllegalAccessException, InvocationTargetException {
		Tracer tracer = GlobalTracer.get();
		Tracer.SpanBuilder spanBuilder = tracer.buildSpan("VertxInvocationHandler/invoke/" + name);
		for (int i = 0; i < args.length; i++) {
			spanBuilder.withTag("arg" + i, args[i].getClass().getName());
		}
		Span span = spanBuilder
				.start();
		try {
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
				result = awaitFiber(done -> call(methodName, args, deliveryOptions, finalSerialization, done, method));
			} else {
				call(methodName, args, deliveryOptions, serialization, (res) -> {
					try {
						next.handle(res);
					} catch (RuntimeException runtimeException) {
						Tracing.error(runtimeException, span, true);
						throw runtimeException;
					} finally {
						span.finish();
					}
				}, method);
			}

			return result;
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	private void call(String methodName, Object[] args, final DeliveryOptions deliveryOptions, RpcOptions.Serialization serialization, Handler<AsyncResult<Object>> next, Method method) {
		Object message = null;

		String address = name + "/" + methodName;
		if (deploymentId != null) {
			address = deploymentId + "/" + address;
		}

		SuspendableAction1<AsyncResult<Message<Object>>> handler;

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
			message = JsonObject.mapFrom(args[0]);
			handler = new JsonReplyHandler(next, method.getReturnType());
		} else {
			throw new RuntimeException("Unspecified serialization option in invocation.");
		}

		eb.request(address, message, deliveryOptions, Sync.fiber(handler));
	}
}
