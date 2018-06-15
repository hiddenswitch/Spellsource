package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.Semaphore;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.DeckId;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
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
	Map<UserId, Strand> LOCAL_STRANDS = new ConcurrentHashMap<>();
	Map<String, SuspendableQueue<QueueEntry>> QUEUES = new ConcurrentHashMap<>();
	Map<String, SuspendableSemaphore> SEMAPHORES = new ConcurrentHashMap<>();
	Semaphore QUEUES_LOCK = new Semaphore(1);
	Semaphore SEMAPHORES_LOCK = new Semaphore(1);

	/**
	 * Creates a bot game
	 *
	 * @param userId
	 * @param deckId
	 * @param botDeckId
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static GameId bot(UserId userId, DeckId deckId, DeckId botDeckId) throws SuspendExecution, InterruptedException {
		LOGGER.debug("bot: Matchmaker is creating an AI game for " + userId);
		Lock botLock = null;
		GameId gameId = GameId.create();

		try {
			// TODO: Move this lock into pollBotId
			botLock = SharedData.lock("Matchmaking::takingBot", 30000L);

			// The player has been waiting too long. Match to an AI.
			// Retrieve a bot and use it to play against the opponent
			UserRecord bot = Accounts.get(Bots.pollBotId());
			if (botDeckId != null) {
				LOGGER.info("bot: UserId {} requested bot to play deckId {}", userId, botDeckId);
			}

			if (botDeckId == null) {
				botDeckId = new DeckId(Bots.getRandomDeck(bot));
			}

			MatchCreateResponse matchCreateResponse =
					Matchmaking.createMatch(
							MatchCreateRequest.botMatch(gameId, userId, new UserId(bot.getId()), deckId, botDeckId));

			LOGGER.debug("bot: User " + userId + " is unlocked by the AI bot creation path.");
			botLock.release();
			return gameId;
		} finally {
			if (botLock != null) {
				botLock.release();
			}
		}
	}

	/**
	 * Creates a two-player match with the given settings.
	 *
	 * @param gameId
	 * @param userId
	 * @param deckId
	 * @param otherUserId
	 * @param otherDeckId
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static GameId vs(GameId gameId, UserId userId, DeckId deckId, UserId otherUserId, DeckId otherDeckId) throws SuspendExecution, InterruptedException {
		MatchCreateRequest request = new MatchCreateRequest()
				.withDeckId1(deckId)
				.withDeckId2(otherDeckId)
				.withUserId1(userId)
				.withUserId2(otherUserId)
				.withGameId(gameId);

		MatchCreateResponse createMatchResponse = createMatch(request);

		CreateGameSessionResponse createGameSessionResponse = createMatchResponse.getCreateGameSessionResponse();

		if (createGameSessionResponse.pending) {
			LOGGER.debug("vs: Retrying createMatch... ");
			int i = 0;
			final int retries = 4;
			final int retryDelay = 500;
			for (; i < retries; i++) {
				Strand.sleep(retryDelay);

				LOGGER.debug("vs: Checking if the Games service has created a game for gameId " + gameId);
				createGameSessionResponse = Games.getConnections().get(gameId);
				if (!createGameSessionResponse.pending) {
					break;
				}
			}

			if (i >= retries) {
				throw new NullPointerException("Timed out while waiting for a match to be created for users " + userId + " and " + otherUserId);
			}
		}

		LOGGER.debug("vs: Users " + userId + " and " + otherUserId + " are unlocked because a game has been successfully created for them with gameId " + gameId);
		return gameId;
	}

	/**
	 * Gets information about the current match the user is in. This is used for reconnecting.
	 *
	 * @param request The user's ID.
	 * @return The current match information. This may be a queue entry, the match, or nothing (but not null) if the user
	 * is not in a match.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException {
		LOGGER.debug("getCurrentMatch: Retrieving information for userId " + request.getUserId());
		GameId gameId = Games.getGames().get(new UserId(request.getUserId()));
		if (gameId != null) {
			LOGGER.debug("getCurrentMatch: User " + request.getUserId() + " has match " + gameId);
			return CurrentMatchResponse.response(gameId.toString());
		} else {
			LOGGER.debug("getCurrentMatch: User " + request.getUserId() + " does not have match.");
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("expireOrEndMatch: Expiring match " + request.gameId);
		}

		if (request.users == null) {
			throw new NullPointerException("Request does not contain users specified");
		}

		if (request.users.size() != 2) {
			throw new IllegalStateException("There should be two users in a match expire request.");
		}

		SuspendableMap<UserId, GameId> games = Games.getGames();
		for (UserId userId : request.users) {
			games.remove(userId);
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
	 * Creates a match without entering a queue entry between two users.
	 *
	 * @param request All the required information to create a game.
	 * @return Connection information for both users.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static MatchCreateResponse createMatch(MatchCreateRequest request) throws SuspendExecution, InterruptedException {
		Logic.triggers();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("createMatch: Creating match for request " + request.toString());
		}

		final String deckId1 = request.getDeckId1().toString();
		final String deckId2 = request.getDeckId2().toString();
		final String userId1 = request.getUserId1().toString();
		final String userId2 = request.getUserId2().toString();
		final String gameId = request.getGameId().toString();

		StartGameResponse startGameResponse = Logic.startGame(new StartGameRequest()
				.withGameId(gameId)
				.withPlayers(new StartGameRequest.Player()
								.withId(0)
								.withUserId(userId1)
								.withDeckId(deckId1),
						new StartGameRequest.Player()
								.withId(1)
								.withUserId(userId2)
								.withDeckId(deckId2)));

		final Deck deck1 = startGameResponse.getPlayers().get(0).getDeck();
		final Deck deck2 = startGameResponse.getPlayers().get(1).getDeck();

		final CreateGameSessionRequest createGameSessionRequest = new CreateGameSessionRequest()
				.withPregame1(new Configuration(deck1, userId1)
						.withAI(request.isBot1())
						.withAttributes(startGameResponse.getConfig1().getAttributes()))
				.withPregame2(new Configuration(deck2, userId2)
						.withAI(request.isBot2())
						.withAttributes(startGameResponse.getConfig2().getAttributes()))
				.withGameId(gameId);
		CreateGameSessionResponse createGameSessionResponse = Rpc.connect(Games.class).sync().createGameSession(createGameSessionRequest);
		Games.getGames().put(new UserId(userId1), new GameId(gameId));
		Games.getGames().put(new UserId(userId2), new GameId(gameId));
		return new MatchCreateResponse(createGameSessionResponse);
	}

	/**
	 * Retrieves the strands (fibers) that are running on this instance of the JVM.
	 *
	 * @return A map of strands.
	 */
	static Map<UserId, Strand> getLocalStrands() {
		return LOCAL_STRANDS;
	}

	/**
	 * Retrieves a reference to a suspendable queue.
	 *
	 * @param queueId The kind of queue (e.g., {@code "constructed"}
	 * @param key     The component of the queue (e.g., the {@code "first user"} queue.
	 * @return A suspendable queue reference.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static SuspendableQueue<QueueEntry> getQueue(String queueId, String key) throws SuspendExecution, InterruptedException {
		String queueKey = key + "[" + queueId + "]";
		try {
			QUEUES_LOCK.acquire();
			if (QUEUES.containsKey(queueKey)) {
				return QUEUES.get(queueKey);
			} else {
				SuspendableQueue<QueueEntry> v = SuspendableQueue.get(queueKey, 1);
				QUEUES.put(queueKey, v);
				return v;
			}
		} finally {
			QUEUES_LOCK.release();
		}

	}

	static SuspendableSemaphore getParty(String queueId) throws SuspendExecution, InterruptedException {
		String key = "Matchmaking::party[" + queueId + "]";
		try {
			SEMAPHORES_LOCK.acquire();
			if (SEMAPHORES.containsKey(key)) {
				return SEMAPHORES.get(key);
			} else {
				SuspendableSemaphore v = SuspendableSemaphore.create(key, 2);
				SEMAPHORES.put(key, v);
				return v;
			}
		} finally {
			SEMAPHORES_LOCK.release();
		}
	}

	/**
	 * Initializes a cancellation handler on this {@link Vertx} instance.
	 *
	 * @return A way to close this handler, if necessary.
	 */
	static Closeable cancellation() {
		EventBus eb = Vertx.currentContext().owner().eventBus();
		MessageConsumer<JsonObject> consumer = eb.<JsonObject>consumer("Matchmaking::cancel", suspendableHandler(message -> {
			UserId userId = new UserId(message.body().getString("userId"));
			if (getLocalStrands().containsKey(userId)) {
				getLocalStrands().get(userId).interrupt();
			}
		}));

		return consumer::unregister;
	}

	/**
	 * Cancels the user's matchmaking, wherever the user may be connected.
	 *
	 * @param userId The user whose matchmaking should be canceled.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static void cancel(UserId userId) throws SuspendExecution, InterruptedException {
		Vertx.currentContext().owner().eventBus().publish("Matchmaking::cancel", new JsonObject().put("userId", userId.toString()));
	}

	/**
	 * Awaits this user for matchmaking in the queue with {@link MatchmakingRequest#getQueueId()}.
	 * <p>
	 * Behaves like {@link java.util.concurrent.BlockingQueue#poll(long, TimeUnit)} but does <b>not</b> throw {@link
	 * InterruptedException} if the user cancels; instead, this method will block and return null for either a timeout or
	 * a cancellation.
	 *
	 * @param request The matchmaking request.
	 * @return The {@link GameId} of the matched game, or {@code null} if the user timed out waiting or canceled using
	 * {@link #cancel(UserId)}.
	 * @throws SuspendExecution
	 */
	@Nullable
	static GameId matchmake(@NotNull MatchmakingRequest request) throws SuspendExecution {
		Lock lock = null;
		final UserId userId = new UserId(request.getUserId());
		try {
			lock = SharedData.lock("Matchmaking::lock." + request.getUserId(), 350L);
			GameId gameId = Games.getGames().get(userId);
			if (gameId != null) {
				LOGGER.debug("matchmake: User {} already has a game {}", request.getUserId(), gameId);
				return gameId;
			}

			if (request.isBotMatch()) {
				LOGGER.debug("matchmake: User {} is getting a bot game", request.getUserId());
				return bot(userId, new DeckId(request.getDeckId()), request.getBotDeckId() == null ? null : new DeckId(request.getBotDeckId()));
			}


			long timeout = request.getTimeout();
			String queueId = request.getQueueId() == null ? "constructed" : request.getQueueId();

			Map<UserId, Strand> strands = getLocalStrands();
			if (strands.putIfAbsent(userId, Strand.currentStrand()) != null) {
				throw new ConcurrentModificationException();
			}

			SuspendableQueue<QueueEntry> firstUser = getQueue(queueId, "Matchmaking::firstUser");
			SuspendableQueue<QueueEntry> secondUser = getQueue(queueId, "Matchmaking::secondUser");
			SuspendableSemaphore party = getParty(queueId);
			long startTime = System.currentTimeMillis();
			try {
				// There are at most two users who can modify the queue
				if (party.tryAcquire(timeout)) {
					try {
						// Test if we are the first user
						GameId thisGameId = GameId.create();
						if (firstUser.offer(new QueueEntry(request, thisGameId), true)) {
							// One user is in the queue at this point (this user)
							while (true) {
								try {
									timeout -= System.currentTimeMillis() - startTime;
									startTime = System.currentTimeMillis();
									if (timeout < 0) {
										// We timed out for whatever reason
										LOGGER.debug("matchmaking: User {} retrying", userId);
										return null;
									}
									// Wait until the second user is added
									QueueEntry match = secondUser.poll(timeout);
									if (match == null) {
										// We timed out, which is as good as cancelling.
										throw new InterruptedException();
									} else {
										// A second user has released us and now we have our match
										// Second user creates game
										if (!match.gameId.equals(thisGameId)) {
											throw new AssertionError("Game IDs do not match.");
										}
										return match.gameId;
									}
								} catch (VertxException | InterruptedException canceled) {
									// We timed out or cancelled waiting for a second user. The semaphore and the queue semantics
									// ensure we're the user in firstUser, unless a second user has already gotten us
									if (firstUser.poll(350L) == null) {
										// A second user has already consumed us to start a match. We have to continue to wait
										// for the second user.
										continue;
									} else {
										// We successfully cancelled. The semaphore ensures that this user was the one that removed
										// itself from first user if a second user didn't.
										LOGGER.debug("matchmake: User {} cancelled matchmaking", userId);
										return null;
									}
								}

							}
						} else {
							// The queue has a user waiting in it. This means we are the second user. Retrieve the first
							// user. If we consume this first, the first user cannot possibly cancel the matchmaking. It
							// doesn't really matter if this poll is interruptible, but the exception either way (cancel or
							// interrupt) will lead us to the right places.
							try {
								QueueEntry match = firstUser.poll(timeout);
								if (match == null) {
									throw new InterruptedException();
								} else {
									vs(match.gameId,
											new UserId(match.req.getUserId()),
											new DeckId(match.req.getDeckId()),
											userId,
											new DeckId(request.getDeckId()));
									// Queue ourselves into the second user slot, to signal to the first user whom they're matching
									// with.
									if (!secondUser.offer(new QueueEntry(request, match.gameId), true)) {
										throw new AssertionError();
									}
									// At this point, the first user isn't permitted to cancel gracefully. The test for valid
									// matching will be kicked off to the match connection.
									LOGGER.debug("matchmake: Matching {} and {} into game {}", match.req.getUserId(), userId, match.gameId);
									return match.gameId;
								}
							} catch (VertxException | InterruptedException canceled) {
								LOGGER.debug("matchmake: User {} cancelled matchmaking", userId);
								return null;
							}
						}
					} finally {
						party.release();
					}
				} else {
					// Timed out
					return null;
				}
			} finally {
				lock.release();
				strands.remove(userId);
			}
		} catch (Throwable t) {
			Throwable inner = t;
			if (t instanceof VertxException) {
				inner = t.getCause();
			}
			if (inner instanceof TimeoutException) {
				LOGGER.debug("matchmake: User {} is already waiting on another invocation", userId);
			} else if (inner instanceof InterruptedException) {
				LOGGER.debug("matchmake: User {} cancelled matchmaking", userId);
			}

			return null;
		} finally {
			if (lock != null) {
				lock.release();
			}
		}
	}

	class QueueEntry implements Serializable {
		MatchmakingRequest req;
		GameId gameId;

		public QueueEntry(MatchmakingRequest req, GameId gameId) {
			this.req = req;
			this.gameId = gameId;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("userId", req.getUserId())
					.toString();
		}
	}

	/**
	 * Enqueues the user with the specified request.
	 *
	 * @param request The matchmaking request
	 */
	@Suspendable
	static void enqueue(MatchmakingRequest request) throws SuspendExecution, NullPointerException, IllegalStateException {
		Lock lock = null;
		try {
			lock = Connection.methodLock(request.getUserId());
			// Check if the user is already in a game
			UserId userId = new UserId(request.getUserId());
			if (Games.getGames().containsKey(userId)) {
				throw new IllegalStateException("User is already in a game");
			}

			SuspendableMap<UserId, String> currentQueue = SuspendableMap.getOrCreate("Matchmaking::currentQueue");
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
		} finally {
			if (lock != null) {
				lock.release();
			}
		}
	}

	@Suspendable
	static void dequeue(UserId userId) throws SuspendExecution {
		Lock lock = null;
		try {
			lock = Connection.methodLock(userId.toString());
			SuspendableMap<UserId, String> currentQueue = SuspendableMap.getOrCreate("Matchmaking::currentQueue");

			String queueId = currentQueue.remove(userId);
			if (queueId != null) {
				SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(queueId);
				queue.offer(new MatchmakingQueueEntry()
						.setCommand(MatchmakingQueueEntry.Command.CANCEL)
						.setUserId(userId.toString()), false);
			}
		} finally {
			if (lock != null) {
				lock.release();
			}
		}
	}

	static void handleConnections() {
		Connection.connected(suspendableHandler(connection -> {
			AtomicReference<Fiber<Void>> isAlive = new AtomicReference<>();
			AtomicReference<Fiber<Void>> gameReady = new AtomicReference<>();

			// If the user disconnects, dequeue them immediately.
			connection.endHandler(suspendableHandler(v -> {
				// Dequeue the user if they're currently enqueued.
				for (Fiber fiber : new Fiber[]{isAlive.getAndSet(null), gameReady.getAndSet(null)}) {
					if (fiber != null) {
						fiber.interrupt();
					}
				}

				dequeue(new UserId(connection.userId()));
			}));

			connection.handler(suspendableHandler(msg -> {
				EnvelopeMethod method = msg.getMethod();

				if (method != null) {
					if (method.getEnqueue() != null) {
						// Always dequeue the user first, silently succeeds regardless if they're currently enqueued.
						for (Fiber fiber : new Fiber[]{isAlive.getAndSet(null), gameReady.getAndSet(null)}) {
							if (fiber != null) {
								fiber.interrupt();
							}
						}

						dequeue(new UserId(connection.userId()));

						isAlive.set(getContextScheduler().newFiber(() -> {
							SuspendableCondition ping = SuspendableCondition.getOrCreate("Matchmaking::connection__ping[" + connection.userId() + "]");
							ping.awaitMillis(Long.MAX_VALUE);
							SuspendableCondition pong = SuspendableCondition.getOrCreate("Matchmaking::connection__pong[" + connection.userId() + "]");
							pong.signalAll();
							return null;
						}));

						gameReady.set(getContextScheduler().newFiber(() -> {
							boolean ready = getGameReadyCondition(connection.userId()).awaitMillis(Long.MAX_VALUE) > 0;

							if (ready) {
								// TODO: Message the user that their game is ready
								String id = Matchmaking.getCurrentMatch(CurrentMatchRequest.request(connection.userId())).getGameId();

								if (id == null) {
									throw new AssertionError("Current match ID should not be null if the game is ready.");
								}

								connection.write(new Envelope()
										.added(new EnvelopeAdded()
												.match(new Match()
														.createdAt(System.currentTimeMillis())
														.id(id))));
							}

							return null;
						}));

						isAlive.get().start();
						gameReady.get().start();

						enqueue(new MatchmakingRequest()
								.withUserId(connection.userId())
								.setQueueId(method.getEnqueue().getQueueId())
								.withDeckId(method.getEnqueue().getRequest().getDeckId())
								.withBotDeckId(method.getEnqueue().getRequest().getBotDeckId()));

						connection.write(new Envelope()
								.result(new EnvelopeResult()
										.enqueue(new DefaultMethodResponse())));
					}

					if (method.getDequeue() != null) {
						// Interrupt the notifications
						for (Fiber fiber : new Fiber[]{isAlive.getAndSet(null), gameReady.getAndSet(null)}) {
							if (fiber != null) {
								fiber.interrupt();
							}
						}

						dequeue(new UserId(connection.userId()));

						connection.write(new Envelope()
								.result(new EnvelopeResult()
										.dequeue(new DefaultMethodResponse())));
					}
				}
			}));
		}));
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
				.setWaitsForHost(false));

		Closeable quickPlay = startMatchmaker("quick-play", new MatchmakingQueueConfiguration()
				.setBotOpponent(true)
				.setLobbySize(1)
				.setName("Quick Play")
				.setOnce(false)
				.setPrivateLobby(false)
				.setRanked(false)
				.setRules(new CardDesc[0])
				.setStillConnectedTimeout(4000L)
				.setWaitsForHost(false));

		return (completionHandler -> constructed.close(v1 -> quickPlay.close(completionHandler)));
	}

	@Suspendable
	static Closeable startMatchmaker(String queueId, MatchmakingQueueConfiguration configuration) throws SuspendExecution {
		Fiber fiber = new Fiber(() -> {
			// There should only be one matchmaker per queue per cluster
			Lock lock;
			try {
				lock = SharedData.lock(queueId);
			} catch (VertxException timedOut) {
				if (timedOut.getCause() instanceof TimeoutException) {
					// The queue already exists elsewhere in the cluster, message will be logged later
					lock = null;
				} else {
					// A different, probably real error occurred.
					throw new RuntimeException(timedOut);
				}
			}

			if (lock == null) {
				LOGGER.info("startMatchmaker {}: Matchmaker already exists (only one per node per cluster allowed)", queueId);
				return;
			}

			try {
				List<MatchmakingRequest> thisMatchRequests = new ArrayList<>();
				int lobbySize = configuration.getLobbySize();
				SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(queueId);

				// Dequeue requests
				do {
					MatchmakingQueueEntry request = queue.take();
					switch (request.command) {
						case ENQUEUE:
							thisMatchRequests.add(request.request);
							break;
						case CANCEL:
							thisMatchRequests.removeIf(existingReq -> existingReq.getUserId().equals(request.userId));
							break;
					}

					if (thisMatchRequests.size() == lobbySize) {
						// Start a game. Check that everyone is still connected.

						long stillConnectedTimeout = configuration.getStillConnectedTimeout();
						List<Future> futures = new ArrayList<>(thisMatchRequests.size());
						// Everyone in the lobby will be pinged
						for (int i = 0; i < thisMatchRequests.size(); i++) {
							Future<Void> future = Future.future();
							SuspendableCondition pong = SuspendableCondition.getOrCreate("Matchmaking::connection__pong[" + thisMatchRequests.get(i).getUserId() + "]");
							SuspendableCondition ping = SuspendableCondition.getOrCreate("Matchmaking::connection__ping[" + thisMatchRequests.get(i).getUserId() + "]");
							ping.signal();
							pong.awaitMillis(stillConnectedTimeout, future);
							futures.add(future);
						}

						// Send out all the pings at once
						CompositeFuture allConnected = invoke(CompositeFuture.join(futures)::setHandler);

						// Is everyone still connected?
						if (allConnected.failed()) {
							// Reenqueue people who are still alive, then continue
							for (int i = thisMatchRequests.size() - 1; i >= 0; i--) {
								if (allConnected.failed(i)) {
									thisMatchRequests.remove(i);
								}
							}

							continue;
						}

						// Is this a bot game?
						if (configuration.isBotOpponent()) {
							// Create a bot game.
							MatchmakingRequest user = thisMatchRequests.get(0);
							bot(new UserId(user.getUserId()), new DeckId(user.getDeckId()), user.getBotDeckId() == null ? null : new DeckId(user.getBotDeckId()));
							getGameReadyCondition(user.getUserId()).signal();
							continue;
						}

						// Create a game for every pair
						if (thisMatchRequests.size() % 2 != 0) {
							throw new AssertionError("thisMatchRequests.size()");
						}

						for (int i = 0; i < thisMatchRequests.size(); i += 2) {
							MatchmakingRequest user1 = thisMatchRequests.get(i);
							MatchmakingRequest user2 = thisMatchRequests.get(i + 1);

							// This is a standard competitive match
							vs(GameId.create(), new UserId(user1.getUserId()), new DeckId(user1.getDeckId()), new UserId(user2.getUserId()), new DeckId(user2.getDeckId()));

							getGameReadyCondition(user1.getUserId()).signal();
							getGameReadyCondition(user2.getUserId()).signal();
						}
					}
				} while (/*Queues that run once are typically private games*/!configuration.isOnce());

				// Clean up all the resources that the queue used
				queue.destroy();
			} catch (VertxException | InterruptedException ex) {
				// Cancelled
			} finally {
				lock.release();
			}
		});

		fiber.start();

		return completionHandler -> {
			fiber.interrupt();
			completionHandler.handle(Future.succeededFuture());
		};
	}

	@NotNull
	@Suspendable
	static SuspendableCondition getGameReadyCondition(String userId) {
		return SuspendableCondition.getOrCreate(userId + "__gameReady");
	}

	class MatchmakingQueueEntry implements Serializable, ClusterSerializable {
		enum Command {
			ENQUEUE,
			CANCEL
		}

		private Command command;
		private MatchmakingRequest request;
		private String userId;


		@Override
		public void writeToBuffer(Buffer buffer) {
			json(this).writeToBuffer(buffer);
		}

		@Override
		public int readFromBuffer(int pos, Buffer buffer) {
			JsonObject obj = new JsonObject();
			int newPos = obj.readFromBuffer(pos, buffer);
			MatchmakingQueueEntry inst = obj.mapTo(MatchmakingQueueEntry.class);
			this.command = inst.command;
			this.request = inst.request;
			this.userId = inst.userId;
			return newPos;
		}


		public Command getCommand() {
			return command;
		}

		public MatchmakingQueueEntry setCommand(Command command) {
			this.command = command;
			return this;
		}

		public MatchmakingRequest getRequest() {
			return request;
		}

		public MatchmakingQueueEntry setRequest(MatchmakingRequest request) {
			this.request = request;
			return this;
		}

		public String getUserId() {
			return userId;
		}

		public MatchmakingQueueEntry setUserId(String userId) {
			this.userId = userId;
			return this;
		}
	}

	class MatchmakingQueueConfiguration implements Serializable {
		private String name;
		private CardDesc[] rules;
		private int lobbySize = 2;
		private boolean botOpponent;
		private boolean ranked;
		private boolean privateLobby;
		private boolean waitsForHost;
		private long stillConnectedTimeout = 2000L;
		private boolean once;

		public String getName() {
			return name;
		}

		public MatchmakingQueueConfiguration setName(String name) {
			this.name = name;
			return this;
		}

		public CardDesc[] getRules() {
			return rules;
		}

		public MatchmakingQueueConfiguration setRules(CardDesc[] rules) {
			this.rules = rules;
			return this;
		}

		public int getLobbySize() {
			return lobbySize;
		}

		public MatchmakingQueueConfiguration setLobbySize(int lobbySize) {
			this.lobbySize = lobbySize;
			return this;
		}

		public boolean isBotOpponent() {
			return botOpponent;
		}

		public MatchmakingQueueConfiguration setBotOpponent(boolean botOpponent) {
			this.botOpponent = botOpponent;
			return this;
		}

		public boolean isRanked() {
			return ranked;
		}

		public MatchmakingQueueConfiguration setRanked(boolean ranked) {
			this.ranked = ranked;
			return this;
		}

		public boolean isPrivateLobby() {
			return privateLobby;
		}

		public MatchmakingQueueConfiguration setPrivateLobby(boolean privateLobby) {
			this.privateLobby = privateLobby;
			return this;
		}

		public boolean isWaitsForHost() {
			return waitsForHost;
		}

		public MatchmakingQueueConfiguration setWaitsForHost(boolean waitsForHost) {
			this.waitsForHost = waitsForHost;
			return this;
		}

		public long getStillConnectedTimeout() {
			return stillConnectedTimeout;
		}

		public MatchmakingQueueConfiguration setStillConnectedTimeout(long stillConnectedTimeout) {
			this.stillConnectedTimeout = stillConnectedTimeout;
			return this;
		}

		public boolean isOnce() {
			return once;
		}

		public MatchmakingQueueConfiguration setOnce(boolean once) {
			this.once = once;
			return this;
		}
	}
}
