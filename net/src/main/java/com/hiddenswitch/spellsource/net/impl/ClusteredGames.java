package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.base.Throwables;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.server.Configuration;
import com.hiddenswitch.spellsource.net.impl.server.VertxScheduler;
import com.hiddenswitch.spellsource.net.impl.util.DeckType;
import com.hiddenswitch.spellsource.net.impl.util.GameRecord;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import com.hiddenswitch.spellsource.net.models.ConfigurationRequest;
import com.hiddenswitch.spellsource.net.models.CreateGameSessionResponse;
import com.hiddenswitch.spellsource.net.models.GetCollectionRequest;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.logic.GameStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static io.vertx.core.json.JsonObject.mapFrom;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

public class ClusteredGames extends SyncVerticle implements Games {

	private Map<GameId, ServerGameContext> contexts = new ConcurrentHashMap<>();
	private MessageConsumer<JsonObject> registration;

	@Override
	protected void syncStart() throws SuspendExecution {
		CardCatalogue.loadCardsFromPackage();
		var eb = Vertx.currentContext().owner().eventBus();
		registration = eb.consumer("Games.createGameSession",
				fiber((Message<JsonObject> request) ->
						request.reply(json(createGameSession(request.body().mapTo(ConfigurationRequest.class))))));
	}

	public Map<GameId, ServerGameContext> getContexts() {
		return Collections.unmodifiableMap(contexts);
	}

	@Override
	public CreateGameSessionResponse createGameSession(ConfigurationRequest request) throws SuspendExecution, InterruptedException {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ClusteredGames/createGameSession")
				.asChildOf(request.getSpanContext())
				.start();
		try (var s1 = tracer.activateSpan(span)) {
			span.log(json(request).getMap());
			Games.LOGGER.debug("createGameSession: Creating game session for request " + request.toString());

			if (request.getGameId() == null) {
				throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
			}

			// Loads persistence game triggers that implement legacy functionality. In other words, ensures that game contexts
			// will contain the event-listening enchantments that interact with network services like the database to store
			// stuff about cards.
			Logic.triggers();
			// Get the collection data from the configurations that are not yet populated with valid cards
			for (var configuration : request.getConfigurations()) {
				var playerAttributes = new AttributeMap();

				if (configuration.getDeck() instanceof CollectionDeck) {
					var deckCollection = Inventory.getCollection(new GetCollectionRequest()
							.withUserId(configuration.getUserId().toString())
							.withDeckId(configuration.getDeck().getDeckId()));

					// Create the deck and assign all the appropriate IDs to the cards
					Deck deck = deckCollection.asDeck(configuration.getUserId().toString());

					// TODO: Add player information as attached to the hero entity
					configuration.setDeck(deck);

					// Add all the attributes that were specified in the deck collection
					// Implements Signature
					if (deckCollection.getCollectionRecord().getPlayerEntityAttributes() != null) {
						playerAttributes.putAll(deckCollection.getCollectionRecord().getPlayerEntityAttributes());
					}
				}

				var username = mongo().findOne(Accounts.USERS, json("_id", configuration.getUserId().toString()), UserRecord.class).getUsername();
				configuration.setName(username);
				// TODO: Get more attributes from database
				playerAttributes.put(Attribute.NAME, username);
				playerAttributes.put(Attribute.USER_ID, configuration.getUserId().toString());
				playerAttributes.put(Attribute.DECK_ID, configuration.getDeck().getDeckId());

				configuration.setPlayerAttributes(playerAttributes);
			}

			// If we're the ones deploying this match...
			var context = new ServerGameContext(
					request.getGameId(),
					new VertxScheduler(Vertx.currentContext().owner()),
					request.getConfigurations());

			// Enable tracing
			context.setSpanContext(span.context());

			try {
				// Deal with ending the game
				context.addEndGameHandler(session -> {
					// Do not record replays if we're interrupting
					if (Strand.currentStrand().isInterrupted()) {
						return;
					}
					Games.LOGGER.debug("onGameOver: Handling on game over for session " + session.getGameId());
					var gameOverId = new GameId(session.getGameId());
					// The players should not accidentally wind back up in games
					removeGameAndRecordReplay(gameOverId);
				});

				var response = CreateGameSessionResponse.session(deploymentID(), context);
				contexts.put(request.getGameId(), context);
				// Plays the game context in its own fiber
				context.play(true);
				span.log("awaitingRegistration");
				context.awaitReadyForConnections();
				return response;
			} catch (RuntimeException any) {
				if (Throwables.getRootCause(any) instanceof TimeoutException) {
					span.log("timeout");
				}
				Tracing.error(any, span, true);
				throw any;
			}
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
	private void removeGameAndRecordReplay(@NotNull GameId gameId) throws SuspendExecution {
		Objects.requireNonNull(gameId);
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ClusteredGames/removeGameAndRecordReplay")
				.asChildOf(tracer.activeSpan())
				.start();
		var scope = tracer.activateSpan(span);
		try {
			if (!contexts.containsKey(gameId)) {
				Games.LOGGER.debug("removeGameAndRecordReplay {}: This deployment with deploymentId {} does not contain the gameId, or this game has already been ended", gameId, deploymentID());
				return;
			}
			Games.LOGGER.debug("removeGameAndRecordReplay {}", gameId);
			var gameContext = contexts.remove(gameId);

			UserId winner = null;

			try {
				var isGameOver = gameContext.updateAndGetGameOver();
				Games.LOGGER.debug("removeGameAndRecordReplay: updateAndGetGameOver={}", isGameOver);
				if (gameContext.getWinner() != null && gameContext.getWinner().getUserId() != null) {
					winner = new UserId(gameContext.getWinner().getUserId());
				}
				// Save the wins/losses
				if (winner != null) {
					var userIdWinner = winner.toString();
					var userIdLoser = gameContext.getOpponent(gameContext.getWinner()).getUserId();
					var deckIdWinner = (String) gameContext.getWinner().getAttribute(Attribute.DECK_ID);
					var deckIdLoser = (String) gameContext.getOpponent(gameContext.getWinner()).getAttribute(Attribute.DECK_ID);
					// Check if this deck was a draft deck
					if (mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner, "deckType", DeckType.DRAFT.toString()),
							json("$inc", json("totalGames", 1, "wins", 1))).getDocModified() > 0L) {
						mongo().updateCollection(Draft.DRAFTS, json("_id", userIdWinner), json("$inc", json("publicDraftState.wins", 1)));
						LOGGER.debug("endGame {}: Marked {} as winner in draft", gameId, userIdWinner);
					} else {
						mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner),
								json("$inc", json("totalGames", 1, "wins", 1)));
						LOGGER.debug("endGame {}: Marked {} as winner in other", gameId, userIdWinner);
					}

					// Check if this deck was a draft deck
					if (mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser, "deckType", DeckType.DRAFT.toString()),
							json("$inc", json("totalGames", 1))).getDocModified() > 0L) {
						mongo().updateCollection(Draft.DRAFTS, json("_id", userIdLoser), json("$inc", json("publicDraftState.losses", 1)));
						LOGGER.debug("endGame {}: Marked {} as loser in draft", gameId, userIdLoser);
					} else {
						mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser),
								json("$inc", json("totalGames", 1)));
						LOGGER.debug("endGame {}: Marked {} as loser in other", gameId, userIdLoser);
					}
				}
				// If the game is still running when this is called, make sure to force end the game
			} finally {
				if (gameContext.getStatus() == GameStatus.RUNNING) {
					gameContext.loseBothPlayers();
				}
			}

			var userIds = gameContext.getPlayerConfigurations().stream().map(Configuration::getUserId).map(UserId::toString).collect(toList());
			// Presence is automatically updated by a timer
			try {
				var botGame = gameContext.getPlayerConfigurations().stream().anyMatch(Configuration::isBot);
				var deckIds = gameContext.getPlayerConfigurations().stream().map(Configuration::getDeck).map(Deck::getDeckId).collect(toList());
				var playerNames = gameContext.getPlayerConfigurations().stream().map(Configuration::getName).collect(toList());

				var saveSpan = tracer.buildSpan("ClusteredGames/removeGameAndRecordReplay/saveReplay")
						.asChildOf(span)
						.start();
				var scope2 = tracer.activateSpan(saveSpan);
				try {
					var gameRecord = new GameRecord(gameId.toString())
							.setTrace(gameContext.getTrace())
							.setCreatedAt(new Date())
							.setBotGame(botGame)
							.setPlayerUserIds(userIds)
							.setDeckIds(deckIds)
							.setPlayerNames(playerNames);
					if (!Strand.currentStrand().isInterrupted()) {
						mongo().insert(Games.GAMES, mapFrom(gameRecord));
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
		Games.LOGGER.debug("stop: Stopping the ClusteredGamesImpl, hosting contexts: {}", contexts.keySet().stream().map(GameId::toString).reduce((s1, s2) -> s1 + ", " + s2).orElseGet(() -> "none"));
		for (var gameId : contexts.keySet()) {
			Objects.requireNonNull(gameId.toString());
			removeGameAndRecordReplay(gameId);
		}
		if (contexts.size() != 0) {
			Games.LOGGER.warn("stop: Did not succeed in stopping all sessions");
		}
		Void t = awaitResult(h -> registration.unregister(h));
		Games.LOGGER.debug("stop: Activity monitors unregistered");
		Games.LOGGER.debug("stop: Sessions killed");
	}
}
