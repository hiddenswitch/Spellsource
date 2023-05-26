package com.hiddenswitch.framework.impl;

import com.google.common.collect.Sets;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum;
import com.hiddenswitch.spellsource.rpc.Spellsource.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.logic.GameStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.hiddenswitch.framework.schema.keycloak.Keycloak.KEYCLOAK;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAME_USERS;
import static io.micrometer.core.instrument.Metrics.globalRegistry;

public class ClusteredGames extends AbstractVerticle {
	private static final Counter GAMES_CREATED = Counter.builder("games.created")
			.description("The number of games created.")
			.baseUnit(BaseUnits.EVENTS)
			.register(globalRegistry);
	private static final Counter GAMES_FINISHED = Counter.builder("games.finished")
			.description("The number of games finished.")
			.baseUnit(BaseUnits.EVENTS)
			.register(globalRegistry);
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredGames.class);
	private Map<String, ServerGameContext> contexts = new ConcurrentHashMap<>();
	private MessageConsumer<?> registration;


	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		// TODO: deal with this elsewhere
		ClasspathCardCatalogue.CLASSPATH.loadCardsFromPackage();
		CodecRegistration.register(ServerToClientMessage.getDefaultInstance())
				.andRegister(ClientToServerMessage.getDefaultInstance());
		var eb = Vertx.currentContext().owner().eventBus();
		registration = eb.<ConfigurationRequest>consumer(Games.GAMES_CREATE_GAME_SESSION, request ->
				createGameSession(request.body()).onSuccess(request::reply).onFailure(t -> request.fail(-1, t.getMessage())));

		// should we wait for registration to finish?
		registration.completionHandler(v -> startPromise.complete());
	}

	public Future<CreateGameSessionResponse> createGameSession(ConfigurationRequest request) {
		LOGGER.trace("createGameSession: creating game session for request {}", request);

		if (request.getGameId() == null) {
			throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
		}

		// Loads persistence game triggers that implement legacy functionality. In other words, ensures that game contexts
		// will contain the event-listening enchantments that interact with network services like the database to store
		// stuff about cards.
		// Logic.triggers();
		// Get the collection data from the configurations that are not yet populated with valid cards
		var playerConfigurations = new ArrayList<Future>();
		for (var configuration : request.getConfigurations()) {
			var playerAttributes = new AttributeMap();

			var deckId = configuration.getDeck().getDeckId();
			var userId = configuration.getUserId();
			var deckCollectionFut = Future.succeededFuture();
			if (configuration.getDeck() instanceof CollectionDeck) {
				deckCollectionFut = Legacy.getDeck(deckId, userId)
						.compose(deckCollection -> {

							// Create the deck and assign all the appropriate IDs to the cards
							var deck = ModelConversions.getGameDeck(userId, deckCollection);

							configuration.setDeck(deck);

							// Add all the attributes that were specified in the deck collection
							// Implements Signature
							for (var tuple : deckCollection.getCollection().getPlayerEntityAttributesList()) {
								playerAttributes.put(Attribute.valueOf(tuple.getAttribute().name()), tuple.getStringValue());
							}

							return Future.succeededFuture();
						});
			}

			var configurationFut = deckCollectionFut
					.compose(v -> Environment.withExecutor(queryExecutor -> queryExecutor.findOneRow(dsl -> {
								return dsl.select(KEYCLOAK.USER_ENTITY.USERNAME).from(KEYCLOAK.USER_ENTITY)
										.where(KEYCLOAK.USER_ENTITY.ID.eq(userId));
							}))
							.map(usernameRow -> usernameRow.getString(0))
							.compose(username -> {
								configuration.setName(username);
								// TODO: Get more attributes from database
								playerAttributes.put(Attribute.NAME, username);
								playerAttributes.put(Attribute.USER_ID, userId);
								playerAttributes.put(Attribute.DECK_ID, deckId);

								configuration.setPlayerAttributes(playerAttributes);

								return Future.succeededFuture(configuration);
							}));

			playerConfigurations.add(configurationFut);
		}

		return CompositeFuture.all(playerConfigurations)
				.compose(v -> {
					var context = new ServerGameContext(
							request.getGameId(),
							new VertxScheduler(Vertx.currentContext().owner()),
							request.getConfigurations());

					// Deal with ending the game
					context.addEndGameHandler(session -> {
						GAMES_FINISHED.increment();
						try {
							var gameOverId = session.getGameId();
							removeGameAndRecordReplay(gameOverId);
						} catch (Throwable t) {
							LOGGER.warn("could not record game because", t);
						}
					});

					var response = CreateGameSessionResponse.session(deploymentID(), context);
					contexts.put(request.getGameId(), context);
					// Plays the game context in its own fiber
					context.play(true);
					return context
							.handlersReady()
							.map(response);
				}).compose(response -> {
					GAMES_CREATED.increment();
					return Future.succeededFuture(response);
				});

	}

	/**
	 * Handles a game that ends by any means.
	 * <p>
	 * Records metadata, like wins and losses.
	 *
	 * @param gameId
	 */
	private Future<Void> removeGameAndRecordReplay(@NotNull String gameId) {
		Objects.requireNonNull(gameId);
		if (!Thread.currentThread().isVirtual()) {
			throw new UnsupportedOperationException("expected to be in virtual thread");
		}

		var gameIdLong = Long.parseLong(gameId);
		var gameContext = contexts.remove(gameId);
		if (gameContext == null) {
			return Future.succeededFuture();
		}

		return Environment.withExecutor(executor -> {
			gameContext.updateAndGetGameOver();
			String winner = null;
			if (gameContext.getWinner() != null && gameContext.getWinner().getUserId() != null) {
				winner = gameContext.getWinner().getUserId();
			}
			// Save the wins/losses
			if (winner != null) {
				var userIdWinner = winner;
				var userIdLoser = gameContext.getOpponent(gameContext.getWinner()).getUserId();
				return CompositeFuture.all(
						executor.execute(dsl -> dsl.update(GAME_USERS)
								.set(GAME_USERS.VICTORY_STATUS, GameUserVictoryEnum.WON)
								.where(GAME_USERS.USER_ID.eq(userIdWinner), GAME_USERS.GAME_ID.eq(gameIdLong))),
						executor.execute(dsl -> dsl.update(GAME_USERS)
								.set(GAME_USERS.VICTORY_STATUS, GameUserVictoryEnum.LOST)
								.where(GAME_USERS.USER_ID.eq(userIdLoser), GAME_USERS.GAME_ID.eq(gameIdLong))),
						executor.execute(dsl -> dsl.update(Tables.GAMES)
								.set(Tables.GAMES.STATUS, GameStateEnum.FINISHED)
								.set(Tables.GAMES.TRACE, gameContext.getTrace().toJson())
								.where(Tables.GAMES.ID.eq(gameIdLong)))).map((Void) null);
			}
			return Future.succeededFuture();
		}).recover(t -> {
			if (gameContext.getStatus() == GameStatus.RUNNING) {
				var async = Environment.async();
				async.run(v -> gameContext.loseBothPlayers());
			}
			return Future.succeededFuture(null);
		}).onFailure(Environment.onFailure());
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		var keys = Sets.newCopyOnWriteArraySet(contexts.keySet());
		LOGGER.trace("stop: Stopping the ClusteredGames, hosting contexts: {}", keys.stream().map(String::toString).reduce((s1, s2) -> s1 + ", " + s2).orElseGet(() -> "none"));
		var async = Environment.async();

		for (var context : contexts.values()) {
			if (context.isRunning()) {
				LOGGER.warn("stop: Game gameId={} was still running when stop was called.", context.getGameId());
			}
		}

		registration.unregister();

		async.run(v -> {
			try {
				for (var gameId : keys) {
					Objects.requireNonNull(gameId);
					removeGameAndRecordReplay(gameId);
				}
			} finally {
				if (!contexts.isEmpty()) {
					LOGGER.warn("failed to close all contexts");
				}
				stopPromise.complete();
			}
		});

		LOGGER.trace("stop: Unregistered");
	}
}
