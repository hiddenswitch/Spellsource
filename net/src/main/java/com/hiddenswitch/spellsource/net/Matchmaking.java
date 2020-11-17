package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.Tracing;
import io.vertx.ext.sync.concurrent.SuspendableLock;
import io.vertx.ext.sync.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.*;
import com.hiddenswitch.spellsource.net.models.ConfigurationRequest;
import com.hiddenswitch.spellsource.net.models.MatchmakingRequest;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.shareddata.AsyncMap;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.fiber;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.getContextScheduler;

/**
 * The matchmaking service is the primary entry point into ranked games for clients.
 */
public interface Matchmaking {
	Logger LOGGER = LoggerFactory.getLogger(Matchmaking.class);
	String CONSTRUCTED = "constructed";
	String QUICK_PLAY = "quickPlay";

	/**
	 * Enqueues the user with the specified request.
	 *
	 * @param request The matchmaking request
	 * @return {@code true} if the user had successfully enqueued.
	 */
	@Suspendable
	static boolean enqueue(MatchmakingRequest request) throws SuspendExecution, NullPointerException, IllegalStateException {
		var span = GlobalTracer.get().buildSpan("Matchmaking/enqueue").start();
		var enqueued = false;
		try (var scope = GlobalTracer.get().activateSpan(span)) {
			span.setTag("userId", request.getUserId())
					.setTag("deckId", request.getDeckId())
					.setTag("queueId", request.getQueueId());

			// Check if the user is already in a game
			var userId = new UserId(request.getUserId());
			if (Games.isInGame(userId)) {
				throw new IllegalStateException("User is already in a game");
			}

			var currentQueue = getUsersInQueues();
			var alreadyQueued = currentQueue.putIfAbsent(request.getUserId(), request.getQueueId()) != null;
			if (alreadyQueued) {
				throw new IllegalStateException("User is already enqueued in a different queue.");
			}

			var eventBus = Vertx.currentContext().owner().eventBus();
			Message<Buffer> res = io.vertx.ext.sync.Sync.await(h -> eventBus.request(getQueueAddress(request.getQueueId()), new MatchmakingQueueEntry()
					.setCommand(MatchmakingQueueEntry.Command.ENQUEUE)
					.setUserId(request.getUserId())
					.setRequest(request), h));

			if (!res.body().toString().equals("OK")) {
				throw new AssertionError(String.format("probably queue missing, invalid response from queueId=%s", request.getQueueId()));
			}

			LOGGER.trace("enqueue {}: Successfully enqueued", request.getUserId());

			Presence.notifyFriendsOfPresence(new UserId(request.getUserId()));
			enqueued = true;
		} catch (Throwable t) {
			Tracing.error(t, span, false);
		} finally {
			span.setTag("enqueued", enqueued);
			span.finish();
		}
		return enqueued;
	}

	/**
	 * Records which queue a user is currently occupying.
	 *
	 * @return
	 */
	@NotNull
	@Suspendable
	static SuspendableMap<String, String> getUsersInQueues() {
		return SuspendableMap.getOrCreate("Matchmaking.currentQueue");
	}

	@NotNull
	static void getUsersInQueues(Handler<AsyncResult<AsyncMap<String, String>>> handler) {
		SuspendableMap.<String, String>getOrCreate("Matchmaking.currentQueue", handler);
	}

	/**
	 * Removes the specified user from whichever queue it is in.
	 *
	 * @param userId
	 * @throws SuspendExecution
	 */
	@Suspendable
	static void dequeue(UserId userId) throws SuspendExecution {
		var span = GlobalTracer.get().buildSpan("Matchmaking/dequeue").start();
		var dequeued = false;
		try (var s = GlobalTracer.get().activateSpan(span)) {
			span.setTag("userId", userId.toString());
			var currentQueue = getUsersInQueues();
			var queueId = currentQueue.remove(userId.toString());
			if (queueId != null) {
				var eventBus = Vertx.currentContext().owner().eventBus();
				eventBus.send(getQueueAddress(queueId), new MatchmakingQueueEntry()
						.setCommand(MatchmakingQueueEntry.Command.CANCEL)
						.setUserId(userId.toString()));
				dequeued = true;
			}
		} finally {
			span.setTag("dequeued", dequeued);
			span.finish();
		}
	}

	/**
	 * The default queues include the constructed and quick play (i.e. bot) queues.
	 *
	 * @return
	 * @throws SuspendExecution
	 */
	@Suspendable
	static Closeable startDefaultQueues() throws SuspendExecution {
		var constructed = startMatchmaker(CONSTRUCTED, new MatchmakingQueueConfiguration()
				.setBotOpponent(false)
				.setLobbySize(2)
				.setName("Constructed")
				.setOnce(false)
				.setPrivateLobby(false)
				.setRanked(true)
				.setRules(new CardDesc[0])
				.setStillConnectedTimeout(1000L)
				.setStartsAutomatically(true));

		var quickPlay = startMatchmaker(QUICK_PLAY, new MatchmakingQueueConfiguration()
				.setBotOpponent(true)
				.setLobbySize(1)
				.setName("Quick Play")
				.setOnce(false)
				.setPrivateLobby(false)
				.setRanked(false)
				.setRules(new CardDesc[0])
				.setStillConnectedTimeout(4000L)
				.setStartsAutomatically(true));

		return (completionHandler -> {
			var p = Promise.<Void>promise();
			constructed.close(p);
			p.future().onComplete(v -> {
				quickPlay.close(completionHandler);
			});
		});
	}

	/**
	 * Starts a matchmaking queue.
	 * <p>
	 * Queues can be joined by players using {@link Matchmaking#enqueue(MatchmakingRequest)}.
	 * <p>
	 * For a given queue ID, there shall only be one matchmaking queue. Multiple attempts to start a queue with the same
	 * ID will not fail; instead, they will quietly be daemonized as "secondaries" and only one will be promoted to
	 * "primary" if the existing primary ends for any exceptional reason, like a shutdown or a crash.  If the computer
	 * running the matchmaking queue shuts down, the queue will be restarted on a different computer. In a non-clustered
	 * environment this behaviour is ignored.
	 *
	 * @param queueId            The name of the queue to start.
	 * @param queueConfiguration The configuration of this queue.
	 * @return A method to close the queue.
	 * @throws SuspendExecution
	 */
	@Suspendable
	static Closeable startMatchmaker(String queueId, MatchmakingQueueConfiguration queueConfiguration) throws SuspendExecution {
		var context = Vertx.currentContext();
		var tracer1 = GlobalTracer.get();
		var span1 = tracer1.buildSpan("Matchmaking/startMatchmaker")
				.withTag("queueId", queueId)
				.start();

		try (var s3 = tracer1.activateSpan(span1)) {
			if (context == null) {
				throw new IllegalStateException("must run on context");
			}

			var awaitReady = new CountDownLatch(1);
			var thisMatchRequests = new ArrayList<MatchmakingRequest>();
			var thisFiber = new AtomicReference<Fiber<Void>>(null);

			thisFiber.set(new Fiber<>("Matchmaking.queues." + queueId, getContextScheduler(context), () -> {
				var userToQueue = getUsersInQueues();

				SuspendableLock lock = null;
				MessageConsumer<MatchmakingQueueEntry> consumer = null;
				try {
					while (!Strand.currentStrand().isInterrupted()) {
						// Use an async lock so that timing out doesn't throw an exception
						// There should only be one matchmaker per queue per cluster. The lock here will make this invocation
						lock = SuspendableLock.lock("Matchmaking.queues.locks." + queueId, Long.MAX_VALUE);

						long gamesCreated = 0;

						var vertx = context.owner();
						var eventBus = vertx.eventBus();
						var tracer = GlobalTracer.get();

						consumer = eventBus.consumer(getQueueAddress(queueId));
						var adaptor = io.vertx.ext.sync.Sync.<Message<MatchmakingQueueEntry>>streamAdaptor();
						consumer.handler(adaptor);
						io.vertx.ext.sync.Sync.<Void>await(consumer::completionHandler);

						// Dequeue requests
						do {
							if (Strand.currentStrand().isInterrupted()) {
								return null;
							}
							var span = tracer.buildSpan("Matchmaking/startMatchmaker/loop").start();
							span.setTag("queueId", queueId);
							span.log(json(queueConfiguration).getMap());
							try (var s2 = tracer.activateSpan(span)) {
								thisMatchRequests.clear();
								awaitReady.countDown();
								while (thisMatchRequests.size() < queueConfiguration.getLobbySize()) {
									Message<MatchmakingQueueEntry> request;
									try {
										if (queueConfiguration.getEmptyLobbyTimeout() > 0L && thisMatchRequests.isEmpty()) {
											span.log(ImmutableMap.of("thisMatchRequests.size", thisMatchRequests.size()));
											request = adaptor.receive(queueConfiguration.getEmptyLobbyTimeout());
										} else if (queueConfiguration.getAwaitingLobbyTimeout() > 0L && !thisMatchRequests.isEmpty()) {
											span.log(ImmutableMap.of("thisMatchRequests.size", thisMatchRequests.size()));
											request = adaptor.receive(queueConfiguration.getAwaitingLobbyTimeout());
										} else {
											span.log(ImmutableMap.of("thisMatchRequests.size", thisMatchRequests.size()));
											request = adaptor.receive();
										}
									} catch (Throwable ex) {
										request = null;
										// Interrupted
										var cause = Throwables.getRootCause(ex);
										if (cause instanceof InterruptedException) {
											// will be cleaned up by shutdown hook
											return null;
										} else if (!(cause instanceof TimeoutException)) {
											throw ex;
										}
									}

									if (request == null) {
										span.log("timeout");

										// queue.destroy() is dealt with outside of here
										if (queueConfiguration.isOnce()) {
											return null;
										} else {
											continue;
										}
									}

									switch (request.body().getCommand()) {
										case ENQUEUE:
											thisMatchRequests.add(request.body().getRequest());
											break;
										case CANCEL:
											var finalRequest = request;
											thisMatchRequests.removeIf(existingReq -> existingReq.getUserId().equals(finalRequest.body().getUserId()));
											userToQueue.remove(request.body().getUserId());
											break;
									}

									request.reply(Buffer.buffer("OK"));
								}

								var gameId = GameId.create();
								span.setBaggageItem("gameId", gameId.toString());
								span.setTag("gameId", gameId.toString());

								// We've successfully dequeued
								var gameCreateSpan = tracer
										.buildSpan("Matchmaking/startMatchmaker/createGame")
										.start();
								try (var s4 = tracer.activateSpan(gameCreateSpan)) {
									gameCreateSpan.setTag("gameId", gameId.toString());

									// Is this a bot game?
									if (queueConfiguration.isBotOpponent()) {
										// Actually creating the game can happen without joining
										// Create a bot game.
										var user = thisMatchRequests.get(0);
										var botLock = SuspendableLock.noOpLock();

										try {
											// TODO: Move this lock into pollBotId
											// The player has been waiting too long. Match to an AI.
											// Retrieve a bot and use it to play against the opponent
											var bot = Accounts.get(Bots.pollBotId().toString());

											var botDeckId = user.getBotDeckId() == null
													? new DeckId(Bots.getRandomDeck(bot))
													: new DeckId(user.getBotDeckId());

											Games.createGame(ConfigurationRequest.botMatch(
													gameId,
													new UserId(user.getUserId()),
													new UserId(bot.getId()),
													new DeckId(user.getDeckId()),
													botDeckId)
													.setSpanContext(gameCreateSpan.context()));
										} finally {
											botLock.release();
										}

										var connection = Connection.writeStream(user.getUserId());
										connection.write(gameReadyMessage());
										continue;
									}

									// Create a game for every pair
									if (thisMatchRequests.size() % 2 != 0) {
										throw new AssertionError(String.format("thisMatchRequests.size() was not divisible by two, it was %d", thisMatchRequests.size()));
									}

									for (var i = 0; i < thisMatchRequests.size(); i += 2) {
										var user1 = thisMatchRequests.get(i);
										var user2 = thisMatchRequests.get(i + 1);

										// This is a standard two player competitive match
										var request =
												ConfigurationRequest.versusMatch(gameId,
														new UserId(user1.getUserId()),
														new DeckId(user1.getDeckId()),
														new UserId(user2.getUserId()),
														new DeckId(user2.getDeckId()))
														.setSpanContext(gameCreateSpan.context());
										Games.createGame(request);

										LOGGER.trace("startMatchmaker {}: Created game for {} and {}", queueId, user1.getUserId(), user2.getUserId());

										for (var innerConnection : new MessageProducer[]{Connection.writeStream(user1.getUserId()), Connection.writeStream(user2.getUserId())}) {
											@SuppressWarnings("unchecked")
											var connection = (MessageProducer<Envelope>) innerConnection;
											connection.write(gameReadyMessage());
										}
									}
								} catch (RuntimeException runtimeException) {
									Tracing.error(runtimeException, gameCreateSpan, true);
									throw runtimeException;
								} finally {
									for (var request : thisMatchRequests) {
										userToQueue.remove(request.getUserId());
									}
									gameCreateSpan.finish();
								}
								gamesCreated++;
							} catch (RuntimeException runtimeException) {
								Tracing.error(runtimeException, span, true);
								throw runtimeException;
							} finally {
								span.setTag("gamesCreated", gamesCreated);
								span.finish();
							}
						} while (/*Queues that run once are typically private games*/!queueConfiguration.isOnce());

						if (queueConfiguration.isOnce()) {
							return null;
						}
					}
				} catch (RuntimeException ex) {
					var cause = Throwables.getRootCause(ex);
					if (cause instanceof InterruptedException) {
						return null;
					}
					throw ex;
				} finally {
					if (lock != null) {
						lock.release();
					}

					if (consumer != null) {
						// TODO: Explore how to change this into a blocking operation
						consumer.unregister();
					}
				}
				return null;
			}));

			thisFiber.get().setUncaughtExceptionHandler((f, e) -> context.exceptionHandler().handle(e));

			// We don't join on the fiber (we don't wait until the queue has actually started), we return immediately.
			context.runOnContext(v -> {
				thisFiber.get().start();
			});

			if (queueConfiguration.isJoin()) {
				try {
					if (!awaitReady.await(4000L, TimeUnit.MILLISECONDS)) {
						throw new TimeoutException();
					}

					span1.setTag("startedHere", true);
				} catch (Throwable e) {
					var cause = Throwables.getRootCause(e);
					if (cause instanceof TimeoutException) {
						// we didn't get this matchmaker, continue peacefully
						span1.setTag("startedHere", false);
					} else {
						// If we were interrupted we need to close
						Tracing.error(e, span1, false);
					}
				}
			}

			Closeable closeable = completionHandler -> {
				if (thisFiber.get() == null) {
					completionHandler.handle(Future.succeededFuture());
					return;
				}
				// Don't interrupt twice if something else makes this fiber end early.
				if (thisFiber.get().isAlive() && !thisFiber.get().isInterrupted()) {
					thisFiber.get().interrupt();
				}

				if (!thisMatchRequests.isEmpty()) {
					getUsersInQueues(res -> {
						if (res.succeeded()) {
							removeEnqueuedAndNotify(queueId, thisMatchRequests, res.result(), completionHandler);
						} else {
							completionHandler.handle(res.mapEmpty());
						}
					});
					return;
				}

				completionHandler.handle(Future.succeededFuture());
			};

			// We may have already been interrupted
			if (Strand.currentStrand().isInterrupted()) {
				closeable.close(Promise.promise());
			}

			return closeable;
		} finally {
			span1.finish();
		}

	}

	static void removeEnqueuedAndNotify(String queueId, List<MatchmakingRequest> thisMatchRequests, AsyncMap<String, String> userToQueue, Handler<AsyncResult<Void>> completionHandler) {
		for (var existingRequest : thisMatchRequests) {
			userToQueue.remove(existingRequest.getUserId(), res -> {
				if (res.succeeded()) {
					var connection = Connection.writeStream(existingRequest.getUserId());
					connection.write(new Envelope().result(new EnvelopeResult().dequeue(new DefaultMethodResponse())));
				}

				completionHandler.handle(Future.succeededFuture());
			});
		}
	}

	@NotNull
	static String getQueueAddress(String queueId) {
		return "Matchmaking.queues.handlers." + queueId;
	}

	static long getTimeout() {
		return 8000L;
	}

	/**
	 * Returns the message that indicates that a game is ready to play.
	 *
	 * @return
	 */
	static Envelope gameReadyMessage() {
		return new Envelope()
				.result(new EnvelopeResult()
						.enqueue(new MatchmakingQueuePutResponse()
								.unityConnection(new MatchmakingQueuePutResponseUnityConnection().firstMessage(new ClientToServerMessage()
										.messageType(MessageType.FIRST_MESSAGE)
										.firstMessage(new ClientToServerMessageFirstMessage())))));
	}

	/**
	 * Configures the {@link Connection} interface of web socket communications to accept enqueues, dequeues, and notify
	 * connected players of changes in queue status.
	 */
	static void handleConnections() {
		Connection.connected("Matchmaking/handleConnections", (connection, fut) -> {
			LOGGER.trace("handleConnections {}: Matchmaking ready", connection.userId());
			// If the user disconnects, dequeue them immediately.
			connection.addCloseHandler(fiber(v -> {
				dequeue(new UserId(connection.userId()));
				v.complete();
			}));

			connection.handler(fiber(msg -> {
				var method = msg.getMethod();

				if (method != null) {
					if (method.getEnqueue() != null) {
						LOGGER.trace("handleConnections enqueue {}: Dequeuing", connection.userId());
						// Always dequeue the user first, silently succeeds regardless if they're currently enqueued.
						dequeue(new UserId(connection.userId()));
						LOGGER.trace("handleConnections enqueue {}: Enqueuing", connection.userId());
						enqueue(new MatchmakingRequest()
								.withUserId(connection.userId())
								.setQueueId(method.getEnqueue().getQueueId())
								.withDeckId(method.getEnqueue().getDeckId())
								.withBotDeckId(method.getEnqueue().getBotDeckId()));
						LOGGER.trace("handleConnections enqueue {}: Enqueued", connection.userId());
					}

					if (method.getDequeue() != null) {
						dequeue(new UserId(connection.userId()));

						connection.write(new Envelope()
								.result(new EnvelopeResult()
										.dequeue(new DefaultMethodResponse())));
					}
				}
			}));
			fut.handle(Future.succeededFuture());
		});
	}
}
