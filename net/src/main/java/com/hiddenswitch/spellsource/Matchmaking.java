package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.concurrent.*;
import com.hiddenswitch.spellsource.impl.DeckId;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
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
		SuspendableLock botLock = null;
		GameId gameId = GameId.create();

		try {
			// TODO: Move this lock into pollBotId
			botLock = SuspendableLock.lock("Matchmaking::takingBot", 30000L);

			// The player has been waiting too long. Match to an AI.
			// Retrieve a bot and use it to play against the opponent
			UserRecord bot = Accounts.get(Bots.pollBotId());
			if (botDeckId != null) {
				LOGGER.info("bot: UserId {} requested bot to play deckId {}", userId, botDeckId);
			}

			if (botDeckId == null) {
				botDeckId = new DeckId(Bots.getRandomDeck(bot));
			}

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("expireOrEndMatch: Expiring match " + request.getGameId());
		}

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
	 * Cancels the user's matchmaking, wherever the user may be connected.
	 *
	 * @param userId The user whose matchmaking should be canceled.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static void cancel(UserId userId) throws SuspendExecution, InterruptedException {
		dequeue(userId);
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
		LOGGER.trace("matchmake {}: Starting request {}", request.getUserId(), request);
		String userId = request.getUserId();
//		Fiber<Void> f1 = null;
		if (request.isBotMatch()) {
			request.setQueueId("quickPlay");
		} else {
			request.setQueueId("constructed");
		}
		UserId key = new UserId(userId);
		enqueue(request);
		try {
			/*
			f1 = getContextScheduler().newFiber(() -> {
				SuspendableCondition ping = pingCondition(userId);
				SuspendableCondition pong = pongCondition(userId);
				if (!ping.await()) {
					// Cancelled
					LOGGER.trace("matchmake: Ping pong canceled for {}", request);
					return null;
				}
				pong.signalAll();
				return null;
			});

			f1.start();
			*/

			long timeout = request.getTimeout();
			boolean hasMatch = false;
			while (timeout > 0) {
				timeout = getGameReadyCondition(userId).awaitMillis(timeout);
				hasMatch = Games.getGames().containsKey(key);
				if (hasMatch) {
					LOGGER.trace("matchmake: Received match for {}", request);
					break;
				}
			}

			if (!hasMatch) {
				throw new TimeoutException("hasMatch");
			}

			return Games.getGames().get(key);
		} catch (TimeoutException ex) {
			LOGGER.trace("matchmake {}: Dequeued due to timeout", userId);
			dequeue(key);
			return null;
		} catch (Throwable any) {
			LOGGER.error("matchmake {}", userId, any);
			dequeue(key);
			return null;
		} finally {
			LOGGER.trace("matchmake {}: Exiting", userId);
			/*
			if (f1 != null) {
				f1.interrupt();
			}
			*/
		}
	}

	/**
	 * Enqueues the user with the specified request.
	 *
	 * @param request The matchmaking request
	 */
	@Suspendable
	static void enqueue(MatchmakingRequest request) throws SuspendExecution, NullPointerException, IllegalStateException {
		SuspendableLock lock = null;
		try {
			lock = Connection.methodLock(request.getUserId());
			// Check if the user is already in a game
			UserId userId = new UserId(request.getUserId());
			if (Games.getGames().containsKey(userId)) {
				throw new IllegalStateException("User is already in a game");
			}

			SuspendableMap<UserId, String> currentQueue = currentQueue();
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

	@NotNull
	@Suspendable
	static SuspendableMap<UserId, String> currentQueue() {
		return SuspendableMap.getOrCreate("Matchmaking::currentQueue");
	}

	@Suspendable
	static void dequeue(UserId userId) throws SuspendExecution {
		SuspendableLock lock = null;
		try {
			lock = Connection.methodLock(userId.toString());
			SuspendableMap<UserId, String> currentQueue = currentQueue();
			String queueId = currentQueue.remove(userId);
			if (queueId != null) {
				SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(queueId);
				queue.offer(new MatchmakingQueueEntry()
						.setCommand(MatchmakingQueueEntry.Command.CANCEL)
						.setUserId(userId.toString()), false);
				LOGGER.trace("dequeue {}: Successfully dequeued", userId);
			}
		} finally {
			if (lock != null) {
				lock.release();
			}
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
				.setWaitsForHost(false));

		Closeable quickPlay = startMatchmaker("quickPlay", new MatchmakingQueueConfiguration()
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
		Fiber<Void> fiber = getContextScheduler().newFiber(() -> {
			// There should only be one matchmaker per queue per cluster
			SuspendableLock lock;
			try {
				lock = SuspendableLock.lock("Matchmaking::queues[" + queueId + "]");
			} catch (VertxException timedOut) {
				if (timedOut.getCause() instanceof TimeoutException) {
					// The queue already exists elsewhere in the cluster, message will be logged later
					lock = null;
				} else if (timedOut.getCause() instanceof InterruptedException) {
					LOGGER.info("startMatchmaker {}: Closing on a failover instance (interrupted while waiting for lock)", queueId);
					return null;
				} else {
					// A different, probably real error occurred.
					throw new RuntimeException(timedOut);
				}
			}

			if (lock == null) {
				LOGGER.info("startMatchmaker {}: Matchmaker already exists (only one per node per cluster allowed)", queueId);
				return null;
			}

			try {
				List<MatchmakingRequest> thisMatchRequests = new ArrayList<>();
				int lobbySize = configuration.getLobbySize();
				SuspendableQueue<MatchmakingQueueEntry> queue = SuspendableQueue.get(queueId);

				// Dequeue requests
				do {
					LOGGER.trace("startMatchmaker {}: Awaiting {} users", queueId, lobbySize);
					while (thisMatchRequests.size() < lobbySize) {
						MatchmakingQueueEntry request = queue.take();
						switch (request.command) {
							case ENQUEUE:
								thisMatchRequests.add(request.request);
								LOGGER.trace("startMatchmaker {}: Queued {}", queueId, request.getUserId());
								break;
							case CANCEL:
								thisMatchRequests.removeIf(existingReq -> existingReq.getUserId().equals(request.userId));
								currentQueue().remove(new UserId(request.userId));
								LOGGER.trace("startMatchmaker {}: Dequeued {}", queueId, request.getUserId());
								break;
						}
					}
					// Start a game. Check that everyone is still connected.

					/* TODO: This is still too slow
					long stillConnectedTimeout = configuration.getStillConnectedTimeout();
					List<Future> futures = new ArrayList<>(thisMatchRequests.size());
					LOGGER.trace("startMatchmaker {}: Checking still connected", queueId);
					// Everyone in the lobby will be pinged
					for (int i = 0; i < thisMatchRequests.size(); i++) {
						Future<Void> future = Future.future();
						String userId = thisMatchRequests.get(i).getUserId();
						SuspendableCondition pong = pongCondition(userId);
						SuspendableCondition ping = pingCondition(userId);
						pong.awaitMillis(stillConnectedTimeout, future);
						LOGGER.trace("startMatchmaker {}: ponged, now pinging {}", queueId, userId);
						ping.signal();
						futures.add(future);
					}

					// Send out all the pings at once
					LOGGER.trace("startMatchmaker {}: Joining all connected", queueId);
					CompositeFuture allConnected = Sync.invoke1(CompositeFuture.join(futures)::setHandler);

					// Is everyone still connected?
					if (allConnected.failed()) {
						LOGGER.trace("startMatchmaker {}: Failed all connected", queueId);
						// Reenqueue people who are still alive, then continue
						for (int i = thisMatchRequests.size() - 1; i >= 0; i--) {
							if (allConnected.failed(i)) {
								thisMatchRequests.remove(i);
							}
						}

						continue;
					}

					LOGGER.trace("startMatchmaker {}: Succeeded all connected", queueId);
					*/

					// Is this a bot game?
					if (configuration.isBotOpponent()) {
						// Create a bot game.
						MatchmakingRequest user = thisMatchRequests.get(0);
						bot(new UserId(user.getUserId()), new DeckId(user.getDeckId()), user.getBotDeckId() == null ? null : new DeckId(user.getBotDeckId()));
						getGameReadyCondition(user.getUserId()).signal();
						thisMatchRequests.clear();
						currentQueue().remove(new UserId(user.getUserId()));
						continue;
					}

					// Create a game for every pair
					if (thisMatchRequests.size() % 2 != 0) {
						throw new AssertionError("thisMatchRequests.size()");
					}

					LOGGER.trace("startMatchmaker {}: Creating game", queueId);
					for (int i = 0; i < thisMatchRequests.size(); i += 2) {
						MatchmakingRequest user1 = thisMatchRequests.get(i);
						MatchmakingRequest user2 = thisMatchRequests.get(i + 1);

						// This is a standard competitive match
						vs(GameId.create(), new UserId(user1.getUserId()), new DeckId(user1.getDeckId()), new UserId(user2.getUserId()), new DeckId(user2.getDeckId()));
						LOGGER.trace("startMatchmaker {}: Created game for {} and {}", queueId, user1.getUserId(), user2.getUserId());

						getGameReadyCondition(user1.getUserId()).signal();
						getGameReadyCondition(user2.getUserId()).signal();

						currentQueue().remove(new UserId(user1.getUserId()));
						currentQueue().remove(new UserId(user2.getUserId()));
					}

					thisMatchRequests.clear();
				} while (/*Queues that run once are typically private games*/!configuration.isOnce());

				// Clean up all the resources that the queue used
				queue.destroy();
			} catch (VertxException | InterruptedException ex) {
				// Cancelled
			} finally {
				lock.release();
			}
			return null;
		});

		fiber.start();

		AtomicReference<Fiber<Void>> thisFiber = new AtomicReference<>(fiber);
		return completionHandler -> {
			thisFiber.get().interrupt();
			completionHandler.handle(Future.succeededFuture());
		};
	}

	@NotNull
	@Suspendable
	static SuspendableCondition pingCondition(String userId) {
		return SuspendableCondition.getOrCreate("Matchmaking::connection__ping[" + userId + "]");
	}

	@NotNull
	@Suspendable
	static SuspendableCondition pongCondition(String userId) {
		return SuspendableCondition.getOrCreate("Matchmaking::connection__pong[" + userId + "]");
	}

	@NotNull
	@Suspendable
	static SuspendableCondition getGameReadyCondition(String userId) {
		return SuspendableCondition.getOrCreate("Matchmaking::connection__gameReady[" + userId + "]");
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
							SuspendableCondition ping = pingCondition(connection.userId());
							SuspendableCondition pong = pongCondition(connection.userId());
							if (!ping.await()) {
								// Cancelled
								return null;
							}
							pong.signalAll();
							return null;
						}));

						gameReady.set(getContextScheduler().newFiber(() -> {
							boolean ready = getGameReadyCondition(connection.userId()).await();

							if (ready) {
								// TODO: Message the user that their game is ready
								String id = Matchmaking.getCurrentMatch(CurrentMatchRequest.request(connection.userId())).getGameId();

								if (id == null) {
									throw new AssertionError("Current match ID should not be null if the game is ready.");
								}

								connection.write(new Envelope()
										.result(new EnvelopeResult()
												.enqueue(new MatchmakingQueuePutResponse()
														.unityConnection(new MatchmakingQueuePutResponseUnityConnection().firstMessage(new ClientToServerMessage()
																.messageType(MessageType.FIRST_MESSAGE)
																.firstMessage(new ClientToServerMessageFirstMessage()))))));
							}

							return null;
						}));

						isAlive.get().start();
						gameReady.get().start();

						enqueue(new MatchmakingRequest()
								.withUserId(connection.userId())
								.setQueueId(method.getEnqueue().getQueueId())
								.withDeckId(method.getEnqueue().getDeckId())
								.withBotDeckId(method.getEnqueue().getBotDeckId()));
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
