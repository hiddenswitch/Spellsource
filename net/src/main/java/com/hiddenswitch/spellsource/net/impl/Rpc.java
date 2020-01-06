package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableFunction;
import com.hiddenswitch.spellsource.net.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.util.Serialization;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.Sync;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hiddenswitch.spellsource.net.impl.Sync.suspendableHandler;

/**
 * This class provides a way to register and connect to verticles advertised on the Vert.x {@link EventBus}. Its
 * functionality is similar to a simple remoting or RPC framework.
 *
 * @see #register(Object, Class) for the method to register your service on the {@link EventBus}.
 * @see #connect(Class) to connect to a service available on the {@link EventBus}.
 */
public class Rpc {

	/**
	 * Registers a class to make its methods available to be called on the {@link EventBus}. This method will wait until
	 * the class is ready to be contacted on the {@link EventBus}; i.e., once all its {@link EventBus#consumer(String)}
	 * have been registered.
	 * <p>
	 * Conceptually, this is like registering an instance of a microservice; you can have multiple {@code instance} of the
	 * same {@code serviceInterface}, and the {@link #connect(Class)} will send a request to one of the {@code instance}
	 * serving that {@code serviceInterface}.
	 * <p>
	 * Vert.x provides the {@link EventBus} to enable communication between verticles (think Java beans) as a form of
	 * lightweight RPC. <a href="http://vertx.io/docs/vertx-service-proxy/java/">Vert.x's documented tools</a> are tedious
	 * to use and engineering-wise, they are restricted in terms of the datatypes they can transfer across the network.
	 * This method instead provides a way to register a plain Java interface as the specification for the protocol a
	 * verticle would respond to over the {@link EventBus}.
	 * <p>
	 * The registration function will register every non-static method specified in the interface with {@link
	 * EventBus#consumer(String)}, specifying the address as:
	 * <p>
	 * {@code serviceInterface.getName() + "/" + method.getName(); }
	 * <p>
	 * Internally, the system will use {@link Serialization#serialize(Object)} to serialize the message, which uses {@link
	 * java.io.ObjectOutputStream} to write the message (i.e., {@link java.io.Serializable} for serialization).
	 * <p>
	 * The following is an example of how to use {@link #register(Object, Class)}. Typically you would register your
	 * service at the end of a verticle {@link io.vertx.core.Verticle#start(Future)} implementation. Suppose you are
	 * creating an {@code Inventory} service here:
	 *
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
	 * @param <T>              The service interface type.
	 * @param <R>              A concrete implementation of the service interface type.
	 * @param instance         An instance of a concrete implementation of {@code serviceInterface}. This is a host for
	 *                         the service specified by {@code serviceInterface} on your cluster.
	 * @param serviceInterface An interface with one argument non-default methods whose argument and return value are both
	 *                         implementing {@link java.io.Serializable}. This is your API.
	 * @return A registration that can be later unregistered with {@link #unregister(Registration)}
	 * @see #getAddress(Class, SuspendableAction1) for a way to get the address of a given method on the event bus.
	 */
	public static <T, R extends T> Registration register(R instance, Class<T> serviceInterface) throws SuspendExecution {
		return Sync.awaitResult(h -> register(instance, serviceInterface, h));
	}

	/**
	 * An asynchronous version of {@link #register(Object, Class)}
	 *
	 * @param <T>              The service interface type.
	 * @param <R>              A concrete implementation of the service interface type.
	 * @param instance         An instance of a concrete implementation of {@code serviceInterface}. This is a host for
	 *                         the service specified by {@code serviceInterface} on your cluster.
	 * @param serviceInterface An interface with one argument non-default methods whose argument and return value are both
	 *                         implementing {@link java.io.Serializable}. This is your API.
	 * @param handler          A handler that receives a registration that can be later unregistered with {@link
	 *                         #unregister(Registration)}
	 */
	@SuppressWarnings("unchecked")
	public static <T, R extends T> void register(R instance, Class<T> serviceInterface, Handler<AsyncResult<Registration>> handler) {
		final EventBus eb = Vertx.currentContext().owner().eventBus();
		final String name = serviceInterface.getName();

		Registration registration = new Registration();

		registration.setMessageConsumers(Stream.of(serviceInterface.getDeclaredMethods()).flatMap(method -> {
			if (Modifier.isStatic(method.getModifiers())
					|| method.isDefault()
					|| method.getDeclaringClass().equals(Verticle.class)) {
				return Stream.empty();
			}

			final String address = name + "/" + method.getName();

			SuspendableFunction<Object, Object> finalMethod = arg -> method.invoke(instance, arg);

			// Get the context at the time of calling this function
			RpcOptions.Serialization serialization = defaultSerialization();
			RpcOptions rpcOptions = method.getAnnotation(RpcOptions.class);
			if (rpcOptions != null) {
				serialization = rpcOptions.serialization();
			}
			SuspendableAction1 eventBusHandler;
			MessageConsumer consumer;
			if (serialization == RpcOptions.Serialization.JAVA) {
				eventBusHandler = new BufferEventBusHandler<>(finalMethod);
				consumer = eb.consumer(address, suspendableHandler(eventBusHandler));
			} else if (serialization == RpcOptions.Serialization.JSON) {
				eventBusHandler = new JsonEventBusHandler<>(finalMethod, method.getParameterTypes()[0]);
				consumer = eb.<JsonObject>consumer(address, suspendableHandler(eventBusHandler));
			} else {
				throw new RuntimeException("Unexpected serialization option for this event bus handler.");
			}


			// If the instance we are consuming supports deployment IDs, register a function prefixed with the
			// deployment ID in order to support stateful message consumers.
			if (instance instanceof AbstractVerticle) {
				AbstractVerticle deployedInstance = (AbstractVerticle) instance;
				// Specific deployment instance ID consumer.
				final String specificInstanceAddress = deployedInstance.deploymentID() + "::" + address;
				MessageConsumer consumerSpecific = eb.consumer(specificInstanceAddress, suspendableHandler(eventBusHandler));
				return Stream.of(consumer, consumerSpecific);
			}

			return Stream.of(consumer);
		}).collect(Collectors.toList()));

		CompositeFuture.join(registration.getMessageConsumers()
				.stream().map(consumer -> {
					Promise<Void> future = Promise.promise();
					consumer.completionHandler(future);
					return future.future();
				}).collect(Collectors.toList())).setHandler(then -> {
			if (then.succeeded()) {
				handler.handle(Future.succeededFuture(registration));
			} else {
				handler.handle(Future.failedFuture(then.cause()));
			}
		});
	}

	/**
	 * Connects to the given {@code serviceInterface} on the {@link EventBus} and gives you an {@link NetworkedRpcClient}
	 * you can call methods on. Does not require the service to be running in order to be connected to.
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
	 * @param <T>              The type of the Java interface.
	 * @return An {@link NetworkedRpcClient }.
	 */
	@Suspendable
	@SuppressWarnings("unchecked")
	public static <T> RpcClient<T> connect(Class<? extends T> serviceInterface) {
		return new NetworkedRpcClient<T>(Vertx.currentContext().owner().eventBus(), serviceInterface);
	}

	/**
	 * Gets an address from a named method call.
	 * <p>
	 * For example, to get the address of an {@link Accounts#createAccount(CreateAccountRequest)} call, do:
	 *
	 * <pre>
	 *     {@code
	 *     RPC.getAddress(Accounts.class, accounts -> Accounts.createAccount(null));
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

		return serviceInterface.getName() + "/" + outName[0];
	}

	/**
	 * Unregister this service from the event bus.
	 *
	 * @param registration A registration entry returned by {@link #register(Object, Class)}
	 * @param handler      A handler that returns when all the consumers have been unregistered.
	 */
	@SuppressWarnings("unchecked")
	public static void unregister(Registration registration, Handler<AsyncResult<CompositeFuture>> handler) {
		List<MessageConsumer> consumers = registration.getMessageConsumers();
		CompositeFuture.all(consumers.stream().map(consumer -> {
			Promise<Void> promise = Promise.promise();
			consumer.unregister(promise);
			return (Future) promise.future();
		}).collect(Collectors.toList())).setHandler(handler);
	}

	/**
	 * Unregister this service from the event bus.
	 *
	 * @param registration A registration entry return by {@link #register(Object, Class)}
	 * @return A succeeded future when all of the registration entries have been removed.
	 * @throws SuspendExecution
	 */
	public static CompositeFuture unregister(Registration registration) throws SuspendExecution {
		return Sync.awaitResult(h -> unregister(registration, h));
	}

	public static RpcOptions.Serialization defaultSerialization() {
		return RpcOptions.Serialization.JSON;
	}
}
