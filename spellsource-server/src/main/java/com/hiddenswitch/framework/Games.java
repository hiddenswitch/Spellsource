package com.hiddenswitch.framework;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.hiddenswitch.framework.impl.*;
import com.hiddenswitch.framework.rpc.VertxGamesGrpcServer;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.server.GrpcServerRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * A service that starts a game session, accepts connections from players and manages the state of the game.
 */
public class Games {
	public static final long DEFAULT_NO_ACTIVITY_TIMEOUT = 225000L;
	public static final String GAMES = "games";
	public static final Comparator<net.demilich.metastone.game.entities.Entity> ENTITY_NATURAL_ORDER = Comparator
			.comparing(net.demilich.metastone.game.entities.Entity::getZone)
			.thenComparingInt(net.demilich.metastone.game.entities.Entity::getIndex);
	public static final String GAMES_CREATE_GAME_SESSION = "games:createGameSession";
	public static final String ADDRESS_IS_IN_GAME = "games:isInGame:";
	public static final String ADDRESS_CONCEDE_GAME = "games:concede:";
	public static final String WRITER_ADDRESS_PREFIX = "games:writer:";
	public static final String READER_ADDRESS_PREFIX = "games:reader:";
	/** Lazily hosts a persisted single-player match. Internal cluster API. */
	public static final String GAMES_RESTORE_GAME = "games:restoreGame";
	private static final Logger LOGGER = LoggerFactory.getLogger(Games.class);
	public static final String GAMES_DEFAULT_NO_ACTIVITY_TIMEOUT = "games.defaultNoActivityTimeout";

	@NotNull
	public static String getMessagesFromClientAddress(String userId) {
		return READER_ADDRESS_PREFIX + userId;
	}

	@NotNull
	public static String getMessagesFromServerAddress(String userId) {
		return WRITER_ADDRESS_PREFIX + userId;
	}

	/**
	 * Binds a bidirectional gRPC game stream for {@code userId} to the clustered EventBus addresses
	 * {@code games:reader:{userId}} and {@code games:writer:{userId}}.
	 */
	public static void subscribeGame(String userId, GrpcServerRequest<Spellsource.ClientToServerMessage, Spellsource.ServerToClientMessage> request, WriteStream<Spellsource.ServerToClientMessage> response) {
		var vertx = Vertx.currentContext().owner();
		var eventBus = vertx.eventBus();
		var keepAliveManager = Environment.keepAliveManager(request.connection(), v -> {
			try {
				request.end();
			} catch (IllegalStateException ignored) {
			}
		}, true);
		keepAliveManager.onTransportStarted();
		var publisher = eventBus.<Spellsource.ClientToServerMessage>publisher(getMessagesFromClientAddress(userId));
		var retryPublisher = new RetryMessageProducer<>(publisher, 50, 100, body -> body.getMessageType() == Spellsource.MessageTypeMessage.MessageType.FIRST_MESSAGE);
		var consumer = eventBus.<Spellsource.ServerToClientMessage>consumer(getMessagesFromServerAddress(userId));
		var consumerReady = consumer.completion().otherwiseEmpty();
		request.handler(body -> {
			keepAliveManager.onDataReceived();
			consumerReady.onComplete(v -> retryPublisher.write(body));
		});
		request.endHandler(v -> {
			keepAliveManager.onTransportTermination();
			retryPublisher.close();
			consumer.unregister();
		});
		request.exceptionHandler(t -> {
			keepAliveManager.onTransportTermination();
			retryPublisher.close();
			consumer.unregister();
		});
		consumer.bodyStream().pipe().endOnComplete(false).endOnFailure(false).to(response);
		consumerReady.onComplete(v -> request.resume());
	}

	/**
	 * Creates a match without entering a queue entry between two users.
	 *
	 * @param request All the required information to create a game.
	 * @return Connection information for both users.
	 */
	public static Future<CreateGameSessionResponse> createGame(ConfigurationRequest request) {
		CodecRegistration.register(CreateGameSessionResponse.class).andRegister(ConfigurationRequest.class);
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.<CreateGameSessionResponse>request(GAMES_CREATE_GAME_SESSION, request).map(Message::body);
	}

	/**
	 * Specifies the number of milliseconds to wait for players to connect to a {@link ServerGameContext} that was just
	 * created.
	 */
	public static long getDefaultConnectionTime() {
		return 8000L;
	}

	public static Future<String> getGameId(@NotNull String userId) {
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.<String>request(ADDRESS_IS_IN_GAME + userId, "", new DeliveryOptions().setSendTimeout(100L))
				.otherwiseEmpty()
				.compose(res -> res != null
						? Future.succeededFuture(res.body())
						: eb.<String>request(GAMES_RESTORE_GAME, userId).map(Message::body).otherwiseEmpty())
				.map(res -> res != null ? res : null);
	}

	/**
	 * Concedes the game currently hosted for {@code userId}.
	 */
	public static Future<Boolean> concedeGame(@NotNull String userId) {
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.<Boolean>request(ADDRESS_CONCEDE_GAME + userId, "")
				.map(Message::body);
	}

	public static long getDefaultNoActivityTimeout() {
		return Long.parseLong(System.getProperties().getProperty(GAMES_DEFAULT_NO_ACTIVITY_TIMEOUT, Long.toString(Games.DEFAULT_NO_ACTIVITY_TIMEOUT)));
	}

	public static BindAll<VertxGamesGrpcServer.GamesApi> services() {
		return new VertxGamesGrpcServer.GamesApi() {
			@Override
			public Future<StringValue> isInMatch(GrpcServerRequest<Empty, StringValue> grpcServerRequest, Empty request) {
				var userId = grpcServerRequest.routingContext().user().subject();
				if (userId == null) {
					return Future.succeededFuture(StringValue.of(""));
				}
				return Games.getGameId(userId)
						.map(res -> StringValue.newBuilder()
								.setValue(res == null ? "" : res)
								.build());
			}
		}::bindAll;
	}
}
