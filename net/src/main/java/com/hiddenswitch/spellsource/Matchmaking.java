package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.Fiber;
import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.concurrent.*;
import com.hiddenswitch.spellsource.impl.DeckId;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.*;
import io.vertx.core.streams.WriteStream;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
	 * Gets information about the current match the user is in. This is used for reconnecting.
	 *
	 * @param request The user's ID.
	 * @return The current match information. This may be a queue entry, the match, or nothing (but not null) if the user
	 * 		is not in a match.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException {
		GameId gameId = Games.getGames().get(new UserId(request.getUserId()));
		if (gameId != null) {
			return CurrentMatchResponse.response(gameId.toString());
		} else {
			return CurrentMatchResponse.response(null);
		}
	}

	/**
	 * Ends a match and allows the user to re-enter the queue.
	 * <p>
	 * The Games service, which also has an end game session function, is distinct from this one. This method allows the
	 * user to enter the queue again (typically users can only be in one queue at a time, either playing a game inside the
	 * matchmaking queue or waiting to be matched into a game). Typical users should not be able to play multiple games at
	 * once.
	 *
	 * @param request The user or game ID to exit from a match.
	 * @return Information about the expiration/cancellation requested.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static MatchExpireResponse expireOrEndMatch(MatchExpireRequest request) throws SuspendExecution, InterruptedException {
		// Not clear if this must be a global lock...
		SuspendableLock lock = SuspendableLock.lock("Matchmaking::expireOrEndMatch");

		try {
			LOGGER.debug("expireOrEndMatch: Expiring match {}", request.getGameId());

			if (request.getUsers() == null) {
				throw new NullPointerException("Request does not contain users specified");
			}

			if (request.getUsers().size() != 2) {
				throw new IllegalStateException("There should be two users in a match expire request.");
			}

			SuspendableMap<UserId, GameId> games = Games.getGames();
			for (UserId userId : request.getUsers()) {
				games.remove(userId);
			}

			if (LOGGER.isTraceEnabled()) {
				Collection<UserId> values = games.keySet();
				Collection<UserId> usersQueued = Matchmaking.userToQueue().keySet();
				LOGGER.debug("expireOrEndMatch: Users with games n={} {}", values.size(), values);
				LOGGER.debug("expireOrEndMatch: Users queued n={} {}", usersQueued.size(), usersQueued);
			}
		} finally {
			lock.release();
		}

		/*
		// End the game in alliance mode
		Logic.endGame(new EndGameRequest()
				.withPlayers(new EndGameRequest.Player()
								.withDeckId(decks.get(0).toString()),
						new EndGameRequest.Player()
								.withDeckId(decks.get(1).toString())));

		if (logger.isDebugEnabled()) {
			logger.debug("expireOrEndMatch: Called Logic::endGame");
		}
		*/

		return new MatchExpireResponse();
	}

	/**
	 * Enqueues the user with the specified request.
	 *
	 * @param request The matchmaking request
	 */
	@Suspendable
	static void enqueue(MatchmakingRequest request) throws SuspendExecution, NullPointerException, IllegalStateException {
		SuspendableLock lock = null;
		LOGGER.trace("enqueue {}: Enqueueing {}", request.getUserId(), request);
		try {
			lock = Connection.methodLock(request.getUserId());
			// Check if the user is already in a game
			UserId userId = new UserId(request.getUserId());
			if (Games.getGames().containsKey(userId)) {
				throw new IllegalStateException("User is already in a game");
			}

			SuspendableMap<UserId, String> currentQueue = userToQueue();
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
		} finally {
			if (lock != null) {
				lock.release();
			}
		}
	}

	/**
	 * Records which queue a user is currently occupying.
	 *
	 * @return
	 */
	@NotNull
	@Suspendable
	static SuspendableMap<UserId, String> userToQueue() {
		return SuspendableMap.getOrCreate("Matchmaking::currentQueue");
	}

	@Suspendable
	static void dequeue(UserId userId) throws SuspendExecution {
		SuspendableLock lock = Connection.methodLock(userId.toString());
		try {
			SuspendableMap<UserId, String> currentQueue = userToQueue();
			String queueId = currentQueue.remove(userId);
			if (queueId != null) {
				SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(queueId);
				queue.offer(new MatchmakingQueueEntry()
						.setCommand(MatchmakingQueueEntry.Command.CANCEL)
						.setUserId(userId.toString()), false);
				LOGGER.trace("dequeue {}: Successfully dequeued", userId);
			} else {
				LOGGER.trace("dequeue {}: User was not enqueued", userId);
			}
		} finally {
			lock.release();
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
		Fiber<Void> fiber = getContextScheduler().newFiber(() -> {
			// There should only be one matchmaker per queue per cluster
			SuspendableLock lock = null;
			SuspendableQueue<MatchmakingQueueEntry> queue = null;
			try {
				lock = SuspendableLock.lock("Matchmaking::queues[" + queueId + "]");
				queue = SuspendableQueue.get(queueId);
				SuspendableMap<UserId, String> userToQueue = userToQueue();

				List<MatchmakingRequest> thisMatchRequests = new ArrayList<>();

				// Dequeue requests
				do {
					LOGGER.trace("startMatchmaker {}: Awaiting {} users", queueId, queueConfiguration.getLobbySize());

					while (thisMatchRequests.size() < queueConfiguration.getLobbySize()) {
						MatchmakingQueueEntry request;
						if (queueConfiguration.getEmptyLobbyTimeout() > 0L && thisMatchRequests.isEmpty()) {
							request = queue.poll(queueConfiguration.getEmptyLobbyTimeout());
						} else if (queueConfiguration.getAwaitingLobbyTimeout() > 0L && !thisMatchRequests.isEmpty()) {
							request = queue.poll(queueConfiguration.getAwaitingLobbyTimeout());
						} else {
							request = queue.take();
						}

						if (request == null) {
							// The request timed out.
							// Remove any awaiting users, then break
							for (MatchmakingRequest existingRequest : thisMatchRequests) {
								userToQueue.remove(new UserId(existingRequest.getUserId()));
								WriteStream<Envelope> connection = Connection.writeStream(existingRequest.getUserId());
								if (connection != null) {
									// Notify the user they were dequeued
									connection.write(new Envelope().result(new EnvelopeResult().dequeue(new DefaultMethodResponse())));
								}
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

					GameId gameId = GameId.create();

					// Is this a bot game?
					if (queueConfiguration.isBotOpponent()) {
						// Actually creating the game can happen without joining
						// Create a bot game.
						MatchmakingRequest user = thisMatchRequests.get(0);
						SuspendableLock botLock = SuspendableLock.lock(Bots.TAKING_BOT_LOCK_NAME);

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

						if (connection != null) {
							connection.write(gameReadyMessage());
						}

						userToQueue.remove(new UserId(user.getUserId()));

						thisMatchRequests.clear();
						continue;
					}

					// Create a game for every pair
					if (thisMatchRequests.size() % 2 != 0) {
						throw new AssertionError("thisMatchRequests.size()");
					}

//					if (queueConfiguration.isStartsAutomatically())
					LOGGER.trace("startMatchmaker {}: Creating game", queueId);
					for (int i = 0; i < thisMatchRequests.size(); i += 2) {
						int thisIndex = i;
						MatchmakingRequest user1 = thisMatchRequests.get(thisIndex);
						MatchmakingRequest user2 = thisMatchRequests.get(thisIndex + 1);

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
							if (connection != null) {
								connection.write(gameReadyMessage());
							}
						}

						userToQueue.remove(new UserId(user1.getUserId()));
						userToQueue.remove(new UserId(user2.getUserId()));
					}

					thisMatchRequests.clear();
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

		fiber.start();

		AtomicReference<Fiber<Void>> thisFiber = new AtomicReference<>(fiber);
		return completionHandler -> {
			// Don't interrupt twice if something else makes this fiber end early.
			if (thisFiber.get().isAlive() && thisFiber.get().isInterrupted()) {
				thisFiber.get().interrupt();
			}

			completionHandler.handle(Future.succeededFuture());
		};
	}

	static Envelope gameReadyMessage() {
		return new Envelope()
				.result(new EnvelopeResult()
						.enqueue(new MatchmakingQueuePutResponse()
								.unityConnection(new MatchmakingQueuePutResponseUnityConnection().firstMessage(new ClientToServerMessage()
										.messageType(MessageType.FIRST_MESSAGE)
										.firstMessage(new ClientToServerMessageFirstMessage())))));
	}

	static void handleConnections() {
		Connection.connected(connection -> {
			// If the user disconnects, dequeue them immediately.
			connection.endHandler(suspendableHandler((SuspendableAction1<Void>) v -> {
				dequeue(new UserId(connection.userId()));
			}));

			connection.handler(suspendableHandler((SuspendableAction1<Envelope>) msg -> {
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
		});
	}
}
