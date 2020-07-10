/**
 * <b>The core multiplayer code for Spellsource.</b>
 * <p>
 * Spellsource uses Vertx as a REST and web socket framework, Hazelcast to share data between members of the serving
 * cluster, Quasar to make async code behave synchronously with fibers, and MongoDB as its database persistence
 * backend.
 * <p>
 * The convention is to implement as much server code as possible as a static method in an interface. Mark the method as
 * {@link co.paralleluniverse.fibers.Suspendable} or make it {@code throw SuspendExecution} to ensure it works as a
 * fiber. Then you can use methods like {@link com.hiddenswitch.spellsource.net.impl.Mongo#findOne(java.lang.String,
 * io.vertx.core.json.JsonObject, io.vertx.core.json.JsonObject)}, which appears synchronous but actually uses Quasar
 * fibers to perform an async call with Netty (the high performance way of doing things).
 * <p>
 * To easily create JSON objects, use {@link com.hiddenswitch.spellsource.net.impl.QuickJson#json(java.lang.Object...)}. For
 * example, to create the object:
 * <pre>
 *   {
 *     "$set": {
 *       "test.field": "value"
 *     },
 *     "$inc": {
 *       "other.field": 2
 *     }
 *   }
 * </pre>
 * Use:
 * <pre>
 *   {@code
 *     import static com.hiddenswitch.spellsource.util.QuickJson.*;
 *
 *     JsonObject obj = json("$set", json("test.field", "value"), "$inc", json("other.field", 2));
 *   }
 * </pre>
 * Observe the {@code import static} line to make things as elegant as possible.
 * <p>
 * To turn async code into something that returns on its callback, use {@link io.vertx.ext.sync.Sync#awaitResult(java.util.function.Consumer)}.
 * For example:
 * <pre>
 *   {@code
 *     import static io.vertx.ext.sync.Sync.awaitResult;
 *
 *     MailResult mailResult = awaitResult(h -> mailClient.sendMail(new MailMessage(), h));
 *   }
 * </pre>
 * Observe that {@code "h"} is the callback for the async function {@code sendMail}.
 * <p>
 * Callbacks that should be executed as fibers can be wrapped with {@link com.hiddenswitch.spellsource.net.impl.Sync#fiber(co.paralleluniverse.strands.SuspendableAction1)}.
 * <p>
 * To easily access the Spellsource database, use {@link com.hiddenswitch.spellsource.net.impl.Mongo#mongo()} in a fiber:
 * <pre>
 *   {@code
 *     import static com.hiddenswitch.spellsource.util.Mongo.mongo;
 *
 *     Record record = new Record();
 *     mongo().insert("collection", JsonObject.mapFrom(record));
 *     mongo().update("collection", json("_id", "12345"), json("$set", json("value", 1)));
 *   }
 * </pre>
 * Observe the use of {@code json(...)} and {@code mapFrom}.
 * <p>
 * To add a REST method, visit {@link com.hiddenswitch.spellsource.net.Gateway} and read the documentation there.
 * <p>
 * To add a method available to the web socket channel, visit {@link com.hiddenswitch.spellsource.net.Connection} and read
 * the documentation there.
 *
 * @see com.hiddenswitch.spellsource.net.Games for the service that creates games and converts internal game data into a
 * 		format compatible with the Unity3D client.
 */
package com.hiddenswitch.spellsource.net;