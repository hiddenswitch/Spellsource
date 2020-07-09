package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.server.Configuration;
import com.hiddenswitch.spellsource.net.impl.server.VertxScheduler;
import com.hiddenswitch.spellsource.net.impl.util.DeckType;
import com.hiddenswitch.spellsource.net.impl.util.GameRecord;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import com.hiddenswitch.spellsource.net.models.ConfigurationRequest;
import com.hiddenswitch.spellsource.net.models.CreateGameSessionResponse;
import com.hiddenswitch.spellsource.net.models.GetCollectionRequest;
import com.hiddenswitch.spellsource.net.models.GetCollectionResponse;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
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

import java.util.*;
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
	public void start() throws SuspendExecution {
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
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("ClusteredGames/createGameSession")
				.asChildOf(request.getSpanContext())
				.start();
		try (Scope s1 = tracer.activateSpan(span)) {
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
			for (Configuration configuration : request.getConfigurations()) {
				AttributeMap playerAttributes = new AttributeMap();

				if (configuration.getDeck() instanceof CollectionDeck) {
					GetCollectionResponse deckCollection = Inventory.getCollection(new GetCollectionRequest()
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

				String username = mongo().findOne(Accounts.USERS, json("_id", configuration.getUserId().toString()), UserRecord.class).getUsername();
				configuration.setName(username);
				// TODO: Get more attributes from database
				playerAttributes.put(Attribute.NAME, username);
				playerAttributes.put(Attribute.USER_ID, configuration.getUserId().toString());
				playerAttributes.put(Attribute.DECK_ID, configuration.getDeck().getDeckId());

				configuration.setPlayerAttributes(playerAttributes);
			}

			CreateGameSessionResponse pending = CreateGameSessionResponse.pending(deploymentID());
			SuspendableMap<GameId, CreateGameSessionResponse> connections = Games.getConnections();
			SuspendableMap<UserId, GameId> games = Games.getUsersInGames();
			CreateGameSessionResponse connection = connections.putIfAbsent(request.getGameId(), pending);
			// If we're the ones deploying this match...
			if (connection == null) {
				ServerGameContext context = new ServerGameContext(
						request.getGameId(),
						new VertxScheduler(Vertx.currentContext().owner()),
						request.getConfigurations());

				// Enable tracing
				context.setSpanContext(span.context());

				try {
					for (Configuration configuration : request.getConfigurations()) {
						games.put(configuration.getUserId(), request.getGameId());
					}

					// Deal with ending the game
					context.addEndGameHandler(session -> {
						// Do not record replays if we're interrupting
						if (Strand.currentStrand().isInterrupted()) {
							return;
						}
						Games.LOGGER.debug("onGameOver: Handling on game over for session " + session.getGameId());
						GameId gameOverId = new GameId(session.getGameId());
						// The players should not accidentally wind back up in games
						removeGameAndRecordReplay(gameOverId);
					});

					CreateGameSessionResponse response = CreateGameSessionResponse.session(deploymentID(), context);
					connections.replace(request.getGameId(), response);
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
					// If an error occurred, make sure to remove users from the games we just put them into.
					for (Configuration configuration : request.getConfigurations()) {
						games.remove(configuration.getUserId(), request.getGameId());
					}
					connections.remove(request.getGameId());
					throw any;
				}
			} else {
				Games.LOGGER.debug("createGameSession: Repeat createGameSessionRequest suspected because actually deploymentId " + connection.deploymentId + " is responsible for deploying this match.");
				// Otherwise, return its state, whatever it is
				return connection;
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
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("ClusteredGames/removeGameAndRecordReplay")
				.asChildOf(tracer.activeSpan())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			if (!contexts.containsKey(gameId)) {
				Games.LOGGER.debug("endGame {}: This deployment with deploymentId {} does not contain the gameId, or this game has already been ended", gameId, deploymentID());
				return;
			}
			Games.LOGGER.debug("endGame {}", gameId);
			ServerGameContext gameContext = contexts.remove(gameId);
			Games.getConnections().remove(gameId);

			UserId winner = null;
			try {
				gameContext.updateAndGetGameOver();
				if (gameContext.getWinner() != null && gameContext.getWinner().getUserId() != null) {
					winner = new UserId(gameContext.getWinner().getUserId());
				}
				// Save the wins/losses
				if (winner != null) {
					String userIdWinner = winner.toString();
					String userIdLoser = gameContext.getOpponent(gameContext.getWinner()).getUserId();
					String deckIdWinner = (String) gameContext.getWinner().getAttribute(Attribute.DECK_ID);
					String deckIdLoser = (String) gameContext.getOpponent(gameContext.getWinner()).getAttribute(Attribute.DECK_ID);
					// Check if this deck was a draft deck
					if (mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner, "deckType", DeckType.DRAFT.toString()),
							json("$inc", json("totalGames", 1, "wins", 1))).getDocModified() > 0L) {
						mongo().updateCollection(Draft.DRAFTS, json("_id", userIdWinner), json("$inc", json("publicDraftState.wins", 1)));
						LOGGER.trace("endGame {}: Marked {} as winner in draft", gameId, userIdWinner);
					} else {
						mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner),
								json("$inc", json("totalGames", 1, "wins", 1)));
						LOGGER.trace("endGame {}: Marked {} as winner in other", gameId, userIdWinner);
					}

					// Check if this deck was a draft deck
					if (mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser, "deckType", DeckType.DRAFT.toString()),
							json("$inc", json("totalGames", 1))).getDocModified() > 0L) {
						mongo().updateCollection(Draft.DRAFTS, json("_id", userIdLoser), json("$inc", json("publicDraftState.losses", 1)));
						LOGGER.trace("endGame {}: Marked {} as loser in draft", gameId, userIdLoser);
					} else {
						mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser),
								json("$inc", json("totalGames", 1)));
						LOGGER.trace("endGame {}: Marked {} as loser in other", gameId, userIdLoser);
					}
				}
				// If the game is still running when this is called, make sure to force end the game
			} finally {
				if (gameContext.getStatus() == GameStatus.RUNNING) {
					gameContext.loseBothPlayers();
				}
			}

			// Set the player's presence to no longer be in a game
			List<String> userIds = gameContext.getPlayerConfigurations().stream().map(Configuration::getUserId).map(UserId::toString).collect(toList());
			for (String userId : userIds) {
				var interrupted = Strand.interrupted();
				Presence.updatePresence(userId);
				if (interrupted) {
					Strand.currentStrand().interrupt();
				}
			}

			try {
				boolean botGame = gameContext.getPlayerConfigurations().stream().anyMatch(Configuration::isBot);
				List<String> deckIds = gameContext.getPlayerConfigurations().stream().map(Configuration::getDeck).map(Deck::getDeckId).collect(toList());
				List<String> playerNames = gameContext.getPlayerConfigurations().stream().map(Configuration::getName).collect(toList());

				Span saveSpan = tracer.buildSpan("ClusteredGames/removeGameAndRecordReplay/saveReplay")
						.asChildOf(span)
						.start();
				Scope scope2 = tracer.activateSpan(saveSpan);
				try {
					GameRecord gameRecord = new GameRecord(gameId.toString())
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
	public void stop() throws Exception {
		Games.LOGGER.debug("stop: Stopping the ClusteredGamesImpl.");
		for (GameId gameId : contexts.keySet()) {
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
