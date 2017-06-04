package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.sync.Sync;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class provides a way to register and connect to verticles advertised on the Vert.x {@link EventBus}. Its
 * functionality is similar to a simple remoting or RPC framework.
 *
 * @see #register(Object, Class, EventBus) for the method to register your service on the {@link EventBus}.
 * @see #connect(Class, EventBus) to connect to a service available on the {@link EventBus}.
 */
public class RPC {
	/**
	 * Registers a class to make its methods available to be called on the {@link EventBus}. Conceptually, this is like
	 * registering an instance of a microservice; you can have multiple {@code instance} of the same {@code
	 * serviceInterface}, and the {@link #connect(Class, EventBus)} will send a request to one of the {@code instance}
	 * serving that {@code serviceInterface}.
	 * <p>
	 * Vert.x provides the {@link EventBus} to enable communication between verticles (think Java beans) as a form of
	 * lightweight RPC. <a href="http://vertx.io/docs/vertx-service-proxy/java/">Vert.x's documented tools</a> are
	 * tedious to use and engineering-wise, they are restricted in terms of the datatypes they can transfer across the
	 * network. This method instead provides a way to register a plain Java interface as the specification for the
	 * protocol a verticle would respond to over the {@link EventBus}.
	 * <p>
	 * The registration function will register every public non-default method with a single argument specified in the
	 * interface with {@link EventBus#consumer(String)}, specifying the address as:
	 * <p>
	 * {@code serviceInterface.getName() + "::" + method.getName(); }
	 * <p>
	 * Internally, the system will use {@link Serialization#serialize(Object)} to serialize the message, which uses
	 * {@link java.io.ObjectOutputStream} to write the message (i.e., {@link java.io.Serializable} for serialization).
	 * <p>
	 * The following is an example of how to use {@link #register(Object, Class, EventBus)}. Typically you would
	 * register your service at the end of a verticle {@link io.vertx.core.Verticle#start(Future)} implementation.
	 * Suppose you are creating an {@code Inventory} service here:
	 * <p>
	 * <pre>
	 * {@code
	 * // You need an RPC client to the cards service.
	 * RpcClient<Cards> cards;
	 * public void start() throws SuspendExecution {
	 *     super.start();
	 *     // Connect to the Cards service.
	 *     cards = RPC.connect(Cards.class, vertx.eventBus());
	 *     // Register this instance as a host serving the Inventory API.
	 *     RPC.register(this, Inventory.class, vertx.eventBus());
	 * }
	 * </pre>
	 *
	 * @param instance         An instance of a concrete implementation of {@code serviceInterface}. This is a host for
	 *                         the service specified by {@code serviceInterface} on your cluster.
	 * @param serviceInterface An interface with one argument non-default methods whose argument and return value are
	 *                         both implementing {@link java.io.Serializable}. This is your API.
	 * @param eb               A reference to a Vert.x {@link EventBus}, typically accessed via {@code
	 *                         vertx.eventBus()}.
	 * @param <T>              The service interface type.
	 * @param <R>              A concrete implementation of the service interface type.
	 */
	@Suspendable
	public static <T, R extends T> void register(R instance, Class<T> serviceInterface, final EventBus eb) {
		final String name = serviceInterface.getName();

		for (Method method : serviceInterface.getDeclaredMethods()) {
			if (method.isDefault()
					|| method.getParameterCount() > 1) {
				return;
			}
			String methodName = name + "::" + method.getName();

			eb.consumer(methodName, Sync.fiberHandler(Consumer.of(arg -> {
				try {
					return method.invoke(instance, arg);
				} catch (InvocationTargetException e) {
					RuntimeException re = (RuntimeException) (e.getTargetException());
					if (re != null) {
						throw re;
					}
					return null;
				} catch (IllegalAccessException e) {
					return null;
				} catch (Throwable e) {
					throw e;
				}
			})));
		}
	}

	/**
	 * Connects to the given {@code serviceInterface} on the {@link EventBus} and gives you an {@link RpcClientImpl} you can
	 * call methods on. Does not require the service to be running in order to be connected to.
	 * <p>
	 * Internally, this method creates a {@link Proxy} instance that implements {@link T}. When you call one of {@link
	 * T}'s methods, this proxy will {@link EventBus#send(String, Object)} a {@link java.io.Serializable}-serialized
	 * message to an address of the form:
	 * <p>
	 * {@code serviceInterface.getName() + "::" + method.getName(); }
	 * <p>
	 * It will unpack the reply and give it back to you.
	 * <p>
	 * Below is an example of using this method:
	 * <pre>
	 * {@code
	 * public void localMethod() throws SuspendExecution {
	 *     // Connect to the Cards service. Typically you would do this in a start method of a verticle.
	 *     RpcClient<Cards> cards = RPC.connect(Cards.class, vertx.eventBus());
	 *     // Use the cards service. Note that we use "sync()" to idiomatically call this API "synchronously", in the
	 *     // sense that continuations in Fibers are synchronous.
	 *     GetCardResponse response = cards.sync().getCard(new GetCardRequest().withCardId("minion_boulderfist_ogre"));
	 * }
	 * </pre>
	 *
	 * @param serviceInterface The Java interface that corresponds to the API you're connecting to.
	 * @param bus              An {@link EventBus}, typically accessed with {@code vertx.eventBus()}.
	 * @param <T>              The type of the Java interface.
	 * @return An {@link RpcClientImpl }.
	 */
	@Suspendable
	@SuppressWarnings("unchecked")
	public static <T> RpcClient<T> connect(Class<? extends T> serviceInterface, final EventBus bus) {
		final VertxInvocationHandler<T> invocationHandler = new VertxInvocationHandler<>();

		RpcClientImpl<T> result = new RpcClientImpl<>((T) Proxy.newProxyInstance(
				serviceInterface.getClassLoader(),
				new Class[]{serviceInterface},
				invocationHandler
		));

		invocationHandler.eb = bus;
		invocationHandler.name = serviceInterface.getName();
		invocationHandler.ApiClient = result;

		return result;
	}

}
