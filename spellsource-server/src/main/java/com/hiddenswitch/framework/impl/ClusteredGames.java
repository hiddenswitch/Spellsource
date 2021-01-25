package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.base.Throwables;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.keycloak.tables.daos.UserEntityDao;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.rpc.Spellsource.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.logic.GameStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.framework.schema.spellsource.Tables.GAME_USERS;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;

public class ClusteredGames extends SyncVerticle {
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
	protected void syncStart() throws SuspendExecution {
		// TODO: deal with this elsewhere
		CardCatalogue.loadCardsFromPackage();
		CodecRegistration.register(ServerToClientMessage.getDefaultInstance())
				.andRegister(ClientToServerMessage.getDefaultInstance());
		var eb = Vertx.currentContext().owner().eventBus();
		registration = eb.<ConfigurationRequest>consumer(Games.GAMES_CREATE_GAME_SESSION, request -> {
			var body = request.body();

			fiber(() -> createGameSession(body))
					.onSuccess(request::reply)
					.onFailure(t -> request.fail(-1, t.getMessage()));
		});
	}

	public Map<String, ServerGameContext> getContexts() {
		return Collections.unmodifiableMap(contexts);
	}

	public CreateGameSessionResponse createGameSession(ConfigurationRequest request) throws SuspendExecution, InterruptedException {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ClusteredGames/createGameSession")
				.asChildOf(request.getSpanContext())
				.start();
		try (var s1 = tracer.activateSpan(span)) {
			span.log(JsonObject.mapFrom(request).getMap());
			LOGGER.debug("createGameSession: Creating game session for request " + request.toString());

			if (request.getGameId() == null) {
				throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
			}

			// Loads persistence game triggers that implement legacy functionality. In other words, ensures that game contexts
			// will contain the event-listening enchantments that interact with network services like the database to store
			// stuff about cards.
			// Logic.triggers();
			// Get the collection data from the configurations that are not yet populated with valid cards
			for (var configuration : request.getConfigurations()) {
				var playerAttributes = new AttributeMap();

				var deckId = configuration.getDeck().getDeckId();
				var userId = configuration.getUserId();
				if (configuration.getDeck() instanceof CollectionDeck) {
					var deckCollection = await(Legacy.getDeck(deckId, userId));

					// Create the deck and assign all the appropriate IDs to the cards
					var deck = ModelConversions.getGameDeck(userId, deckCollection);

					configuration.setDeck(deck);

					// Add all the attributes that were specified in the deck collection
					// Implements Signature
					for (var tuple : deckCollection.getCollection().getPlayerEntityAttributesList()) {
						playerAttributes.put(Attribute.valueOf(tuple.getAttribute().name()), tuple.getStringValue());
					}
				}

				var userEntitiesDao = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
				var userRecord = await(userEntitiesDao.findOneById(userId));
				var username = userRecord.getUsername();
				configuration.setName(username);
				// TODO: Get more attributes from database
				playerAttributes.put(Attribute.NAME, username);
				playerAttributes.put(Attribute.USER_ID, userId);
				playerAttributes.put(Attribute.DECK_ID, deckId);

				configuration.setPlayerAttributes(playerAttributes);
			}

			var context = new ServerGameContext(
					request.getGameId(),
					new VertxScheduler(Vertx.currentContext().owner()),
					request.getConfigurations());

			// Enable tracing
			context.setSpanContext(span.context());

			try {
				// Deal with ending the game
				context.addEndGameHandler(session -> {
					GAMES_FINISHED.increment();
					// Do not record replays if we're interrupting
					if (Strand.currentStrand().isInterrupted()) {
						return;
					}
					var gameOverId = session.getGameId();
					// The players should not accidentally wind back up in games
					removeGameAndRecordReplay(gameOverId);
				});

				var response = CreateGameSessionResponse.session(deploymentID(), context);
				contexts.put(request.getGameId(), context);
				// Plays the game context in its own fiber
				context.play(true);
				span.log("awaitingRegistration");
				await(context.handlersReady());
				GAMES_CREATED.increment();
				return response;
			} catch (RuntimeException any) {
				if (Throwables.getRootCause(any) instanceof TimeoutException) {
					span.log("timeout");
				}
				Tracing.error(any, span, true);
				throw any;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			span.finish();
		}
	}

	/**
	 * Handles a game that ends by any means.
	 * <p>
	 * Records metadata, like wins and losses.
	 *
	 * @param gameId
	 * @throws SuspendExecution
	 */
	@Suspendable
	private void removeGameAndRecordReplay(@NotNull String gameId) throws SuspendExecution {
		Objects.requireNonNull(gameId);
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ClusteredGames/removeGameAndRecordReplay")
				.asChildOf(tracer.activeSpan())
				.withTag("gameId", gameId)
				.start();
		var scope = tracer.activateSpan(span);
		var gameIdLong = Long.parseLong(gameId);

		try {
			if (!contexts.containsKey(gameId)) {
				LOGGER.debug("removeGameAndRecordReplay {}: This deployment with deploymentId {} does not contain the gameId, or this game has already been ended", gameId, deploymentID());
				return;
			}
			LOGGER.debug("removeGameAndRecordReplay {}", gameId);
			var gameContext = contexts.remove(gameId);

			String winner = null;

			var executor = Environment.queryExecutor();
			try {
				var isGameOver = gameContext.updateAndGetGameOver();
				LOGGER.debug("removeGameAndRecordReplay: updateAndGetGameOver={}", isGameOver);
				if (gameContext.getWinner() != null && gameContext.getWinner().getUserId() != null) {
					winner = gameContext.getWinner().getUserId();
				}
				// Save the wins/losses
				if (winner != null) {
					var userIdWinner = winner;
					var userIdLoser = gameContext.getOpponent(gameContext.getWinner()).getUserId();
					if (!Strand.currentStrand().isInterrupted()) {
						await(executor
								.execute(dsl -> dsl.update(GAME_USERS)
										.set(GAME_USERS.VICTORY_STATUS, GameUserVictoryEnum.WON)
										.where(GAME_USERS.USER_ID.eq(userIdWinner), GAME_USERS.GAME_ID.eq(gameIdLong))));
						await(executor
								.execute(dsl -> dsl.update(GAME_USERS)
										.set(GAME_USERS.VICTORY_STATUS, GameUserVictoryEnum.LOST)
										.where(GAME_USERS.USER_ID.eq(userIdLoser), GAME_USERS.GAME_ID.eq(gameIdLong))));
					}
				}
				// If the game is still running when this is called, make sure to force end the game
			} finally {
				if (gameContext.getStatus() == GameStatus.RUNNING) {
					gameContext.loseBothPlayers();
				}
			}

			// Presence is automatically updated by a timer
			try {
				var saveSpan = tracer.buildSpan("ClusteredGames/removeGameAndRecordReplay/saveReplay")
						.asChildOf(span)
						.start();
				var scope2 = tracer.activateSpan(saveSpan);
				try {
					if (!Strand.currentStrand().isInterrupted()) {
						// kick this off, but don't wait
						await(executor.execute(dsl -> dsl.update(Tables.GAMES)
								.set(Tables.GAMES.STATUS, GameStateEnum.FINISHED)
								.set(Tables.GAMES.TRACE, gameContext.getTrace().toJson())
								.where(Tables.GAMES.ID.eq(gameIdLong))));
					}
				} catch (Throwable any) {
					Tracing.error(any);
				} finally {
					saveSpan.finish();
					scope2.close();
				}
			} catch (Throwable ex) {
				Tracing.error(ex);
			}
		} finally {
			scope.close();
			span.finish();
		}
	}

	@Override
	@Suspendable
	protected void syncStop() throws SuspendExecution {
		LOGGER.debug("stop: Stopping the ClusteredGamesImpl, hosting contexts: {}", contexts.keySet().stream().map(String::toString).reduce((s1, s2) -> s1 + ", " + s2).orElseGet(() -> "none"));
		for (var gameId : contexts.keySet()) {
			Objects.requireNonNull(gameId);
			removeGameAndRecordReplay(gameId);
		}
		if (contexts.size() != 0) {
			LOGGER.warn("stop: Did not succeed in stopping all sessions");
		}
		Void t = await(registration.unregister());
		LOGGER.debug("stop: Activity monitors unregistered");
		LOGGER.debug("stop: Sessions killed");
	}
}
