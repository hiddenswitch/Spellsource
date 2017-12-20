package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Matchmaking;
import com.hiddenswitch.spellsource.Port;
import com.hiddenswitch.spellsource.impl.server.EventBusWriter;
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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import net.demilich.metastone.game.cards.CardCatalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusteredGamesImpl extends AbstractService<ClusteredGamesImpl> implements Games {
	public static final String READER_ADDRESS_PREFIX = "ClusteredGamesImpl/";
	private Registration registration;
	private Map<String, CreateGameSessionResponse> connections;
	private Map<String, GameSession> sessions = new HashMap<>();
	private Map<String, List<MessageConsumer<Buffer>>> pipes = new HashMap<>();

	@Override
	public void start() throws SuspendExecution {
		super.start();

		CardCatalogue.loadCardsFromPackage();

		if (vertx.isClustered()) {
			connections = SharedData.getClusterWideMap("ClusteredGamesImpl/connections", vertx.sharedData());
		} else {
			connections = vertx.sharedData().getLocalMap("ClusteredGamesImpl/connections");
		}

		registration = Rpc.register(this, Games.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public ContainsGameSessionResponse containsGameSession(ContainsGameSessionRequest request) throws SuspendExecution, InterruptedException {
		final String gameId = request.gameId;
		CreateGameSessionResponse response = connections.getOrDefault(gameId, null);
		if (response != null && response.getDeploymentId().equals(deploymentID())) {
			return new ContainsGameSessionResponse(true);
		} else {
			return new ContainsGameSessionResponse(false);
		}
	}

	@Override
	public CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException {
		final String gameId = request.getGameId();

		if (gameId == null) {
			throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
		}

		CreateGameSessionResponse connection = connections.putIfAbsent(gameId, CreateGameSessionResponse.pending(deploymentID()));
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
			pipes.put(gameId, pipeList);
			Map<String, ActivityMonitor> gameActivityMonitors = new HashMap<>();
			gameActivityMonitors.put(gameId, activityMonitor);
			final CreateGameSessionResponse response = CreateGameSessionResponse.session(deploymentID(), session);
			connections.put(session.getGameId(), response);
			sessions.put(session.getGameId(), session);
			return response;
		} else {
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
		GameSession session = sessions.get(gameId);
		session.kill();
		connections.remove(gameId);
		if (pipes.containsKey(gameId)) {
			for (MessageConsumer<Buffer> consumer : pipes.get(gameId)) {
				consumer.unregister();
			}
			pipes.remove(gameId);
		}

	}

	@Override
	public DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request) {
		return null;
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
		throw new UnsupportedOperationException();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
		for (String gameId : sessions.keySet()) {
			kill(gameId);
		}
	}

	public static void configureWebsocketHandler(Router router, EventBus bus) {
		final AuthHandler authHandler = new SpellsourceAuthHandler(bus);

		router.route("/" + Games.WEBSOCKET_PATH + "-clustered")
				.method(HttpMethod.GET)
				.handler(authHandler);

		router.route("/" + Games.WEBSOCKET_PATH + "-clustered")
				.method(HttpMethod.GET)
				.handler(context -> {
					final ServerWebSocket socket = context.request().upgrade();
					final String userId = context.user().principal().getString("_id");
					final MessageConsumer<Buffer> consumer = bus.<Buffer>consumer(EventBusWriter.WRITER_ADDRESS_PREFIX + userId);
					final Pump pump1 = Pump.pump(socket, bus.publisher(READER_ADDRESS_PREFIX + userId)).start();
					Pump.pump(consumer.bodyStream(), socket).start();
					socket.closeHandler(disconnected -> {
						pump1.stop();
						consumer.unregister();
					});
				});
	}
}
