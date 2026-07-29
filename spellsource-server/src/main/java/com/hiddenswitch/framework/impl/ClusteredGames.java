package com.hiddenswitch.framework.impl;

import com.google.common.collect.Sets;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.rpc.Spellsource.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import io.github.jklingsporn.vertx.jooq.shared.postgres.JSONToJsonObjectConverter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.logic.GameStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.demilich.metastone.game.logic.Trace;

import static com.hiddenswitch.framework.schema.keycloak.Keycloak.KEYCLOAK;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAMES;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAME_USERS;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;

public class ClusteredGames extends AbstractVirtualThreadVerticle {
	private static final Counter GAMES_CREATED = Counter.builder("games.created")
			.description("The number of games created.")
			.baseUnit(BaseUnits.EVENTS)
			.register(globalRegistry);
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredGames.class);
	private final Map<String, ServerGameContext> contexts = new ConcurrentHashMap<>();
	private final Map<String, Future<String>> restoring = new ConcurrentHashMap<>();
	private final SqlCachedCardCatalogue cardCatalogue = new SqlCachedCardCatalogue();
	private MessageConsumer<?> registration;
	private MessageConsumer<?> restoreRegistration;

	@Override
	public void startVirtual() throws Exception {
		CodecRegistration.register(ServerToClientMessage.getDefaultInstance())
				.andRegister(ClientToServerMessage.getDefaultInstance());
		var eb = Vertx.currentContext().owner().eventBus();
		registration = eb.<ConfigurationRequest>consumer(Games.GAMES_CREATE_GAME_SESSION, request ->
				createGameSession(request.body()).onSuccess(request::reply).onFailure(t -> request.fail(-1, t.getMessage())));
		restoreRegistration = eb.<String>consumer(Games.GAMES_RESTORE_GAME, request ->
			restoreGame(request.body()).onSuccess(request::reply).onFailure(t -> request.fail(-1, t.getMessage())));

		var registrationFut = Promise.<Void>promise();
		cardCatalogue.invalidateAllAndRefresh();
		await(cardCatalogue.subscribe());

		// should we wait for registration to finish?
		registration.completion().onComplete(v -> {
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
		var playerConfigurations = new ArrayList<Future<?>>();
		var resumable = request.getConfigurations().stream().filter(Configuration::isBot).count() == 1
				&& request.getConfigurations().size() == 2;
		for (var configuration : request.getConfigurations()) {
			if (resumable && !configuration.isBot()) {
				// A single-player context remains hosted while its client is absent.
				configuration.setNoActivityTimeout(0L);
			}
			var playerAttributes = new AttributeMap();

			var deckId = configuration.getDeck().getDeckId();
			var userId = configuration.getUserId();
			var deckCollectionFut = Future.succeededFuture();
			if (configuration.getDeck() instanceof CollectionDeck) {
				deckCollectionFut = Legacy.getDeck(cardCatalogue, deckId, userId)
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
					.compose(_ -> Environment.withExecutor(queryExecutor -> queryExecutor.findOneRow(dsl -> dsl.select(KEYCLOAK.USER_ENTITY.USERNAME).from(KEYCLOAK.USER_ENTITY)
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
			
			RogueManager.handleGameStart(deckId, Long.parseLong(request.getGameId()));
		}

		return Future.all(playerConfigurations)
				.compose(_ -> {
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
					return vertx.deployVerticle(serverContextVerticle, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
							.map(deploymentId -> CreateGameSessionResponse.session(deploymentID(), serverContextVerticle.serverGameContext));
				})
				.onFailure(Environment.onFailure())
				.compose(response -> {
					GAMES_CREATED.increment();
					LOGGER.trace("handlers are ready for request {}", request);
					return Future.succeededFuture(response);
				});
	}

	/** Finds and reconstructs the latest checkpointed one-human/one-bot game for a returning player. */
	private Future<String> restoreGame(String userId) {
		var hosted = contexts.values().stream()
				.filter(context -> context.getPlayerConfigurations().stream().anyMatch(c -> !c.isBot() && userId.equals(c.getUserId())))
				.findFirst();
		if (hosted.isPresent()) {
			return Future.succeededFuture(hosted.get().updateAndGetGameOver() ? null : hosted.get().getGameId());
		}
		return Environment.query(dsl -> dsl.select(GAMES.ID, GAMES.TRACE, GAME_USERS.PLAYER_INDEX, GAME_USERS.DECK_ID)
				.from(GAMES).join(GAME_USERS).on(GAME_USERS.GAME_ID.eq(GAMES.ID))
				.where(GAME_USERS.USER_ID.eq(userId))
				.and(GAMES.STATUS.eq(GameStateEnum.STARTED))
				.and(GAMES.TRACE.isNotNull())
				.and(GAME_USERS.VICTORY_STATUS.eq(com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum.UNKNOWN))
				.and(GAME_USERS.GAME_ID.in(dsl.select(GAME_USERS.GAME_ID).from(GAME_USERS)
						.groupBy(GAME_USERS.GAME_ID).having(org.jooq.impl.DSL.count().eq(1))))
				.orderBy(GAMES.CREATED_AT.desc()).limit(1))
				.compose(rows -> {
					var iterator = rows.iterator();
					if (!iterator.hasNext()) return Future.succeededFuture(null);
					var row = iterator.next();
					var id = row.getLong("id");
					try {
						var trace = Trace.load(row.getJsonObject("trace").encode());
						if (trace.getMulligans() == null) throw new IllegalArgumentException("trace has no completed mulligans");
						return restoring.computeIfAbsent(id.toString(), ignored -> {
							var future = createResumedSession(id.toString(), userId, row.getShort("player_index"), row.getString("deck_id"), trace)
									.map(CreateGameSessionResponse::getGameId);
							future.onComplete(done -> restoring.remove(id.toString()));
							return future;
						});
					} catch (Throwable cause) {
						LOGGER.warn("Cannot restore persisted game {} for {}; abandoning it", id, userId, cause);
						return abandonUnrestorableGame(id, userId).map((String) null);
					}
				});
	}

	private Future<Void> abandonUnrestorableGame(long gameId, String userId) {
		return Environment.withDslContext(dsl -> dsl.update(GAME_USERS)
				.set(GAME_USERS.VICTORY_STATUS, com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum.LOST)
				.where(GAME_USERS.GAME_ID.eq(gameId)).and(GAME_USERS.USER_ID.eq(userId)))
				.compose(ignored -> Environment.withDslContext(dsl -> dsl.update(GAMES)
						.set(GAMES.STATUS, com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum.FINISHED)
						.where(GAMES.ID.eq(gameId))))
				.compose(ignored -> RogueManager.handleGameEnd(gameId, "abandoned-bot-" + gameId).mapEmpty());
	}

	private Future<CreateGameSessionResponse> createResumedSession(String gameId, String userId, short humanIndex, String humanDeckId, Trace trace) {
		try {
			var replayed = trace.replayContext(false, null, cardCatalogue);
			var configurations = new ArrayList<Configuration>();
			for (var index = 0; index < 2; index++) {
				var deck = new GameDeck(cardCatalogue, trace.getHeroClasses().get(index), trace.getDeckCardIds().get(index).getCardIds());
				if (index == humanIndex) deck.setDeckId(humanDeckId);
				configurations.add(new Configuration().setPlayerId(index)
						.setUserId(index == humanIndex ? userId : "resumed-bot-" + gameId)
						.setBot(index != humanIndex).setNoActivityTimeout(index == humanIndex ? 0L : Games.getDefaultNoActivityTimeout())
						.setDeck(deck));
			}
			var serverContextVerticle = new AbstractVirtualThreadVerticle() {
				private ServerGameContext serverGameContext;
				@Override public void startVirtual() {
					serverGameContext = new ServerGameContext(gameId, new VertxScheduler(), configurations, cardCatalogue, ClusteredGames.this, this);
					serverGameContext.restoreFromTrace(trace);
					contexts.put(gameId, serverGameContext);
					await(serverGameContext.handlersReady());
					serverGameContext.play(true);
				}
				@Override public void stopVirtual() { if (serverGameContext != null && serverGameContext.getThread() != null) serverGameContext.getThread().interrupt(); }
			};
			return vertx.deployVerticle(serverContextVerticle, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
					.map(deploymentId -> CreateGameSessionResponse.session(deploymentID(), serverContextVerticle.serverGameContext));
		} catch (Throwable cause) {
			LOGGER.warn("Cannot replay persisted game {}", gameId, cause);
			return abandonUnrestorableGame(Long.parseLong(gameId), userId).compose(ignored -> Future.failedFuture(cause));
		}
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
			var updated = await(Environment.callRoutine(Routines.clusteredGamesUpdateGameAndUsers(winner, userIdLoser, gameIdLong, pTrace)).execute());
			if (!Boolean.TRUE.equals(updated)) {
				throw new IllegalStateException("failed to persist terminal state for gameId=" + gameId);
			}
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
		restoreRegistration.unregister();
		try {
			for (var gameId : keys) {
				Objects.requireNonNull(gameId);
				var context = contexts.get(gameId);
				if (context != null && context.getPlayerConfigurations().stream().filter(Configuration::isBot).count() == 1) {
					context.checkpoint();
					contexts.remove(gameId);
					if (context.getThread() != null) context.getThread().interrupt();
				} else {
					removeGameAndRecordReplay(gameId);
				}
			}
		} finally {
			if (!contexts.isEmpty()) {
				LOGGER.warn("failed to close all contexts");
			}
		}
		LOGGER.trace("stop: Unregistered");
	}
}
