package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Matchmaking;
import com.hiddenswitch.spellsource.Port;
import com.hiddenswitch.spellsource.impl.server.GameSession;
import com.hiddenswitch.spellsource.impl.server.GameSessionImpl;
import com.hiddenswitch.spellsource.impl.server.SessionWriter;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.Pump;
import net.demilich.metastone.game.cards.CardCatalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusteredGamesImpl extends AbstractService<ClusteredGamesImpl> implements Games {
	public static final String READER_ADDRESS_PREFIX = "ClusteredGamesImpl/";
	private Registration registration;
	private Map<GameId, CreateGameSessionResponse> connections;
	private Map<GameId, GameSession> sessions = new HashMap<>();
	private Map<GameId, List<MessageConsumer<Buffer>>> pipes = new HashMap<>();
	private Map<GameId, ActivityMonitor> gameActivityMonitors = new HashMap<>();

	@Override
	public void start() throws SuspendExecution {
		super.start();
		CardCatalogue.loadCardsFromPackage();

		connections = Games.getConnections(vertx);
		registration = Rpc.register(this, Games.class, vertx.eventBus());
	}

	@Override
	public CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException {
		final String gameId = request.getGameId();

		if (gameId == null) {
			throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
		}

		final GameId key = new GameId(gameId);
		CreateGameSessionResponse connection = connections.putIfAbsent(key, CreateGameSessionResponse.pending(deploymentID()));
		// If we're the ones deploying this match...
		if (connection == null) {
			GameSession session = new GameSessionImpl(Gateway.getHostAddress(),
					Port.port(),
					request.getPregame1(),
					request.getPregame2(),
					gameId,
					vertx,
					Games.getDefaultNoActivityTimeout());

			// Deal with ending the game
			session.handleGameOver(this::onGameOver);

			// Listen for messages from the clients
			final EventBus eventBus = vertx.eventBus();
			final ActivityMonitor activityMonitor = new ActivityMonitor(vertx, gameId, request.getNoActivityTimeout(), this::kill);
			final ArrayList<MessageConsumer<Buffer>> pipeList = new ArrayList<>();
			pipeList.add(connect(session, 0, request.getPregame1().getUserId(), eventBus, activityMonitor));
			pipeList.add(connect(session, 1, request.getPregame2().getUserId(), eventBus, activityMonitor));
			pipes.put(key, pipeList);
			gameActivityMonitors.put(key, activityMonitor);
			final CreateGameSessionResponse response = CreateGameSessionResponse.session(deploymentID(), session);
			connections.put(key, response);
			sessions.put(key, session);
			return response;
		} else {
			// Otherwise, return its state, whatever it is
			return connection;
		}
	}

	@Suspendable
	private void onGameOver(GameSessionImpl session) {
		final String gameOverId = session.getGameId();
		vertx.setTimer(500L, Sync.suspendableHandler(t -> kill(gameOverId)));
	}

	private MessageConsumer<Buffer> connect(GameSession session, int playerId, String userId, EventBus eventBus, ActivityMonitor activityMonitor) {
		SessionWriter writer1 = new SessionWriter(userId, playerId, eventBus, session, activityMonitor);
		MessageConsumer<Buffer> reader1 = eventBus.consumer(READER_ADDRESS_PREFIX + userId);
		Pump.pump(reader1.bodyStream(), writer1).start();
		return reader1;
	}

	@Suspendable
	private void kill(String gameId) throws InterruptedException, SuspendExecution {
		try {
			Rpc.connect(Matchmaking.class, vertx.eventBus()).sync().expireOrEndMatch(new MatchExpireRequest(gameId));
		} catch (VertxException noHandler) {
			// TODO: What would be the most sensible solution here?
		}
		final GameId key = new GameId(gameId);
		GameSession session = sessions.get(key);
		if (session != null) {
			session.kill();
		}
		connections.remove(key);
		if (pipes.containsKey(key)) {
			for (MessageConsumer<Buffer> consumer : pipes.get(key)) {
				consumer.unregister();
			}
			pipes.remove(key);
		}

	}

	@Override
	public DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request) {
		return new DescribeGameSessionResponse();
	}

	@Override
	public EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution {
		kill(request.getGameId());
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
		kill(request.getGameId());
		return new ConcedeGameSessionResponse();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
		for (ActivityMonitor monitor : gameActivityMonitors.values()) {
			monitor.cancel();
		}
		gameActivityMonitors.clear();
		for (GameId gameId : sessions.keySet()) {
			kill(gameId.toString());
		}
	}
}
