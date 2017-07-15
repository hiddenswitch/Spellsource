package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.proto3.net.models.CreateAccountRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.ext.sync.Sync;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides a way to register and connect to verticles advertised on the Vert.x {@link EventBus}. Its
 * functionality is similar to a simple remoting or RPC framework.
 *
 * @see #register(Object, Class, EventBus) for the method to register your service on the {@link EventBus}.
 * @see #connect(Class, EventBus) to connect to a service available on the {@link EventBus}.
 */
public class Rpc {
	/**
	 * Registers a class to make its methods available to be called on the {@link EventBus}. This method will wait until
	 * the class is ready to be contacted on the {@link EventBus}; i.e., once all its {@link EventBus#consumer(String)}
	 * have been registered.
	 * <p>
	 * Conceptually, this is like registering an instance of a microservice; you can have multiple {@code instance} of
	 * the same {@code serviceInterface}, and the {@link #connect(Class, EventBus)} will send a request to one of the
	 * {@code instance} serving that {@code serviceInterface}.
	 * <p>
	 * Vert.x provides the {@link EventBus} to enable communication between verticles (think Java beans) as a form of
	 * lightweight RPC. <a href="http://vertx.io/docs/vertx-service-proxy/java/">Vert.x's documented tools</a> are
	 * tedious to use and engineering-wise, they are restricted in terms of the datatypes they can transfer across the
	 * network. This method instead provides a way to register a plain Java interface as the specification for the
	 * protocol a verticle would respond to over the {@link EventBus}.
	 * <p>
	 * The registration function will register every non-static method specified in the interface with {@link
	 * EventBus#consumer(String)}, specifying the address as:
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
	 * @return A registration that can be later unregistered with {@link #unregister(Registration)}
	 * @see #getAddress(Class, SuspendableAction1) for a way to get the address of a given method on the event bus.
	 */
	public static <T, R extends T> Registration register(R instance, Class<T> serviceInterface, final EventBus eb) throws SuspendExecution {
		return Sync.awaitResult(h -> register(instance, serviceInterface, eb, h));
	}

	/**
	 * An asynchronous version of {@link #register(Object, Class, EventBus)}
	 *
	 * @param instance         An instance of a concrete implementation of {@code serviceInterface}. This is a host for
	 *                         the service specified by {@code serviceInterface} on your cluster.
	 * @param serviceInterface An interface with one argument non-default methods whose argument and return value are
	 *                         both implementing {@link java.io.Serializable}. This is your API.
	 * @param eb               A reference to a Vert.x {@link EventBus}, typically accessed via {@code
	 *                         vertx.eventBus()}.
	 * @param handler          A handler that receives a registration that can be later unregistered with {@link
	 *                         #unregister(Registration)}
	 * @param <T>              The service interface type.
	 * @param <R>              A concrete implementation of the service interface type.
	 */
	public static <T, R extends T> void register(R instance, Class<T> serviceInterface, final EventBus eb, Handler<AsyncResult<Registration>> handler) {
		final String name = serviceInterface.getName();

		Registration registration = new Registration();

		registration.setMessageConsumers(Stream.of(serviceInterface.getDeclaredMethods()).map(method -> {
			String methodName = name + "::" + method.getName();

			SuspendableFunction<Object, Object> method1 = arg -> {
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
			};

			// Get the context at the time of calling this function
			RpcOptions.Serialization serialization = RpcOptions.Serialization.JAVA;
			RpcOptions rpcOptions = method.getAnnotation(RpcOptions.class);
			if (rpcOptions != null) {
				serialization = rpcOptions.serialization();
			}
			if (serialization == RpcOptions.Serialization.JAVA) {
				return eb.consumer(methodName, Sync.fiberHandler(new BufferEventBusHandler<>(method1)));
			} else if (serialization == RpcOptions.Serialization.JSON) {
				return eb.consumer(methodName, Sync.fiberHandler(new JsonEventBusHandler<>(method1, method.getParameterTypes()[0])));
			}

			throw new RuntimeException("Not reachable.");
		}).collect(Collectors.toList()));

		CompositeFuture.all(registration.getMessageConsumers()
				.stream().map(consumer -> {
					Future<Void> future = Future.future();
					consumer.completionHandler(future);
					return future;
				}).collect(Collectors.toList())).setHandler(then -> {
			if (then.succeeded()) {
				handler.handle(Future.succeededFuture(registration));
			} else {
				handler.handle(Future.failedFuture(then.cause()));
			}
		});
	}

	/**
	 * Connects to the given {@code serviceInterface} on the {@link EventBus} and gives you an {@link
	 * NetworkedRpcClient} you can call methods on. Does not require the service to be running in order to be connected
	 * to.
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
	 * }
	 * </pre>
	 *
	 * @param serviceInterface The Java interface that corresponds to the API you're connecting to.
	 * @param bus              An {@link EventBus}, typically accessed with {@code vertx.eventBus()}.
	 * @param <T>              The type of the Java interface.
	 * @return An {@link NetworkedRpcClient }.
	 */
	@Suspendable
	@SuppressWarnings("unchecked")
	public static <T> RpcClient<T> connect(Class<? extends T> serviceInterface, final EventBus bus) {
		return new NetworkedRpcClient<T>(bus, serviceInterface);
	}

	/**
	 * Gets an address from a named method call.
	 * <p>
	 * For example, to get the address of an {@link com.hiddenswitch.proto3.net.Accounts#createAccount(CreateAccountRequest)}
	 * call, do:
	 * <p>
	 * <pre>
	 *     {@code
	 *     RPC.getAddress(Accounts.class, accounts -> accounts.createAccount(null));
	 *     }
	 * </pre>
	 * Note you can enter anything for the argument, and you don't need to do anything with the return value.
	 * <p>
	 * Internally, this creates a proxy that implements the interface and sees which method you called.
	 *
	 * @param serviceInterface The service / interface
	 * @param methodCall       A lambda where you call the method on a fake instance of the service.
	 * @param <T>              The interface type.
	 * @return A string corresponding to an {@link EventBus} address for this service method.
	 */
	@SuppressWarnings("unchecked")
	public static <T> String getAddress(Class<? extends T> serviceInterface, SuspendableAction1<T> methodCall) {
		final String[] outName = {null};
		final T proxy = (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[]{serviceInterface}, (proxy1, method, args) -> {
			outName[0] = method.getName();
			return null;
		});

		try {
			methodCall.call(proxy);
		} catch (SuspendExecution | InterruptedException ignored) {
		}

		return serviceInterface.getName() + "::" + outName[0];
	}

	/**
	 * Unregister this service from the event bus.
	 *
	 * @param registration A registration entry returned by {@link #register(Object, Class, EventBus)}
	 * @param handler      A handler that returns when all the consumers have been unregistered.
	 */
	public static void unregister(Registration registration, Handler<AsyncResult<CompositeFuture>> handler) {
		List<MessageConsumer> consumers = registration.getMessageConsumers();
		CompositeFuture.all(consumers.stream().map(consumer -> {
			Future<Void> future = Future.future();
			consumer.unregister(future.completer());
			return (Future) future;
		}).collect(Collectors.toList())).setHandler(handler);
	}

	/**
	 * Unregister this service from the event bus.
	 *
	 * @param registration A registration entry return by {@link #register(Object, Class, EventBus)}
	 * @return A succeeded future when all of the registration entries have been removed.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	public static CompositeFuture unregister(Registration registration) throws SuspendExecution {
		return Sync.awaitResult(h -> unregister(registration, h));
	}
}
