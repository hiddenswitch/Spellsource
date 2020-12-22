package com.hiddenswitch.framework;

import co.paralleluniverse.fibers.Suspendable;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.hiddenswitch.framework.impl.*;
import com.hiddenswitch.framework.rpc.VertxGamesGrpc;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.rpc.*;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import net.demilich.metastone.game.GameContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.sync.Sync.await;


/**
 * A service that starts a game session, accepts connections from players and manages the state of the game.
 * <p>
 * Various static methods convert game data into a format the Unity3D client can understand.
 */
public class Games {
	private static final Logger LOGGER = LoggerFactory.getLogger(Games.class);
	public static final long DEFAULT_NO_ACTIVITY_TIMEOUT = 225000L;
	public static final String GAMES = "games";
	public static final Comparator<net.demilich.metastone.game.entities.Entity> ENTITY_NATURAL_ORDER = Comparator
			.comparing(net.demilich.metastone.game.entities.Entity::getZone)
			.thenComparingInt(net.demilich.metastone.game.entities.Entity::getIndex);
	public static final String GAMES_CREATE_GAME_SESSION = "Games.createGameSession";

	/**
	 * Creates a match without entering a queue entry between two users.
	 *
	 * @param request All the required information to create a game.
	 * @return Connection information for both users.
	 */
	public static Future<MatchCreateResponse> createGame(ConfigurationRequest request) {
		LOGGER.debug("createMatch: Creating match for request {}", request);
		CodecRegistration.register(CreateGameSessionResponse.class).andRegister(MatchCreateResponse.class).andRegister(ConfigurationRequest.class);
		var eb = Vertx.currentContext().owner().eventBus();

		return eb.<CreateGameSessionResponse>request(GAMES_CREATE_GAME_SESSION, request, new DeliveryOptions()
				.setSendTimeout(8000L))
				.map(response -> new MatchCreateResponse(response.body()));
	}


	/**
	 * Specifies the number of milliseconds to wait for players to connect to a {@link ServerGameContext} that was just
	 * created.
	 *
	 * @return
	 */
	public static long getDefaultConnectionTime() {
		return 12000L;
	}


	static Future<String> getGameId(@NotNull String userId) {
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.<String>request(userId + ".isInGame", "", new DeliveryOptions().setSendTimeout(100L))
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

	public static Future<ServerServiceDefinition> services() {
		return Future.succeededFuture(new VertxGamesGrpc.GamesVertxImplBase() {
			@Override
			public Future<StringValue> isInMatch(Empty request) {
				var userId = Accounts.userId();
				if (userId == null) {
					return Future.succeededFuture(StringValue.of(""));
				}
				return Games.getGameId(userId)
						.map(res -> StringValue.newBuilder()
								.setValue(res == null ? "" : res)
								.build());
			}
		}).compose(Accounts::requiresAuthorization);
	}
}
