package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.*;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Matchmaking;
import com.hiddenswitch.spellsource.Port;
import com.hiddenswitch.spellsource.impl.server.GameSession;
import com.hiddenswitch.spellsource.impl.server.GameSessionImpl;
import com.hiddenswitch.spellsource.impl.server.SessionWriter;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.Pump;
import net.demilich.metastone.game.cards.CardCatalogue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClusteredGamesImpl extends AbstractService<ClusteredGamesImpl> implements Games {
	public static final String READER_ADDRESS_PREFIX = "ClusteredGamesImpl/";
	private Registration registration;
	private SuspendableMap<GameId, CreateGameSessionResponse> connections;
	private Map<GameId, GameSession> sessions = new ConcurrentHashMap<>();
	private Map<GameId, List<Runnable>> pipeClosers = new ConcurrentHashMap<>();
	private ListMultimap<GameId, ActivityMonitor> gameActivityMonitors = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

	@Override
	public void start() throws SuspendExecution {
		super.start();
		CardCatalogue.loadCardsFromPackage();

		connections = Games.getConnections(vertx);
		registration = Rpc.register(this, Games.class, vertx.eventBus());
	}

	@Override
	public CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException {
		if (logger.isDebugEnabled()) {
			logger.debug("createGameSession: Creating game session for request " + request.toString());
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
			logger.debug("createGameSession: DeploymentId " + deploymentID() + " is responsible for deploying this match.");
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
			logger.debug("createGameSession: Repeat createGameSessionRequest suspected because actually deploymentId " + connection.deploymentId + " is responsible for deploying this match.");
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
		logger.debug("onGameOver: Handling on game over for session " + session.getGameId());
		final String gameOverId = session.getGameId();
		// The players should not accidentally wind back up in games
		vertx.setTimer(500L, Sync.suspendableHandler(t -> kill(gameOverId)));
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
		logger.debug("connect: Connecting userId " + userId + " with gameId " + session.getGameId());
		final SessionWriter writer = new SessionWriter(userId, playerId, eventBus, session, activityMonitors);
		final MessageConsumer<Buffer> reader = eventBus.consumer(READER_ADDRESS_PREFIX + userId);
		final Pump pipe = Pump.pump(reader.bodyStream(), writer, Integer.MAX_VALUE).start();
		return () -> {
			pipe.stop();
			writer.end();
			reader.unregister();
			logger.debug("connect: Closing writing pipe for userId " + userId);
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
			logger.debug("kill: This deployment with deploymentId " + deploymentID() + " does not contain the gameId "
					+ gameId);
			return;
		}
		logger.debug("kill: Calling kill for gameId " + gameId);
		GameSession session = sessions.remove(key);
		CreateGameSessionResponse connection = connections.remove(key);

		final MatchExpireRequest request = new MatchExpireRequest(gameId);
		request.users = Arrays.asList(connection.userId1, connection.userId2);
		if (request.users.size() < 2
				|| request.users.get(0) == null
				|| request.users.get(1) == null) {
			throw new IllegalArgumentException("No users were returned correctly by the session.");
		}

		try {
			Rpc.connect(Matchmaking.class, vertx.eventBus()).sync().expireOrEndMatch(request);
		} catch (VertxException noHandlerFound) {
			logger.error("kill: For gameId " + gameId + ", an error occurred trying to expireOrEndMatch: " + noHandlerFound.getMessage());
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
			logger.debug("describeGameSession: Describing gameId " + request.getGameId());
			return DescribeGameSessionResponse.fromGameContext(sessions.get(key).getGameContext());
		} else {
			logger.debug("describeGameSession: This game session does not contain the gameId " + request.getGameId());
			return new DescribeGameSessionResponse();
		}
	}

	@Override
	public EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution {
		final GameId key = new GameId(request.getGameId());
		if (sessions.containsKey(key)) {
			logger.debug("endGameSession: Ending the game session for gameId " + request.getGameId());
			kill(request.getGameId());
		} else {
			logger.debug("endGameSession: This instance does not contain the gameId " + request.getGameId()
					+ ". Redirecting your request to the correct deployment.");
			CreateGameSessionResponse connection = connections.get(key);
			if (connection == null) {
				logger.error("endGameSession: No gameId " + key.toString() + " was found to be ended. Aborting.");
				return new EndGameSessionResponse();
			}

			Rpc.connect(Games.class, vertx.eventBus()).sync(connection.deploymentId).endGameSession(request);
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
			logger.debug("concedeGameSession: Conceding game for gameId " + request.getGameId());
			kill(request.getGameId());
		} else {
			logger.debug("concedeGameSession: This instance does not contain the gameId " + request.getGameId()
					+ ". Redirecting your request to the correct deployment.");
			CreateGameSessionResponse connection = connections.get(key);
			if (connection == null) {
				logger.error("concedeGameSession: No gameId " + key.toString() + " was found to be ended. Aborting.");
				return new ConcedeGameSessionResponse();
			}

			Rpc.connect(Games.class, vertx.eventBus()).sync(connection.deploymentId).concedeGameSession(request);
		}
		return new ConcedeGameSessionResponse();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		logger.debug("stop: Stopping the ClusteredGamesImpl.");
		super.stop();
		Rpc.unregister(registration);
		for (ActivityMonitor monitor : gameActivityMonitors.values()) {
			monitor.cancel();
		}
		gameActivityMonitors.clear();
		logger.debug("stop: Activity monitors unregistered");
		for (GameId gameId : sessions.keySet()) {
			kill(gameId.toString());
		}
		logger.debug("stop: Sessions killed");
	}
}
