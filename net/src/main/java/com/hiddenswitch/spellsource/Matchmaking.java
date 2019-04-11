package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction1;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import co.paralleluniverse.strands.concurrent.CyclicBarrier;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.concurrent.*;
import com.hiddenswitch.spellsource.impl.DeckId;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.streams.WriteStream;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.util.Sync.defer;
import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitEvent;
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
		LOGGER.trace("enqueue {}: Enqueueing {}", request.getUserId(), request);
		// Check if the user is already in a game
		UserId userId = new UserId(request.getUserId());
		if (Games.getUsersInGames().containsKey(userId)) {
			throw new IllegalStateException("User is already in a game");
		}

		SuspendableMap<UserId, String> currentQueue = getUsersInQueues();
		boolean alreadyQueued = currentQueue.putIfAbsent(userId, request.getQueueId()) != null;
		if (alreadyQueued) {
			throw new IllegalStateException("User is already enqueued in a different queue.");
		}

		SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(request.getQueueId());
		if (!queue.offer(new MatchmakingQueueEntry()
				.setCommand(MatchmakingQueueEntry.Command.ENQUEUE)
				.setUserId(request.getUserId())
				.setRequest(request), false)) {
			throw new NullPointerException(String.format("queueId=%s not found", request.getQueueId()));
		}
		LOGGER.trace("enqueue {}: Successfully enqueued", request.getUserId());

		Presence.updatePresence(new UserId(request.getUserId()), PresenceEnum.IN_GAME);
		return true;
	}

	/**
	 * Records which queue a user is currently occupying.
	 *
	 * @return
	 */
	@NotNull
	@Suspendable
	static SuspendableMap<UserId, String> getUsersInQueues() {
		return SuspendableMap.getOrCreate("Matchmaking::currentQueue");
	}

	@Suspendable
	static void dequeue(UserId userId) throws SuspendExecution {
		SuspendableMap<UserId, String> currentQueue = getUsersInQueues();
		String queueId = currentQueue.remove(userId);
		if (queueId != null) {
			SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(queueId);
			queue.offer(new MatchmakingQueueEntry()
					.setCommand(MatchmakingQueueEntry.Command.CANCEL)
					.setUserId(userId.toString()), false);
			Presence.updatePresence(userId.toString());
			LOGGER.trace("dequeue {}: Successfully dequeued", userId);
		} else {
			LOGGER.trace("dequeue {}: User was not enqueued", userId);
		}
	}

	@Suspendable
	static Closeable startDefaultQueues() throws SuspendExecution {
		Closeable constructed = startMatchmaker("constructed", new MatchmakingQueueConfiguration()
				.setBotOpponent(false)
				.setLobbySize(2)
				.setName("Constructed")
				.setOnce(false)
				.setPrivateLobby(false)
				.setRanked(true)
				.setRules(new CardDesc[0])
				.setStillConnectedTimeout(1000L)
				.setStartsAutomatically(true));

		Closeable quickPlay = startMatchmaker("quickPlay", new MatchmakingQueueConfiguration()
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

	@Suspendable
	static Closeable startMatchmaker(String queueId, MatchmakingQueueConfiguration queueConfiguration) throws SuspendExecution {
		CountDownLatch awaitReady = new CountDownLatch(1);
		Fiber<Void> fiber = new Fiber<>("Matchmaking::queues[" + queueId + "]", getContextScheduler(), () -> {
			// There should only be one matchmaker per queue per cluster. The lock here will make this invocation
			SuspendableLock lock = null;
			SuspendableQueue<MatchmakingQueueEntry> queue = null;
			try {
				lock = SuspendableLock.lock("Matchmaking::queues[" + queueId + "]");
				LOGGER.info("startMatchmaker {}: Hazelcast node ID={} is running this queue", queueId, Hazelcast.getClusterManager().getNodeID());
				queue = SuspendableQueue.get(queueId);
				SuspendableMap<UserId, String> userToQueue = getUsersInQueues();

				// Dequeue requests
				do {
					List<MatchmakingRequest> thisMatchRequests = new ArrayList<>();
					LOGGER.trace("startMatchmaker {}: Awaiting {} users", queueId, queueConfiguration.getLobbySize());
					awaitReady.countDown();

					while (thisMatchRequests.size() < queueConfiguration.getLobbySize()) {
						MatchmakingQueueEntry request;
						if (queueConfiguration.getEmptyLobbyTimeout() > 0L && thisMatchRequests.isEmpty()) {
							LOGGER.debug("startMatchmaker {}: Polling with empty lobby", queueId);
							request = queue.poll(queueConfiguration.getEmptyLobbyTimeout());
						} else if (queueConfiguration.getAwaitingLobbyTimeout() > 0L && !thisMatchRequests.isEmpty()) {
							LOGGER.debug("startMatchmaker {}: Polling with awaiting lobby", queueId);
							request = queue.poll(queueConfiguration.getAwaitingLobbyTimeout());
						} else {
							LOGGER.debug("startMatchmaker {}: Taking, have {}", queueId, thisMatchRequests.size());
							request = queue.take();
						}

						if (request == null) {
							LOGGER.debug("startMatchmaker {}: Queue timed out", queueId);
							// The request timed out.
							// Remove any awaiting users, then break
							for (MatchmakingRequest existingRequest : thisMatchRequests) {
								userToQueue.remove(new UserId(existingRequest.getUserId()));
								WriteStream<Envelope> connection = Connection.writeStream(existingRequest.getUserId());
								// Notify the user they were dequeued
								connection.write(new Envelope().result(new EnvelopeResult().dequeue(new DefaultMethodResponse())));
							}
							// queue.destroy() is dealt with outside of here
							break;
						}

						switch (request.getCommand()) {
							case ENQUEUE:
								thisMatchRequests.add(request.getRequest());
								LOGGER.trace("startMatchmaker {}: Queued {}", queueId, request.getUserId());
								break;
							case CANCEL:
								thisMatchRequests.removeIf(existingReq -> existingReq.getUserId().equals(request.getUserId()));
								userToQueue.remove(new UserId(request.getUserId()));
								LOGGER.trace("startMatchmaker {}: Dequeued {}", queueId, request.getUserId());
								break;
						}
					}

					// We've successfully dequeued, we can defer
					defer(v -> {
						GameId gameId = GameId.create();

						// Is this a bot game?
						if (queueConfiguration.isBotOpponent()) {
							// Actually creating the game can happen without joining
							// Create a bot game.
							MatchmakingRequest user = thisMatchRequests.get(0);
							SuspendableLock botLock = SuspendableLock.noOpLock();

							try {
								// TODO: Move this lock into pollBotId
								// The player has been waiting too long. Match to an AI.
								// Retrieve a bot and use it to play against the opponent
								UserRecord bot = Accounts.get(Bots.pollBotId());

								DeckId botDeckId = user.getBotDeckId() == null
										? new DeckId(Bots.getRandomDeck(bot))
										: new DeckId(user.getBotDeckId());

								Games.createGame(ConfigurationRequest.botMatch(
										gameId,
										new UserId(user.getUserId()),
										new UserId(bot.getId()),
										new DeckId(user.getDeckId()),
										botDeckId));
							} finally {
								botLock.release();
							}

							WriteStream<Envelope> connection = Connection.writeStream(user.getUserId());

							connection.write(gameReadyMessage());

							userToQueue.remove(new UserId(user.getUserId()));
							return;
						}

						// Create a game for every pair
						try {
							if (thisMatchRequests.size() % 2 != 0) {
								throw new AssertionError("thisMatchRequests.size()");
							}

							LOGGER.trace("startMatchmaker {}: Creating game", queueId);
							for (int i = 0; i < thisMatchRequests.size(); i += 2) {
								MatchmakingRequest user1 = thisMatchRequests.get(i);
								MatchmakingRequest user2 = thisMatchRequests.get(i + 1);

								// This is a standard two player competitive match
								ConfigurationRequest request =
										ConfigurationRequest.versusMatch(gameId,
												new UserId(user1.getUserId()),
												new DeckId(user1.getDeckId()),
												new UserId(user2.getUserId()),
												new DeckId(user2.getDeckId()));
								Games.createGame(request);

								LOGGER.trace("startMatchmaker {}: Created game for {} and {}", queueId, user1.getUserId(), user2.getUserId());

								for (WriteStream innerConnection : new WriteStream[]{Connection.writeStream(user1.getUserId()), Connection.writeStream(user2.getUserId())}) {
									@SuppressWarnings("unchecked")
									WriteStream<Envelope> connection = (WriteStream<Envelope>) innerConnection;
									connection.write(gameReadyMessage());
								}
							}
						} finally {
							for (MatchmakingRequest request : thisMatchRequests) {
								userToQueue.remove(new UserId(request.getUserId()));
							}
						}

					});
				} while (/*Queues that run once are typically private games*/!queueConfiguration.isOnce());
			} catch (VertxException | InterruptedException ex) {
				// Cancelled
			} finally {
				if (lock != null) {
					lock.release();
				}

				// Private lobby locks should be destroyed once they reach here
				if (lock != null && queueConfiguration.isPrivateLobby()) {
					lock.destroy();
				}

				if (queue != null) {
					queue.destroy();
				}
			}
			return null;
		});

		// We don't join on the fiber (we don't wait until the queue has actually started), we return immediately.
		fiber.start();

		AtomicReference<Fiber<Void>> thisFiber = new AtomicReference<>(fiber);
		if (queueConfiguration.isJoin()) {
			try {
				awaitReady.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return completionHandler -> {
			// Don't interrupt twice if something else makes this fiber end early.
			if (thisFiber.get().isAlive() && thisFiber.get().isInterrupted()) {
				thisFiber.get().interrupt();
			}

			completionHandler.handle(Future.succeededFuture());
		};
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

	static void handleConnections() {
		Connection.connected((connection, fut) -> {
			LOGGER.trace("handleConnections {}: Matchmaking ready", connection.userId());
			// If the user disconnects, dequeue them immediately.
			connection.endHandler(suspendableHandler(v -> {
				dequeue(new UserId(connection.userId()));
			}));

			connection.handler(suspendableHandler(msg -> {
				EnvelopeMethod method = msg.getMethod();

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
