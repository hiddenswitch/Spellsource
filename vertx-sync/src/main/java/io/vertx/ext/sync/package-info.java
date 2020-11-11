/**
 *
 * = Vertx-Sync
 *
 * Vertx-sync is a set of utilities that allow you to perform asynchronous operations and receive events in a
 * synchronous way, but without blocking kernel threads.
 *
 * == Introduction
 *
 * One of the key advantages of Vert.x over many legacy application platforms is that it is almost entirely non-blocking
 * (of kernel threads) - this allows it to handle a lot of concurrency (e.g. handle many connections, or messages) using
 * a very small number of kernel threads, which allows it to scale very well.
 *
 * The non blocking nature of Vert.x leads to asynchronous APIs. Asynchronous APIs can take various forms including
 * callback style, promises or Rx-style. Vert.x uses callback style in most places (although it also supports Rx).
 *
 * In some cases, programming using asynchronous APIs can be more challenging than using a direct synchronous style, in
 * particular if you have several operations that you want to do in sequence. Also error propagation is often more complex
 * when using asynchronous APIs.
 *
 * Vertx-sync allows you to work with asynchronous APIs, but using a direct synchronous style that you're already
 * familiar with.
 *
 * It does this by using `fibers`. Fibers are very lightweight threads that do not correspond to underlying kernel threads.
 * When they are blocked they do not block a kernel thread.
 *
 * Vert-sync uses http://docs.paralleluniverse.co/quasar/[Quasar] to implement the fibers.
 *
 * NOTE: Vert-sync currently only works with Java.
 *
 * == SyncVerticle
 * 
 * In order to use vertx-sync you must deploy your code as instances of `io.vertx.ext.sync.SyncVerticle`.
 * You should override the `start()` and (optionally) the `stop()` methods of the verticle.
 *
 * Those methods *must* be annotated with the `@Suspendable` annotation.
 * 
 * Once you've written your sync verticle(s) you deploy them in exactly the same way as any other verticle.
 * 
 * == Instrumentation
 * 
 * Vert.x uses Quasar which implements fibers by using bytecode instrumentation. This is done at run-time using a java
 * agent.
 * 
 * In order for this to work you must start the JVM specifying the java agent jar which is located in the quasar-core
 * jar.
 * 
 * TODO how to reference quasar core jar in fatjar?
 * 
 * ----
 * -javaagent:/path/to/quasar/core/quasar-core.jar
 * ----
 * 
 * If you are using the `vertx` command line tools, the agent configuration can be enabled by setting the `ENABLE_VERTX_SYNC_AGENT`
 * environment variable to `true`, before executing `vertx`.
 *
 * You can also use a offline instrumentation as with the https://github.com/vy/quasar-maven-plugin[quasar-maven-plugin].
 * Check the http://docs.paralleluniverse.co/quasar/[Quasar documentation] for more details.
 * 
 * == Getting one-shot async results
 *
 * Many async operations in Vert.x-land take a `Handler<AsyncResult<T>>` as the last argument. An example would
 * executing a find using the Vert.x Mongo client or sending an event bus message and getting a reply.
 * 
 * Vertx-sync allows you to get the result of a one-shot asynchronous operation in a synchronous way.
 *
 * This is done by using the {@link io.vertx.ext.sync.Sync#await(java.util.function.Consumer)} method.
 *
 * The method is executed specifying the asynchronous operation that you want to execute in the form of a {@link java.util.function.Consumer},
 * the consumer is passed the handler at run-time.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#syncResultExample(io.vertx.core.Vertx)}
 * ----
 *
 * In the above example the fiber is blocked until the reply is returned but no kernel thread is blocked.
 *
 * == Getting one-shot events
 *
 * Vertx-sync can be used to get one-shot events in a synchronous way, for example firings of timers, or the executing of
 * an end handler. This is achieved using the {@link io.vertx.ext.sync.Sync#awaitEvent(java.util.function.Consumer)} method.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#syncEventExample(io.vertx.core.Vertx)}
 * ----
 * 
 * == Streams of events
 *
 * In many places in Vert.x streams of events are provided by passing them to handlers.
 *
 * Examples include event bus message consumers and HTTP server requests on an HTTP server.
 *
 * Vert-sync allows you to receive events from such streams in a synchronous way.
 *
 * You do this with an instance of {@link io.vertx.ext.sync.HandlerReceiverAdaptor} which implements both
 * {@link io.vertx.core.Handler} and {@link io.vertx.ext.sync.Receiver}. You create an instance using
 * {@link io.vertx.ext.sync.Sync#streamAdaptor()}.
 *
 * You can set it as a normal handler and then use the methods on {@link io.vertx.ext.sync.Receiver} to receive
 * events synchronously.
 *
 * Here's an example using an event bus message consumer:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#streamExample(io.vertx.core.Vertx)}
 * ----
 *
 * == Using a `FiberHandler`
 *
 * If you want to do use fibers in a normal handler, e.g. in the request handler of an Http Server then you must first
 * convert the normal handler to a fiber handler.
 *
 * The fiber handler runs the normal handler on a fiber.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#fiberHandlerExample(io.vertx.core.Vertx)}
 * ----
 *
 * == Further examples
 *
 * There are a set of working examples demonstrating vertx-sync in action in the
 * https://github.com/vert-x3/vertx-examples/tree/master/sync-examples[examples repository]
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-sync", groupPackage = "io.vertx")
package io.vertx.ext.sync;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
