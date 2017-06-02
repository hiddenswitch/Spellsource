package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.Matchmaking;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.common.Client;
import com.hiddenswitch.proto3.net.common.ClientToServerMessage;
import com.hiddenswitch.proto3.net.impl.server.GameSession;
import com.hiddenswitch.proto3.net.impl.server.SocketClient;
import com.hiddenswitch.proto3.net.impl.server.GameSessionImpl;
import com.hiddenswitch.proto3.net.impl.server.WebSocketClient;
import com.hiddenswitch.proto3.net.impl.util.ActivityMonitor;
import com.hiddenswitch.proto3.net.impl.util.ServerGameContext;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.RPC;
import com.hiddenswitch.proto3.net.util.IncomingMessage;
import com.hiddenswitch.proto3.net.util.Serialization;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.netty.channel.DefaultChannelId;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.hiddenswitch.proto3.net.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.fiberHandler;

public class GamesImpl extends AbstractService<GamesImpl> implements Games {
	private Logger logger = LoggerFactory.getLogger(GamesImpl.class);

	private static final long CLEANUP_DELAY_MILLISECONDS = 500L;

	private final Map<String, GameSession> games = new HashMap<>();
	private final Map<String, GameSession> gameForUserId = new HashMap<>();
	private final Map<Object, GameSession> gameForSocket = new HashMap<>();
	private final Map<Object, IncomingMessage> netMessages = new HashMap<>();
	private final Map<String, ActivityMonitor> gameActivityMonitors = new HashMap<>();
	private final Map<String, String> keyToSecret = new HashMap<>();

	private NetServer server;
	private HttpServer websocketServer;
	private final int legacyPort;
	private final int websocketPort;

	private RpcClient<Matchmaking> matchmaking;

	public GamesImpl() {
		this.legacyPort = RandomUtils.nextInt(6200, 8080);
		// Skip 8080
		this.websocketPort = RandomUtils.nextInt(8081, 16200);
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
		matchmaking = RPC.connect(Matchmaking.class, vertx.eventBus());

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

		ignored = awaitResult(then -> {
			server = vertx.createNetServer();

			server.connectHandler(socket -> {
				socket.handler(fiberHandler(messageBuffer -> {
					handleLegacySocketMessage(socket, messageBuffer);
				}));
			});

			server.listen(getLegacyPort(), getHost(), listenResult -> {
				if (!listenResult.succeeded()) {
					logger.error("Failure deploying socket listener: {}", listenResult.cause());
					then.handle(Future.failedFuture(listenResult.cause()));
				} else {
					then.handle(Future.succeededFuture());
				}
			});
		});

		logger.debug("GamesImpl::start Created socket server.");

		HttpServer listenResult = awaitResult(then -> {
			websocketServer = vertx.createHttpServer(new HttpServerOptions()
					.setPort(websocketPort)
					.setMaxWebsocketFrameSize(1500));

			websocketServer.websocketHandler(socket -> {
				if (!socket.uri().startsWith("/" + Games.WEBSOCKET_PATH)) {
					throw new RuntimeException();
				}

				socket.handler(fiberHandler(messageBuffer -> {
					handleWebSocketMessage(socket, messageBuffer);
				}));
			});

			websocketServer.listen(then);
		});

		logger.debug("GamesImpl::start Created websocket server.");

		RPC.register(this, Games.class, vertx.eventBus());

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

		GameSessionImpl session = new GameSessionImpl(getHost(), getLegacyPort(), getWebsocketPort(), request.getPregame1(), request.getPregame2(), request.getGameId(), getVertx(), request.getNoActivityTimeout());
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
	 * The legacy message handler. Handles TCP clients using the Java serialization-based TCP API.
	 * <p>
	 * Messages are of the format {8x magic bytes}, followed by a {4xbyte 32bit content length}, followed by the
	 * content. {@link IncomingMessage} is responsible for concatenating the network data until a complete message is
	 * received. Once it is received, the message is deserialized to a {@link ClientToServerMessage} and is processed.
	 *
	 * @param socket        The socket corresponding to the server-client connection.
	 * @param messageBuffer The buffer containing data from the client. It may be part of a message.
	 */
	@Suspendable
	private void handleLegacySocketMessage(NetSocket socket, Buffer messageBuffer) {
		logger.trace("Getting buffer from socket with hashCode {} length {}. Incoming message count: {}", socket.hashCode(), messageBuffer.length(), netMessages.size());
		// Do we have a reader for this socket?
		ClientToServerMessage message = null;
		int bytesRead = 0;
		Buffer remainder = null;
		if (!netMessages.containsKey(socket)) {
			try {
				IncomingMessage firstMessage = new IncomingMessage(messageBuffer);
				netMessages.put(socket, firstMessage);
				bytesRead = firstMessage.getBufferWithoutHeader().length() + IncomingMessage.HEADER_SIZE;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		} else {
			bytesRead = netMessages.get(socket).append(messageBuffer);
		}

		IncomingMessage incomingMessage = netMessages.get(socket);

		if (!incomingMessage.isComplete()) {
			return;
		}

		// If there appears to be data left over after finishing the message, hold onto the remainder of the buffer.
		if (bytesRead < messageBuffer.length()) {
			logger.trace("Some remainder of a message was found. Bytes read: {}, remainder: {}", bytesRead, messageBuffer.length() - bytesRead);
			remainder = messageBuffer.getBuffer(bytesRead, messageBuffer.length());
		}

		try {
			message = Serialization.deserialize(incomingMessage.getBufferWithoutHeader().getBytes());
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Deserializing the message failed!", e);
		} catch (Exception e) {
			logger.error("A different deserialization error occurred!", e);
		} finally {
			netMessages.remove(socket);
		}

		logger.trace("IncomingMessage complete on socket {}, expectedLength {} actual {}", socket.hashCode(), incomingMessage.getExpectedLength(), incomingMessage.getBufferWithoutHeader().length());

		if (message == null) {
			return;
		}

		GameSession session = null;
		if (message.getGameId() != null) {
			session = getGames().get(message.getGameId());
		} else {
			session = gameForSocket.get(socket);
		}

		// Show activity on the game activity monitor
		if (session == null) {
			logger.error("Received a message from a client for a game session that is killed.");
			logger.error("Message: " + message.toString());
			return;
		}

		keepAlive(session);

		switch (message.getMt()) {
			case FIRST_MESSAGE:
				logger.debug("First message received from {}", message.getPlayer1().toString());
				Client client = new SocketClient(socket);
				gameForSocket.put(client.getPrivateSocket(), session);
				// Is this a reconnect?
				if (session.isGameReady()) {
					// TODO: Remove references to the old socket
					// Replace the client
					session.onPlayerReconnected(message.getPlayer1(), client);
				} else {
					logger.debug("Calling onPlayerConnected for {}, {}", toString(), message.getPlayer1().toString());
					session.onPlayerConnected(message.getPlayer1(), client);
				}
				break;
			case UPDATE_ACTION:
				logger.debug("Server received message with ID {} action {}", message.getId(), message.getAction());
				session.onActionReceived(message.getId(), message.getAction());
				break;

			case UPDATE_MULLIGAN:
				session.onMulliganReceived(message.getId(), message.getPlayer1(), message.getDiscardedCards());
				break;
		}

		if (remainder != null) {
			handleLegacySocketMessage(socket, remainder);
		}
	}

	/**
	 * The Unity Websocket client message handler.
	 * <p>
	 * A complete message is received and processed here. Users can only send 3 kinds of messages: <ul> <li>{@link
	 * com.hiddenswitch.proto3.net.client.models.MessageType#FIRST_MESSAGE}: The first message from the client which
	 * authenticates it to the server. </li> <li>{@link com.hiddenswitch.proto3.net.client.models.MessageType#UPDATE_ACTION}:
	 * The client has chosen an action and it is packed into this message.</li> <li>{@link
	 * com.hiddenswitch.proto3.net.client.models.MessageType#UPDATE_MULLIGAN}: The client has chosen which cards to
	 * mulligan.</li> </ul>
	 * <p>
	 * All messages are deserialized into {@link com.hiddenswitch.proto3.net.client.models.ClientToServerMessage}
	 * messages as generated by the Swagger specification.
	 *
	 * @param socket        The {@link ServerWebSocket} that represents the client's connection.
	 * @param messageBuffer The message received from the client.
	 */
	@Suspendable
	private void handleWebSocketMessage(ServerWebSocket socket, Buffer messageBuffer) {
		com.hiddenswitch.proto3.net.client.models.ClientToServerMessage message =
				Configuration.getDefaultApiClient().getJSON().deserialize(messageBuffer.toString(),
						com.hiddenswitch.proto3.net.client.models.ClientToServerMessage.class);

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
		Void r = awaitResult(h -> server.close(h));
		r = awaitResult(h -> websocketServer.close(h));
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
				netMessages.remove(s);
			}
		});

		// Expire the match
		removeFromMatchmaker(gameId);

		// Remove the game session
		games.remove(gameId);
	}

	/**
	 * Gets the port that the legacy game services are hosted at. Typically randomly generated.
	 *
	 * @return The port number.
	 */
	private int getLegacyPort() {
		return legacyPort;
	}

	/**
	 * The host that this server is running on.
	 * <p>
	 * Originally, this function would return the publicly-accessible DNS name of this host. But the hostname lookup
	 * is broken, mysteriously, on Oracle Java 8 in Vert.x, and it hangs.
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
		vertx.setTimer(CLEANUP_DELAY_MILLISECONDS, suspendableHandler(t -> kill(gameOverId)));
	}

	/**
	 * Gets the port that the Unity WebSocket API is running on.
	 *
	 * @return The port number. Typically randomly generated.
	 */
	private int getWebsocketPort() {
		return websocketPort;
	}
}
