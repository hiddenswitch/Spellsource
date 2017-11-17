package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.Matchmaking;
import com.hiddenswitch.spellsource.Port;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.client.Configuration;
import com.hiddenswitch.spellsource.common.Client;
import com.hiddenswitch.spellsource.impl.server.GameSession;
import com.hiddenswitch.spellsource.impl.server.GameSessionImpl;
import com.hiddenswitch.spellsource.impl.server.WebSocketClient;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.client.models.MessageType;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.netty.channel.DefaultChannelId;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.fiberHandler;

public class GamesImpl extends AbstractService<GamesImpl> implements Games {
	private Logger logger = LoggerFactory.getLogger(GamesImpl.class);

	private static final long CLEANUP_DELAY_MILLISECONDS = 500L;

	private final Map<String, GameSession> games = new HashMap<>();
	private final Map<String, GameSession> gameForUserId = new HashMap<>();
	private final Map<Object, GameSession> gameForSocket = new HashMap<>();
	private final Map<String, ActivityMonitor> gameActivityMonitors = new HashMap<>();
	private final Map<String, String> keyToSecret = new HashMap<>();

	private RpcClient<Matchmaking> matchmaking;
	private Registration registration;

	public GamesImpl() {
	}

	/**
	 * Starts the Games service.
	 * <p>
	 * This implementation will load all the cards; create a legacy TCP socket service for Java clients; a Unity
	 * WebSocket compatible service for Unity clients, and then broadcast itself on the cluster to be available for
	 * use.
	 *
	 * @throws SuspendExecution
	 */
	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		matchmaking = Rpc.connect(Matchmaking.class, vertx.eventBus());

		Void ignored = awaitResult(h -> vertx.executeBlocking(blocking -> {
			try {
				CardCatalogue.loadCardsFromPackage();
			} catch (IOException | URISyntaxException | CardParseException e) {
				blocking.fail(e);
				return;
			}

			DefaultChannelId.newInstance();
			// TODO: These ports shouldn't be totally randomized because of AWS security groups
			blocking.complete();
		}, then -> {
			h.handle(Future.succeededFuture());
		}));

		logger.debug("GamesImpl::start Loaded cards.");


		// TODO: Until expire game session is registered correctly, limit this service to a singleton.
		if (noInstancesYet()) {
			Spellsource.spellsource().router(vertx).route("/" + Games.WEBSOCKET_PATH)
					.method(HttpMethod.GET)
					.handler(context -> {
						ServerWebSocket socket = context.request().upgrade();
						socket.handler(fiberHandler(message -> {
							handleWebSocketMessage(socket, message);
						}));
					});

			registration = Rpc.register(this, Games.class, vertx.eventBus());
		}

		logger.debug("GamesImpl::start Registered on event bus.");
	}

	/**
	 * Gets the {@link ServerGameContext} that corresponds to the given game ID.
	 *
	 * @param gameId The game to look up.
	 * @return The game context, or {@code null} if this {@link Games} instance doesn't have it.
	 */
	public ServerGameContext getGameContext(String gameId) {
		GameSession session = this.getGames().get(gameId);
		if (session == null) {
			return null;
		}
		return session.getGameContext();
	}

	/**
	 * Gets the {@link GameSession} that corresponds to the given game ID.
	 *
	 * @param gameId The game to look up.
	 * @return The game session, or {@code null} if this {@link Games} instance doesn't have it.
	 */
	public GameSession getGameSession(String gameId) {
		return this
				.getGames()
				.getOrDefault(gameId, null);
	}

	@Override
	@Suspendable
	public ContainsGameSessionResponse containsGameSession(ContainsGameSessionRequest request) throws SuspendExecution, InterruptedException {
		return new ContainsGameSessionResponse(this.getGames().containsKey(request.gameId));
	}

	@Override
	@Suspendable
	public CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException {
		if (request.getGameId() == null) {
			throw new RuntimeException("Game ID cannot be null in a create game session request.");
		}

		GameSessionImpl session = new GameSessionImpl(getHost(), Port.port(), request.getPregame1(), request.getPregame2(), request.getGameId(), getVertx(), request.getNoActivityTimeout());
		session.handleGameOver(this::onGameOver);
		final String finalGameId = session.getGameId();
		games.put(finalGameId, session);
		for (String userId : new String[]{request.getPregame1().getUserId(), request.getPregame2().getUserId()}) {
			if (userId != null) {
				gameForUserId.put(userId, session);
				keyToSecret.put(userId, session.getSecret(userId));
			}
		}

		// If the game has no activity after a certain amount of time, kill it automatically.
		gameActivityMonitors.put(finalGameId, new ActivityMonitor(vertx, finalGameId, request.getNoActivityTimeout(), this::kill));

		return new CreateGameSessionResponse(session.getConfigurationForPlayer1(), session.getConfigurationForPlayer2(), session.getGameId());
	}

	@Override
	public DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request) {
		return DescribeGameSessionResponse.fromGameContext(getGameContext(request.getGameId()));
	}

	/**
	 * The Unity Websocket client message handler.
	 * <p>
	 * A complete message is received and processed here. Users can only send 3 kinds of messages: <ul> <li>{@link
	 * MessageType#FIRST_MESSAGE}: The first message from the client which authenticates it to the server. </li>
	 * <li>{@link MessageType#UPDATE_ACTION}: The client has chosen an action and it is packed into this message.</li>
	 * <li>{@link MessageType#UPDATE_MULLIGAN}: The client has chosen which cards to mulligan.</li> </ul>
	 * <p>
	 * All messages are deserialized into {@link com.hiddenswitch.spellsource.client.models.ClientToServerMessage}
	 * messages as generated by the Swagger specification.
	 *
	 * @param socket        The {@link ServerWebSocket} that represents the client's connection.
	 * @param messageBuffer The message received from the client.
	 */
	@Suspendable
	public void handleWebSocketMessage(ServerWebSocket socket, Buffer messageBuffer) {
		com.hiddenswitch.spellsource.client.models.ClientToServerMessage message =
				Configuration.getDefaultApiClient().getJSON().deserialize(messageBuffer.toString(),
						com.hiddenswitch.spellsource.client.models.ClientToServerMessage.class);

		GameSession session = gameForSocket.getOrDefault(socket, null);

		if (session != null) {
			keepAlive(session);
		}

		switch (message.getMessageType()) {
			case FIRST_MESSAGE:
				// Make sure this connection is authorized
				final String userId = message.getFirstMessage().getPlayerKey();
				String secret = message.getFirstMessage().getPlayerSecret();
				if (!keyToSecret.containsKey(userId) ||
						!keyToSecret.get(userId).equals(secret)) {
					throw new RuntimeException("Invalid secret.");
				}

				if (session == null) {
					session = gameForUserId.get(userId);
				}

				keepAlive(session);

				Client client = new WebSocketClient(socket, userId, session.getPlayer(userId).getId());
				gameForSocket.put(client.getPrivateSocket(), session);

				if (session.isGameReady()) {
					// TODO: Remove references to the old socket
					// Replace the client
					session.onPlayerReconnected(session.getPlayer(userId), client);
				} else {
					session.onPlayerConnected(session.getPlayer(userId), client);
				}
				break;
			case UPDATE_ACTION:
				if (session == null) {
					throw new RuntimeException();
				}
				final String messageId = message.getRepliesTo();
				session.onActionReceived(messageId, message.getActionIndex());
				break;
			case UPDATE_MULLIGAN:
				if (session == null) {
					throw new RuntimeException();
				}
				final String messageId2 = message.getRepliesTo();
				session.onMulliganReceived(messageId2, message.getDiscardedCardIndices());
				break;
			case EMOTE:
				if (session == null) {
					break;
				}
				session.onEmote(message.getEmote().getEntityId(), message.getEmote().getMessage());
				break;
			case TOUCH:
				if (session == null) {
					break;
				}
				if (null != message.getEntityTouch()) {
					session.onTouch(session.getPlayerIdForSocket(socket), message.getEntityTouch());
				} else if (null != message.getEntityUntouch()) {
					session.onUntouch(session.getPlayerIdForSocket(socket), message.getEntityUntouch());
				}
				break;
			case CONCEDE:
				if (session == null) {
					break;
				}
				session.onConcede(session.getPlayerIdForSocket(socket));
				break;
		}
	}

	/**
	 * Updates an {@link ActivityMonitor} that a message was received from a client, and thus the {@link GameSession} is
	 * still alive.
	 *
	 * @param session The session that has activity.
	 */
	private void keepAlive(GameSession session) {
		String gameId = session.getGameId();
		ActivityMonitor activityMonitor = gameActivityMonitors.get(gameId);
		if (activityMonitor == null) {
			gameActivityMonitors.put(gameId, new ActivityMonitor(getVertx(), gameId, session.getNoActivityTimeout(), this::kill));
			activityMonitor = gameActivityMonitors.get(gameId);
		}
		activityMonitor.activity();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		getGames().values().forEach(GameSession::kill);
		if (registration != null) {
			Rpc.unregister(registration);
			freeSingleton();
		}
	}

	@Override
	public EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution {
		if (request.getGameId() == null) {
			throw new RuntimeException("Game ID cannot be null in an end game session request.");
		}

		this.kill(request.getGameId());

		return new EndGameSessionResponse();
	}

	@Override
	@Suspendable
	public UpdateEntityResponse updateEntity(UpdateEntityRequest request) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public PerformGameActionResponse performGameAction(PerformGameActionRequest request) throws InterruptedException, SuspendExecution {
		if (request.getGameId() == null) {
			throw new RuntimeException("Game ID cannot be null in a perform game action request.");
		}

		final ServerGameContext gameContext = getGameContext(request.getGameId());

		// Merge entities if they're defined
		if (request.getEntities() != null) {
			for (Entity entity : request.getEntities()) {
				final EntityLocation location = entity.getEntityLocation();
				entity.setEntityLocation(EntityLocation.UNASSIGNED);
				entity.moveOrAddTo(gameContext, location.getZone());
			}
		}

		gameContext.getLogic().performGameAction(request.getPlayerId(), request.getAction());

		PerformGameActionResponse response = new PerformGameActionResponse();

		response.setState(gameContext.getGameStateCopy());
		return response;
	}

	@Override
	public ConcedeGameSessionResponse concedeGameSession(ConcedeGameSessionRequest request) throws InterruptedException, SuspendExecution {
		// TODO: Actually do something special when the player concedes
		kill(request.getGameId());
		return new ConcedeGameSessionResponse();
	}

	/**
	 * Kills the specified game by ID.
	 *
	 * @param gameId The ID of the game to abruptly end.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	private void kill(String gameId) throws SuspendExecution, InterruptedException {
		GameSession session = games.get(gameId);

		if (session == null) {
			// The session was already removed
			return;
		}

		// Clear out the activity monitors
		gameActivityMonitors.get(gameId).cancel();
		gameActivityMonitors.remove(gameId);

		// Get the sockets associated with the session
		List<Object> sockets = new ArrayList<>();
		if (session.getClient1() != null) {
			sockets.add(session.getClient1().getPrivateSocket());
		}
		if (session.getClient2() != null) {
			sockets.add(session.getClient2().getPrivateSocket());
		}

		// Kill the session
		session.kill();

		// Clear our maps of these sockets
		sockets.forEach(s -> {
			if (s != null) {
				gameForSocket.remove(s);
			}
		});

		// Expire the match
		removeFromMatchmaker(gameId);

		// Remove the game session
		games.remove(gameId);
	}

	/**
	 * The host that this server is running on.
	 * <p>
	 * Originally, this function would return the publicly-accessible DNS name of this host. But the hostname lookup is
	 * broken, mysteriously, on Oracle Java 8 in Vert.x, and it hangs.
	 *
	 * @return {@code "0.0.0.0"} because the hosts are not aware of their publicly-accessible host names yet.
	 */
	private String getHost() {
		// TODO: Look up this host correctly
		return "0.0.0.0";
	}

	/**
	 * Gets a map of the games by game ID.
	 *
	 * @return The game sessions map.
	 */
	private Map<String, GameSession> getGames() {
		return Collections.unmodifiableMap(games);
	}

	/**
	 * Notifies the matchmaker that the game with the specified ID has expired and that, consequently, the player can
	 * start a new game.
	 *
	 * @param gameId The game ID of the game to expire in the matchmaker.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	private void removeFromMatchmaker(String gameId) throws InterruptedException, SuspendExecution {
		try {
			matchmaking.sync().expireOrEndMatch(new MatchExpireRequest(gameId));
		} catch (VertxException vertxException) {
			// If the matchmaking service isn't visible, don't sweat it.
			if (vertxException.getCause() instanceof ReplyException) {
				if (((ReplyException) vertxException.getCause()).failureType() == ReplyFailure.NO_HANDLERS) {
					return;
				}
			}

			throw vertxException;
		}
	}

	/**
	 * When a game session ends, this handler sets a timer to kill the game.
	 *
	 * @param session The session whose game ended naturally.
	 */
	@Suspendable
	private void onGameOver(GameSession session) {
		final String gameOverId = session.getGameId();
		vertx.setTimer(CLEANUP_DELAY_MILLISECONDS, Sync.suspendableHandler(t -> kill(gameOverId)));
	}
}
