package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.*;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.common.SuspendablePump;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.impl.server.GameSession;
import com.hiddenswitch.spellsource.impl.server.GameSessionImpl;
import com.hiddenswitch.spellsource.impl.server.SessionWriter;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import com.hiddenswitch.spellsource.impl.util.DeckType;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.Pump;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.utils.Attribute;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class ClusteredGamesImpl extends SyncVerticle implements Games {
	public static final String READER_ADDRESS_PREFIX = "Games::reader-";
	private Registration registration;
	private SuspendableMap<GameId, CreateGameSessionResponse> connections;
	private Map<GameId, GameSession> sessions = new ConcurrentHashMap<>();
	private Map<GameId, List<Runnable>> pipeClosers = new ConcurrentHashMap<>();
	private ListMultimap<GameId, ActivityMonitor> gameActivityMonitors = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

	@Override
	public void start() throws SuspendExecution {
		CardCatalogue.loadCardsFromPackage();

		connections = Games.getConnections();
		registration = Rpc.register(this, Games.class);
	}

	@Override
	public CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException {
		if (Games.LOGGER.isDebugEnabled()) {
			Games.LOGGER.debug("createGameSession: Creating game session for request " + request.toString());
		}
		final String gameId = request.getGameId();

		if (gameId == null) {
			throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
		}

		final GameId key = new GameId(gameId);
		final CreateGameSessionResponse pending = CreateGameSessionResponse.pending(deploymentID());
		CreateGameSessionResponse connection = connections.putIfAbsent(key, pending);
		// If we're the ones deploying this match...
		if (connection == null) {
			Games.LOGGER.debug("createGameSession: DeploymentId " + deploymentID() + " is responsible for deploying this match.");
			GameSession session = new GameSessionImpl(Gateway.getHostAddress(),
					Port.port(),
					request.getPregame1(),
					request.getPregame2(),
					gameId,
					vertx);

			// Deal with ending the game
			session.handleGameOver(this::onGameOver);

			// Listen for messages from the clients
			final EventBus eventBus = vertx.eventBus();
			final ActivityMonitor activityMonitor = new ActivityMonitor(vertx, gameId, request.getNoActivityTimeout(), this::kill);
			final ActivityMonitor connectionTimeout = new ActivityMonitor(vertx, gameId, 10000L, this::connectionTimedOut);
			final ArrayList<Runnable> closers = new ArrayList<>();
			closers.add(connect(session, 0, request.getPregame1().getUserId(), eventBus, Arrays.asList(activityMonitor, connectionTimeout)));
			closers.add(connect(session, 1, request.getPregame2().getUserId(), eventBus, Arrays.asList(activityMonitor, connectionTimeout)));
			pipeClosers.put(key, closers);
			gameActivityMonitors.put(key, activityMonitor);
			gameActivityMonitors.put(key, connectionTimeout);
			final CreateGameSessionResponse response = CreateGameSessionResponse.session(deploymentID(), session);
			connections.replace(key, response);
			sessions.put(key, session);
			return response;
		} else {
			Games.LOGGER.debug("createGameSession: Repeat createGameSessionRequest suspected because actually deploymentId " + connection.deploymentId + " is responsible for deploying this match.");
			// Otherwise, return its state, whatever it is
			return connection;
		}
	}

	@Suspendable
	private void connectionTimedOut(ActivityMonitor activityMonitor) throws SuspendExecution, InterruptedException {
		// Check if players have connected
		final GameId key = new GameId(activityMonitor.getGameId());
		final GameSession gameSession = sessions.get(key);
		if (gameSession == null
				|| gameSession.isGameReady()) {
			activityMonitor.cancel();
			gameActivityMonitors.remove(key, activityMonitor);
		} else {
			kill(key.toString());
		}
	}

	@Suspendable
	private void onGameOver(GameSessionImpl session) {
		Games.LOGGER.debug("onGameOver: Handling on game over for session " + session.getGameId());
		final String gameOverId = session.getGameId();
		// The players should not accidentally wind back up in games
		try {
			kill(gameOverId);
		} catch (InterruptedException | SuspendExecution e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Connects the given session. Returns a function used to close the connections.
	 *
	 * @param session
	 * @param playerId
	 * @param userId
	 * @param eventBus
	 * @param activityMonitors
	 * @return A function that when called closes all the created pipes.
	 */
	private Runnable connect(GameSession session, int playerId, String userId, EventBus eventBus, List<ActivityMonitor> activityMonitors) {
		Games.LOGGER.debug("connect: Connecting userId " + userId + " with gameId " + session.getGameId());
		final SessionWriter writer = new SessionWriter(userId, playerId, eventBus, session, activityMonitors);
		final MessageConsumer<Buffer> reader = eventBus.consumer(READER_ADDRESS_PREFIX + userId);
		final Pump pipe = new SuspendablePump<>(reader.bodyStream(), writer, Integer.MAX_VALUE).start();
		return () -> {
			pipe.stop();
			writer.end();
			reader.unregister();
			Games.LOGGER.debug("connect: Closing writing pipe for userId " + userId);
		};
	}

	@Suspendable
	private void kill(ActivityMonitor monitor) throws InterruptedException, SuspendExecution {
		kill(monitor.getGameId());
	}

	@Suspendable
	private void kill(String gameId) throws InterruptedException, SuspendExecution {
		final GameId key = new GameId(gameId);
		if (!sessions.containsKey(key)) {
			Games.LOGGER.debug("kill {}: This deployment with deploymentId {} does not contain the gameId", gameId, deploymentID());
			return;
		}
		Games.LOGGER.debug("kill {}: Calling kill", gameId);
		GameSession session = sessions.remove(key);
		CreateGameSessionResponse connection = connections.remove(key);

		final MatchExpireRequest request = new MatchExpireRequest(gameId);
		request.setUsers(Arrays.asList(connection.userId1, connection.userId2));
		if (request.getUsers().size() < 2
				|| request.getUsers().get(0) == null
				|| request.getUsers().get(1) == null) {
			throw new IllegalArgumentException("No users were returned correctly by the session.");
		}

		UserId winner = null;
		try {
			session.getGameContext().updateAndGetGameOver();
			if (session.getGameContext() != null && session.getGameContext().getWinner() != null && session.getGameContext().getWinner().getUserId() != null) {
				winner = new UserId(session.getGameContext().getWinner().getUserId());
				request.setWinner(winner);
			}
			// Save the wins/losses
			if (winner != null) {
				String userIdWinner = winner.toString();
				String userIdLoser = session.getGameContext().getOpponent(session.getGameContext().getWinner()).getUserId();
				String deckIdWinner = (String) session.getGameContext().getWinner().getAttribute(Attribute.DECK_ID);
				String deckIdLoser = (String) session.getGameContext().getOpponent(session.getGameContext().getWinner()).getAttribute(Attribute.DECK_ID);
				// Check if this deck was a draft deck
				if (Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner, "deckType", DeckType.DRAFT.toString()),
						json("$inc", json("totalGames", 1, "wins", 1))).getDocModified() > 0L) {
					Mongo.mongo().updateCollection(Draft.DRAFTS, json("_id", userIdWinner), json("$inc", json("publicDraftState.wins", 1)));
					LOGGER.trace("kill {}: Marked {} as winner in draft", gameId, userIdWinner);
				} else {
					Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner),
							json("$inc", json("totalGames", 1, "wins", 1)));
					LOGGER.trace("kill {}: Marked {} as winner in other", gameId, userIdWinner);
				}

				// Check if this deck was a draft deck
				if (Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser, "deckType", DeckType.DRAFT.toString()),
						json("$inc", json("totalGames", 1))).getDocModified() > 0L) {
					Mongo.mongo().updateCollection(Draft.DRAFTS, json("_id", userIdLoser), json("$inc", json("publicDraftState.losses", 1)));
					LOGGER.trace("kill {}: Marked {} as loser in draft", gameId, userIdLoser);
				} else {
					Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser),
							json("$inc", json("totalGames", 1)));
					LOGGER.trace("kill {}: Marked {} as loser in other", gameId, userIdLoser);
				}
			}
		} catch (Throwable ex) {
			LOGGER.error("kill {}: Could not get winner due to {}", ex);
		}

		try {
			Matchmaking.expireOrEndMatch(request);
		} catch (VertxException noHandlerFound) {
			Games.LOGGER.error("kill: For gameId " + gameId + ", an error occurred trying to expireOrEndMatch: " + noHandlerFound.getMessage());
		}

		session.kill();

		if (gameActivityMonitors.containsKey(key)) {
			gameActivityMonitors.get(key).forEach(ActivityMonitor::cancel);
		}
		gameActivityMonitors.removeAll(key);

		for (Runnable runnable : pipeClosers.get(key)) {
			runnable.run();
		}

		pipeClosers.remove(key);
	}

	@Override
	public DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request) {
		GameId key = new GameId(request.getGameId());
		if (sessions.containsKey(key)) {
			Games.LOGGER.debug("describeGameSession: Describing gameId " + request.getGameId());
			return DescribeGameSessionResponse.fromGameContext(sessions.get(key).getGameContext());
		} else {
			Games.LOGGER.debug("describeGameSession: This game session does not contain the gameId " + request.getGameId());
			return new DescribeGameSessionResponse();
		}
	}

	@Override
	public EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution {
		final GameId key = new GameId(request.getGameId());
		if (sessions.containsKey(key)) {
			Games.LOGGER.debug("endGameSession: Ending the game session for gameId " + request.getGameId());
			kill(request.getGameId());
		} else {
			Games.LOGGER.debug("endGameSession: This instance does not contain the gameId " + request.getGameId()
					+ ". Redirecting your request to the correct deployment.");
			CreateGameSessionResponse connection = connections.get(key);
			if (connection == null) {
				Games.LOGGER.error("endGameSession: No gameId " + key.toString() + " was found to be ended. Aborting.");
				return new EndGameSessionResponse();
			}

			Rpc.connect(Games.class).sync(connection.deploymentId).endGameSession(request);
		}
		return new EndGameSessionResponse();
	}

	@Override
	public UpdateEntityResponse updateEntity(UpdateEntityRequest request) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PerformGameActionResponse performGameAction(PerformGameActionRequest request) throws InterruptedException, SuspendExecution {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConcedeGameSessionResponse concedeGameSession(ConcedeGameSessionRequest request) throws InterruptedException, SuspendExecution {
		final GameId key = new GameId(request.getGameId());
		if (sessions.containsKey(key)) {
			Games.LOGGER.debug("concedeGameSession: Conceding game for gameId " + request.getGameId());
			kill(request.getGameId());
		} else {
			Games.LOGGER.debug("concedeGameSession: This instance does not contain the gameId " + request.getGameId()
					+ ". Redirecting your request to the correct deployment.");
			CreateGameSessionResponse connection = connections.get(key);
			if (connection == null) {
				Games.LOGGER.error("concedeGameSession: No gameId " + key.toString() + " was found to be ended. Aborting.");
				return new ConcedeGameSessionResponse();
			}

			Rpc.connect(Games.class).sync(connection.deploymentId).concedeGameSession(request);
		}
		return new ConcedeGameSessionResponse();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		Games.LOGGER.debug("stop: Stopping the ClusteredGamesImpl.");
		super.stop();
		Rpc.unregister(registration);
		for (ActivityMonitor monitor : gameActivityMonitors.values()) {
			monitor.cancel();
		}
		gameActivityMonitors.clear();
		Games.LOGGER.debug("stop: Activity monitors unregistered");
		for (GameId gameId : sessions.keySet()) {
			kill(gameId.toString());
		}
		Games.LOGGER.debug("stop: Sessions killed");
	}
}
