package com.hiddenswitch.framework;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.hiddenswitch.framework.impl.*;
import com.hiddenswitch.framework.rpc.VertxGamesGrpcServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.grpc.server.GrpcServerRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;


/**
 * A service that starts a game session, accepts connections from players and manages the state of the game.
 * <p>
 * Various static methods convert game data into a format the Unity3D client can understand.
 */
public class Games {
	public static final long DEFAULT_NO_ACTIVITY_TIMEOUT = 225000L;
	public static final String GAMES = "games";
	public static final Comparator<net.demilich.metastone.game.entities.Entity> ENTITY_NATURAL_ORDER = Comparator
			.comparing(net.demilich.metastone.game.entities.Entity::getZone)
			.thenComparingInt(net.demilich.metastone.game.entities.Entity::getIndex);
	public static final String GAMES_CREATE_GAME_SESSION = "games:createGameSession";
	public static final String ADDRESS_IS_IN_GAME = "games:isInGame:";
	private static final Logger LOGGER = LoggerFactory.getLogger(Games.class);

	/**
	 * Creates a match without entering a queue entry between two users.
	 *
	 * @param request All the required information to create a game.
	 * @return Connection information for both users.
	 */
	public static Future<CreateGameSessionResponse> createGame(ConfigurationRequest request) {
		CodecRegistration.register(CreateGameSessionResponse.class).andRegister(MatchCreateResponse.class).andRegister(ConfigurationRequest.class);
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.<CreateGameSessionResponse>request(GAMES_CREATE_GAME_SESSION, request).map(Message::body);
	}


	/**
	 * Specifies the number of milliseconds to wait for players to connect to a {@link ServerGameContext} that was just
	 * created.
	 *
	 * @return
	 */
	public static long getDefaultConnectionTime() {
		return 8000L;
	}


	public static Future<String> getGameId(@NotNull String userId) {
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.<String>request(ADDRESS_IS_IN_GAME + userId, "", new DeliveryOptions().setSendTimeout(100L))
				.otherwiseEmpty()
				.map(res -> res != null ? res.body() : null);
	}

	/**
	 * Gets the default no activity timeout as configured across the cluster. This timeout is used to determine when to
	 * end games that have received no actions from either client connected to them.
	 *
	 * @return A value in milliseconds of how long to wait for an action from a client before marking a game as over due
	 * to disconnection.
	 */
	public static long getDefaultNoActivityTimeout() {
		return Long.parseLong(System.getProperties().getProperty("games.defaultNoActivityTimeout", Long.toString(Games.DEFAULT_NO_ACTIVITY_TIMEOUT)));
	}

	public static BindAll<VertxGamesGrpcServer.GamesApi> services() {
		return new VertxGamesGrpcServer.GamesApi() {
			@Override
			public Future<StringValue> isInMatch(GrpcServerRequest<Empty, StringValue> grpcServerRequest, Empty request) {
				var userId = Gateway.ROUTING_CONTEXT.get().user().subject();
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
