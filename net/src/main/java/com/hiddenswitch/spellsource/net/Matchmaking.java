package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableLock;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.*;
import com.hiddenswitch.spellsource.net.models.ConfigurationRequest;
import com.hiddenswitch.spellsource.net.models.MatchmakingRequest;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.streams.WriteStream;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.defer;
import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.getContextScheduler;

/**
 * The matchmaking service is the primary entry point into ranked games for clients.
 */
public interface Matchmaking extends Verticle {
	Logger LOGGER = LoggerFactory.getLogger(Matchmaking.class);

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
			if (Games.getUsersInGames().containsKey(userId)) {
				throw new IllegalStateException("User is already in a game");
			}

			var currentQueue = getUsersInQueues();
			var alreadyQueued = currentQueue.putIfAbsent(userId, request.getQueueId()) != null;
			if (alreadyQueued) {
				throw new IllegalStateException("User is already enqueued in a different queue.");
			}

			var eventBus = Vertx.currentContext().owner().eventBus();
			Message<Buffer> res = awaitResult(h -> eventBus.request(getQueueAddress(request.getQueueId()), new MatchmakingQueueEntry()
					.setCommand(MatchmakingQueueEntry.Command.ENQUEUE)
					.setUserId(request.getUserId())
					.setRequest(request), h));

			if (!res.body().toString().equals("OK")) {
				throw new AssertionError(String.format("probably queue missing, invalid response from queueId=%s", request.getQueueId()));
			}

			LOGGER.trace("enqueue {}: Successfully enqueued", request.getUserId());

			Presence.notifyFriendsOfPresence(new UserId(request.getUserId()), PresenceEnum.IN_GAME);
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
	static SuspendableMap<UserId, String> getUsersInQueues() {
		return SuspendableMap.getOrCreate("Matchmaking/currentQueue");
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
			var queueId = currentQueue.remove(userId);
			if (queueId != null) {
				var eventBus = Vertx.currentContext().owner().eventBus();
				eventBus.send(getQueueAddress(queueId), new MatchmakingQueueEntry()
						.setCommand(MatchmakingQueueEntry.Command.CANCEL)
						.setUserId(userId.toString()));
				Presence.updatePresence(userId.toString());
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
		var constructed = startMatchmaker("constructed", new MatchmakingQueueConfiguration()
				.setBotOpponent(false)
				.setLobbySize(2)
				.setName("Constructed")
				.setOnce(false)
				.setPrivateLobby(false)
				.setRanked(true)
				.setRules(new CardDesc[0])
				.setStillConnectedTimeout(1000L)
				.setStartsAutomatically(true));

		var quickPlay = startMatchmaker("quickPlay", new MatchmakingQueueConfiguration()
				.setBotOpponent(true)
				.setLobbySize(1)
				.setName("Quick Play")
				.setOnce(false)
				.setPrivateLobby(false)
				.setRanked(false)
				.setRules(new CardDesc[0])
				.setStillConnectedTimeout(4000L)
				.setStartsAutomatically(true));

		return (completionHandler -> constructed.close(v1 -> quickPlay.close(completionHandler)));
	}

	/**
	 * Starts a matchmaking queue.
	 * <p>
	 * Queues can be joined by players using {@link Matchmaking#enqueue(MatchmakingRequest)}.
	 *
	 * @param queueId
	 * @param queueConfiguration
	 * @return
	 * @throws SuspendExecution
	 */
	@Suspendable
	static Closeable startMatchmaker(String queueId, MatchmakingQueueConfiguration queueConfiguration) throws SuspendExecution {
		if (Vertx.currentContext() == null) {
			throw new IllegalStateException("must run on context");
		}

		var awaitReady = new CountDownLatch(1);
		var thisFiber = new AtomicReference<Fiber<Void>>(null);
		// Use an async lock so that timing out doesn't throw an exception
		// There should only be one matchmaker per queue per cluster. The lock here will make this invocation
		Vertx.currentContext().owner().sharedData().getLockWithTimeout("Matchmaking/queues/" + queueId, getTimeout(), res -> {
			if (res.failed()) {
				// Someone already has the lock
				awaitReady.countDown();
				return;
			}

			LOGGER.trace("startMatchmaker {}: Started", queueId);

			var lock = res.result();
			thisFiber.set(new Fiber<>("Matchmaking/queues/" + queueId, getContextScheduler(), () -> {
				long gamesCreated = 0;


				var vertx = Vertx.currentContext().owner();
				var eventBus = vertx.eventBus();
				var tracer = GlobalTracer.get();
				var consumer = eventBus.<MatchmakingQueueEntry>consumer(getQueueAddress(queueId));
				var adaptor = io.vertx.ext.sync.Sync.<Message<MatchmakingQueueEntry>>streamAdaptor();
				try {
					consumer.handler(adaptor);
					Void registered = awaitResult(consumer::completionHandler);
					var userToQueue = getUsersInQueues();

					// Dequeue requests
					do {
						if (Strand.currentStrand().isInterrupted()) {
							return null;
						}
						var span = tracer.buildSpan("Matchmaking/startMatchmaker/loop").start();
						span.setTag("queueId", queueId);
						span.log(json(queueConfiguration).getMap());
						try (var s2 = tracer.activateSpan(span)) {
							List<MatchmakingRequest> thisMatchRequests = new ArrayList<>();
							awaitReady.countDown();

							while (thisMatchRequests.size() < queueConfiguration.getLobbySize()) {
								Message<MatchmakingQueueEntry> request;
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

								if (request == null) {
									span.log("timeout");
									// The request timed out.
									// Remove any awaiting users, then break
									for (var existingRequest : thisMatchRequests) {
										userToQueue.remove(new UserId(existingRequest.getUserId()));
										var connection = Connection.writeStream(existingRequest.getUserId());
										// Notify the user they were dequeued
										connection.write(new Envelope().result(new EnvelopeResult().dequeue(new DefaultMethodResponse())));
									}
									// queue.destroy() is dealt with outside of here
									break;
								}

								switch (request.body().getCommand()) {
									case ENQUEUE:
										thisMatchRequests.add(request.body().getRequest());
										break;
									case CANCEL:
										thisMatchRequests.removeIf(existingReq -> existingReq.getUserId().equals(request.body().getUserId()));
										userToQueue.remove(new UserId(request.body().getUserId()));
										break;
								}

								request.reply(Buffer.buffer("OK"));
							}

							var gameId = GameId.create();
							span.setBaggageItem("gameId", gameId.toString());
							span.setTag("gameId", gameId.toString());

							// We've successfully dequeued, we can defer
							/*Fiber<Void> createGame = */
							defer(v -> {
								var gameCreateSpan = tracer
										.buildSpan("Matchmaking/startMatchmaker/createGame")
										.start();
								try (var s3 = tracer.activateSpan(gameCreateSpan)) {
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
										return;
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

										for (var innerConnection : new WriteStream[]{Connection.writeStream(user1.getUserId()), Connection.writeStream(user2.getUserId())}) {
											@SuppressWarnings("unchecked")
											var connection = (WriteStream<Envelope>) innerConnection;
											connection.write(gameReadyMessage());
										}
									}
								} catch (RuntimeException runtimeException) {
									Tracing.error(runtimeException, gameCreateSpan, true);
									throw runtimeException;
								} finally {
									for (var request : thisMatchRequests) {
										userToQueue.remove(new UserId(request.getUserId()));
									}
									gameCreateSpan.finish();
								}
							});
							//createGame.setUncaughtExceptionHandler((f, e) -> Vertx.currentContext().exceptionHandler().handle(e));
							gamesCreated++;
						} catch (RuntimeException runtimeException) {
							Tracing.error(runtimeException, span, true);
							throw runtimeException;
						} finally {
							span.setTag("gamesCreated", gamesCreated);
							span.finish();
						}
					} while (/*Queues that run once are typically private games*/!queueConfiguration.isOnce());
				} finally {
					if (lock != null) {
						lock.release();
					}

					if (consumer != null) {
						Void t = awaitResult(consumer::unregister);
					}
				}
				return null;
			}));

			thisFiber.get().setUncaughtExceptionHandler((f, e) -> Vertx.currentContext().exceptionHandler().handle(e));

			// We don't join on the fiber (we don't wait until the queue has actually started), we return immediately.
			thisFiber.get().start();
		});

		if (queueConfiguration.isJoin()) {
			try {
				awaitReady.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
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

			completionHandler.handle(Future.succeededFuture());
		};

		if (queueConfiguration.isAutomaticallyClose()) {
			Vertx.currentContext().addCloseHook(closeable);
		}

		return closeable;
	}

	@NotNull
	static String getQueueAddress(String queueId) {
		return "Matchmaking/queues/" + queueId;
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
		Connection.connected((connection, fut) -> {
			LOGGER.trace("handleConnections {}: Matchmaking ready", connection.userId());
			// If the user disconnects, dequeue them immediately.
			connection.endHandler(Sync.fiber(v -> {
				dequeue(new UserId(connection.userId()));
			}));

			connection.handler(Sync.fiber(msg -> {
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
