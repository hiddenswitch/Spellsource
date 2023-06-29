package com.hiddenswitch.framework.impl;

import com.google.common.collect.Sets;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.rpc.Spellsource.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import io.github.jklingsporn.vertx.jooq.shared.postgres.JSONToJsonObjectConverter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.logic.GameStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.hiddenswitch.framework.schema.keycloak.Keycloak.KEYCLOAK;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;

public class ClusteredGames extends AbstractVirtualThreadVerticle {
	private static final Counter GAMES_CREATED = Counter.builder("games.created")
			.description("The number of games created.")
			.baseUnit(BaseUnits.EVENTS)
			.register(globalRegistry);
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredGames.class);
	private final Map<String, ServerGameContext> contexts = new ConcurrentHashMap<>();
	private MessageConsumer<?> registration;
	private final SqlCachedCardCatalogue cardCatalogue = new SqlCachedCardCatalogue();


	@Override
	public void startVirtual() throws Exception {
		CodecRegistration.register(ServerToClientMessage.getDefaultInstance())
				.andRegister(ClientToServerMessage.getDefaultInstance());
		var eb = Vertx.currentContext().owner().eventBus();
		registration = eb.<ConfigurationRequest>consumer(Games.GAMES_CREATE_GAME_SESSION, request ->
				createGameSession(request.body()).onSuccess(request::reply).onFailure(t -> request.fail(-1, t.getMessage())));

		var registrationFut = Promise.<Void>promise();
		cardCatalogue.invalidateAllAndRefresh();
		await(cardCatalogue.subscribe());

		// should we wait for registration to finish?
		registration.completionHandler(v -> {
			if (v.succeeded()) {
				registrationFut.complete();
			} else if (v.failed()) {
				registrationFut.fail(v.cause());
			}
		});

		await(registrationFut.future());
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
							var deck = ModelConversions.getGameDeck(userId, deckCollection, cardCatalogue);

							configuration.setDeck(deck);

							// Add all the attributes that were specified in the deck collection
							// Implements Signature
							for (var tuple : deckCollection.getCollection().getPlayerEntityAttributesList()) {
								playerAttributes.put(Attribute.valueOf(tuple.getAttribute().name()), tuple.getStringValue());
							}
							LOGGER.trace("retrieved deck for userId {}", userId);
							return Future.succeededFuture();
						});
			}

			var configurationFut = deckCollectionFut
					.compose(v -> Environment.withExecutor(queryExecutor -> queryExecutor.findOneRow(dsl -> dsl.select(KEYCLOAK.USER_ENTITY.USERNAME).from(KEYCLOAK.USER_ENTITY)
									.where(KEYCLOAK.USER_ENTITY.ID.eq(userId))))
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
					LOGGER.trace("loading player configurations for request {}", request);
					var serverContextVerticle = new AbstractVirtualThreadVerticle() {
						private ServerGameContext serverGameContext;

						@Override
						public void startVirtual() {
							this.serverGameContext = new ServerGameContext(
									request.getGameId(),
									new VertxScheduler(),
									request.getConfigurations(),
									cardCatalogue,
									ClusteredGames.this,
									this);

							contexts.put(request.getGameId(), serverGameContext);
							// Plays the game context in its own fiber
							await(serverGameContext.handlersReady());
							serverGameContext.play(true);
						}

						@Override
						public void stopVirtual() {
							var thread = serverGameContext.getThread();
							if (thread != null && thread.isAlive() && !thread.isInterrupted() && !serverGameContext.isGameOver()) {
								thread.interrupt();
							}
						}
					};
					return vertx.deployVerticle(serverContextVerticle)
							.map(deploymentId -> CreateGameSessionResponse.session(deploymentID(), serverContextVerticle.serverGameContext));
				})
				.onFailure(Environment.onFailure())
				.compose(response -> {
					GAMES_CREATED.increment();
					LOGGER.trace("handlers are ready for request {}", request);
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
	public void removeGameAndRecordReplay(@NotNull String gameId) {
		Objects.requireNonNull(gameId);
		if (!Thread.currentThread().isVirtual()) {
			throw new UnsupportedOperationException("expected to be in virtual thread");
		}

		var gameIdLong = Long.parseLong(gameId);
		var gameContext = contexts.remove(gameId);
		if (gameContext == null) {
			return;
		}

		gameContext.updateAndGetGameOver();
		String winner = null;
		if (gameContext.getWinner() != null && gameContext.getWinner().getUserId() != null) {
			winner = gameContext.getWinner().getUserId();
		}
		// Save the wins/losses
		if (winner != null) {
			var userIdLoser = gameContext.getOpponent(gameContext.getWinner()).getUserId();

			var pTrace = JSONToJsonObjectConverter.getInstance().to(gameContext.getTrace().toJson());
			await(Environment.callRoutine(Routines.clusteredGamesUpdateGameAndUsers(winner, userIdLoser, gameIdLong, pTrace)));
		}

		if (gameContext.getStatus() == GameStatus.RUNNING) {
			gameContext.loseBothPlayers();
		}

	}

	@Override
	public void stopVirtual() {
		var keys = Sets.newCopyOnWriteArraySet(contexts.keySet());
		LOGGER.trace("stop: Stopping the ClusteredGames, hosting contexts: {}", keys.stream().map(String::toString).reduce((s1, s2) -> s1 + ", " + s2).orElseGet(() -> "none"));

		for (var context : contexts.values()) {
			if (context.isRunning()) {
				LOGGER.warn("stop: Game gameId={} was still running when stop was called.", context.getGameId());
			}
		}

		registration.unregister();
		try {
			for (var gameId : keys) {
				Objects.requireNonNull(gameId);
				removeGameAndRecordReplay(gameId);
			}
		} finally {
			if (!contexts.isEmpty()) {
				LOGGER.warn("failed to close all contexts");
			}
		}
		LOGGER.trace("stop: Unregistered");
	}
}
