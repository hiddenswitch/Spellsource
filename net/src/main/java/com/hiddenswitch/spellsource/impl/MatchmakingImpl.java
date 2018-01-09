package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.impl.server.PregamePlayerConfiguration;
import com.hiddenswitch.spellsource.impl.server.VertxScheduler;
import com.hiddenswitch.spellsource.impl.util.Scheduler;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.demilich.metastone.game.decks.Deck;

import java.util.*;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

public class MatchmakingImpl extends AbstractService<MatchmakingImpl> implements Matchmaking {
	private static final Logger logger = LoggerFactory.getLogger(Matchmaking.class);
	private static final int DELAY = 4000;
	private RpcClient<Games> games;
	private RpcClient<Logic> logic;
	private RpcClient<Bots> bots;
	private Scheduler scheduler;
	private SuspendableMap<UserId, InvocationId> locks;
	private SuspendableMap<UserId, QueueEntry> queue;
	private Map<UserId, TimerId> timers;
	private SuspendableMap<GameId, CreateGameSessionResponse> connections;
	private Registration registration;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();

		scheduler = new VertxScheduler(vertx);
		timers = new HashMap<>();
		connections = Games.getConnections(vertx);
		queue = Matchmaking.getQueue(vertx);
		locks = Matchmaking.getLocks(vertx);
		games = Rpc.connect(Games.class, vertx.eventBus());
		logic = Rpc.connect(Logic.class, vertx.eventBus());
		bots = Rpc.connect(Bots.class, vertx.eventBus());
		registration = Rpc.register(this, Matchmaking.class, vertx.eventBus());
	}

	@Override
	public MatchCancelResponse cancel(MatchCancelRequest matchCancelRequest) throws SuspendExecution, InterruptedException {
		final String userId = matchCancelRequest.getUserId();
		queue.remove(new UserId(userId));
		return new MatchCancelResponse(true, null, -1);
	}

	@Override
	public MatchCreateResponse createMatch(MatchCreateRequest request) throws SuspendExecution, InterruptedException {
		if (logger.isDebugEnabled()) {
			logger.debug("createMatch: Creating match for request " + request.toString());
		}
		final String deckId1 = request.getDeckId1().toString();
		final String deckId2 = request.getDeckId2().toString();
		final String userId1 = request.getUserId1().toString();
		final String userId2 = request.getUserId2().toString();
		final String gameId = request.getGameId().toString();

		StartGameResponse startGameResponse = logic.sync().startGame(new StartGameRequest()
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
				.withPregame1(new PregamePlayerConfiguration(deck1, userId1)
						.withAI(request.isBot1())
						.withAttributes(startGameResponse.getPregamePlayerConfiguration1().getAttributes()))
				.withPregame2(new PregamePlayerConfiguration(deck2, userId2)
						.withAI(request.isBot2())
						.withAttributes(startGameResponse.getPregamePlayerConfiguration2().getAttributes()))
				.withGameId(gameId);
		CreateGameSessionResponse createGameSessionResponse = games.sync().createGameSession(createGameSessionRequest);
		final GameId finalGameId = new GameId(createGameSessionResponse.gameId);
		final UserId key1 = new UserId(userId1);
		final UserId key2 = new UserId(userId2);
		queue.replace(key1, QueueEntry.ready(finalGameId, new DeckId(deckId1)));
		queue.replace(key2, QueueEntry.ready(finalGameId, new DeckId(deckId2)));

		if (timers.containsKey(key1)) {
			logger.debug("createMatch: Canceling timer for userId " + key1);
			scheduler.cancelTimer(timers.remove(key1));
		}

		if (timers.containsKey(key2)) {
			logger.debug("createMatch: Canceling timer for userId " + key2);
			scheduler.cancelTimer(timers.remove(key2));
		}

		return new MatchCreateResponse(createGameSessionResponse);
	}

	@Override
	@Suspendable
	public MatchmakingResponse matchmakeAndJoin(MatchmakingRequest matchmakingRequest) throws SuspendExecution, InterruptedException {
		if (logger.isDebugEnabled()) {
			logger.debug("matchmakeAndJoin: Starting request for user " + matchmakingRequest.getUserId());
		}

		logQueue();

		final InvocationId invocationId = InvocationId.create();
		RuntimeException ex = null;

		final UserId userId = new UserId(matchmakingRequest.getUserId());
		final DeckId deckId = new DeckId(matchmakingRequest.getDeckId());

		final InvocationId lock = locks.putIfAbsent(userId, invocationId);
		// Sometimes the method may lock another user, which has to be released too if the lock came from this invocation
		UserId otherUserId = null;

		try {
			if (lock != null) {
				logger.debug("matchmakeAndJoin: User " + userId + " is busy elsewhere with invocation " + lock);
				return MatchmakingResponse.notReady(userId.toString(), matchmakingRequest.getDeckId());
			}

			logger.debug("matchmakeAndJoin: User " + userId + " is inspecting the queue");
			final QueueEntry status = queue.putIfAbsent(userId, QueueEntry.pending(deckId));

			if (status != null) {
				// If we're pending, this is a retry
				final MatchmakingResponse response;
				if (status.isPending()) {
					// Refresh the queue entry
					refreshQueueTimer(userId);

					logger.debug("matchmakeAndJoin: User " + userId + " is pending, refreshing the timer.");
					response = MatchmakingResponse.notReady(matchmakingRequest.getUserId(), matchmakingRequest.getDeckId());
				} else {

					// We already have a game
					logger.debug("matchmakeAndJoin: User " + userId + " already has a game with gameId " + status);
					response = MatchmakingResponse.ready(status.gameId.toString());
				}
				logger.debug("matchmakeAndJoin: User " + userId + " is unlocked by the refresh path.");
				return response;
			} else {
				// We're coming into the queue for the first time.
				logger.debug("matchmakeAndJoin: User " + userId + " is now in the queue for the first time.");

				// If this is a bot request, create a bot game.
				if (matchmakingRequest.isBotMatch()) {
					logger.debug("matchmakeAndJoin: Matchmaker is creating an AI game for " + userId);
					final BotsStartGameRequest request = new BotsStartGameRequest(matchmakingRequest.getUserId(), matchmakingRequest.getDeckId());
					BotsStartGameResponse botGameStarted = bots.sync().startGame(request);
					// Occupy a spot in the queue for this user and the bot
					queue.put(userId, QueueEntry.ready(new GameId(botGameStarted.getGameId()), deckId));
					queue.put(new UserId(botGameStarted.getBotUserId()), QueueEntry.ready(new GameId(botGameStarted.getGameId()), new DeckId(botGameStarted.getBotDeckId())));
					logger.debug("matchmakeAndJoin: User " + userId + " is unlocked by the AI bot creation path.");
					return MatchmakingResponse.ready(botGameStarted.getGameId());
				}

				// Otherwise, if there is another user in the queue, create a game for us
				Iterator<Map.Entry<UserId, QueueEntry>> queueIterator = queue.entrySet().iterator();

				// TODO: It probably makes more sense to keep the pending users in a separate set.
				while (queueIterator.hasNext()) {
					final Map.Entry<UserId, QueueEntry> entry = queueIterator.next();
					otherUserId = entry.getKey();
					if (otherUserId.equals(userId)
							|| !entry.getValue().isPending()) {
						logger.debug("matchmakeAndJoin: User " + otherUserId + " is already in a match, checking next queue entry to match " + userId);
						continue;
					}

					logger.debug("matchmakeAndJoin: User " + userId + " is locked by the pairing path.");
					InvocationId otherUserLock = locks.putIfAbsent(otherUserId, invocationId);

					if (otherUserLock != null) {
						logger.debug("matchmakeAndJoin: Matcher wants to pair " + userId + " with " + otherUserId + " but this second user is currently processing their own queue situation.");
						continue;
					}

					final GameId gameId = GameId.create();
					logger.debug("matchmakeAndJoin: Matching users " + otherUserId + " and " + userId + " into gameId " + gameId);

					final DeckId otherDeckId = entry.getValue().deckId;

					MatchCreateResponse createMatchResponse = createMatch(new MatchCreateRequest()
							.withDeckId1(deckId)
							.withDeckId2(otherDeckId)
							.withUserId1(userId)
							.withUserId2(otherUserId)
							.withGameId(gameId));

					CreateGameSessionResponse createGameSessionResponse = createMatchResponse.getCreateGameSessionResponse();

					if (createGameSessionResponse.pending) {
						logger.debug("matchmakeAndJoin: Retrying createMatch... ");
						int i = 0;
						final int retries = 4;
						final int retryDelay = 500;
						for (; i < retries; i++) {
							Strand.sleep(retryDelay);

							logger.debug("matchmakeAndJoin: Checking if the Games service has created a game for gameId " + gameId);
							createGameSessionResponse = connections.get(gameId);
							if (!createGameSessionResponse.pending) {
								break;
							}
						}

						if (i >= retries) {
							throw new NullPointerException("Timed out while waiting for a match to be created for users " + userId + " and " + otherUserId);
						}
					}

					logger.debug("matchmakeAndJoin: Users " + userId + " and " + otherUserId + " are unlocked because a game has been successfully created for them with gameId " + gameId);
					return MatchmakingResponse.ready(gameId.toString());

				}

				logger.debug("matchamkeAndJoin: All users currently in games. Waiting and going into queue.");
			}

		} catch (RuntimeException re) {
			ex = re;
		} finally {
			logger.debug("matchmakeAndJoin: Releasing lock for userId " + userId);
			// Only release the locks if they were associated with this invocation
			locks.remove(userId, invocationId);

			if (otherUserId != null) {
				locks.remove(otherUserId, invocationId);
			}

			if (ex != null) {
				throw ex;
			}
		}
		return MatchmakingResponse.notReady(userId.toString(), deckId.toString());
	}

	public void refreshQueueTimer(UserId userId) {
		logger.debug("refreshQueueTimer: for userId " + userId);
		final TimerId id = timers.remove(userId);

		if (id != null) {
			scheduler.cancelTimer(id);
		}

		timers.put(userId, scheduler.setTimer(DELAY, suspendableHandler(ignored -> {
			queue.remove(userId, GameId.PENDING);
			timers.remove(userId);
		})));
	}

	@Override
	public CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException {
		logger.debug("getCurrentMatch: Retrieving information for userId " + request.getUserId());
		logQueue();
		final QueueEntry queueEntry = queue.get(new UserId(request.getUserId()));
		if (queueEntry != null) {
			logger.debug("getCurrentMatch: User " + request.getUserId() + " has match " + queueEntry.gameId);
			return new CurrentMatchResponse(queueEntry.gameId.toString());
		} else {
			logger.debug("getCurrentMatch: User " + request.getUserId() + " does not have match.");
			return new CurrentMatchResponse(null);
		}
	}

	@Override
	public MatchExpireResponse expireOrEndMatch(MatchExpireRequest request) throws SuspendExecution, InterruptedException {
		if (logger.isDebugEnabled()) {
			logger.debug("expireOrEndMatch: Expiring match " + request.gameId);
		}

		logQueue();
		if (request.users == null) {
			throw new NullPointerException("Request does not contain users specified");
		}

		for (UserId userId : request.users) {
			if (!queue.containsKey(userId)) {
				throw new RuntimeException("Failed to expire game because users " + String.join(", ", request.users.stream().map(UserId::toString).collect(Collectors.toList())) + " had no queue entries");
			}
		}

		// Bots don't occupy queue positions
		List<DeckId> decks = new ArrayList<>();
		for (UserId user : request.users) {
			QueueEntry queueEntry = queue.remove(user);
			if (queueEntry != null) {
				DeckId deckId = queueEntry.deckId;
				decks.add(deckId);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("expireOrEndMatch: Removed decks " + String.join(", ", decks.stream().map(DeckId::toString).collect(Collectors.toList())));
		}


		// End the game in alliance mode
		logic.sync().endGame(new EndGameRequest()
				.withPlayers(new EndGameRequest.Player()
								.withDeckId(decks.get(0).toString()),
						new EndGameRequest.Player()
								.withDeckId(decks.get(1).toString())));

		if (logger.isDebugEnabled()) {
			logger.debug("expireOrEndMatch: Called Logic::endGame");
		}

		return new MatchExpireResponse();
	}


	protected void logQueue() {
		if (logger.isDebugEnabled()) {
			List<String> list = new ArrayList<>();
			for (Map.Entry<UserId, QueueEntry> e : queue.entrySet()) {
				String format = String.format("(%s, %s)", e.getKey().toString(), e.getValue().gameId.toString());
				list.add(format);
			}
			logger.debug("getCurrentMatch: queueContents=" + String.join(", ", list));
		}
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();

		if (registration != null) {
			Rpc.unregister(registration);
		}
	}
}
