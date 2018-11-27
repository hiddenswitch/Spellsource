package com.hiddenswitch.spellsource.impl;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import com.hiddenswitch.spellsource.impl.server.VertxScheduler;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import com.hiddenswitch.spellsource.impl.util.DeckType;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.util.Rpc;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.logic.GameStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class ClusteredGames extends SyncVerticle implements Games {
	private Registration registration;
	private Map<GameId, ServerGameContext> contexts = new ConcurrentHashMap<>();

	@Override
	public void start() throws SuspendExecution {
		CardCatalogue.loadCardsFromPackage();

		registration = Rpc.register(this, Games.class);
	}

	@Override
	public CreateGameSessionResponse createGameSession(ConfigurationRequest request) throws SuspendExecution, InterruptedException {
		if (Games.LOGGER.isDebugEnabled()) {
			Games.LOGGER.debug("createGameSession: Creating game session for request " + request.toString());
		}

		if (request.getGameId() == null) {
			throw new IllegalArgumentException("Cannot create a game session without specifying a gameId.");
		}

		Logic.triggers();
		// Get the collection data from the configurations that are not yet populated with valid cards
		for (Configuration configuration : request.getConfigurations()) {
			if (configuration.getDeck() instanceof CollectionDeck) {
				GetCollectionResponse deckCollection = Inventory.getCollection(new GetCollectionRequest()
						.withUserId(configuration.getUserId().toString())
						.withDeckId(configuration.getDeck().getDeckId()));

				// Create the deck and assign all the appropriate IDs to the cards
				Deck deck = deckCollection.asDeck(configuration.getUserId().toString());

				// TODO: Add player information as attached to the hero entity
				configuration.setDeck(deck);
			}

			String username = Accounts.findOne(configuration.getUserId()).getUsername();
			configuration.setName(username);
			// TODO: Get more attributes from database
			AttributeMap playerAttributes = new AttributeMap();
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
			Games.LOGGER.debug("createGameSession: DeploymentId {} is responsible for deploying this match.", deploymentID());
			ServerGameContext context = new ServerGameContext(
					request.getGameId(),
					new VertxScheduler(Vertx.currentContext().owner()),
					request.getConfigurations());

			try {
				for (Configuration configuration : request.getConfigurations()) {
					games.put(configuration.getUserId(), request.getGameId());
				}

				// Deal with ending the game
				context.handleEndGame(this::onGameOver);

				CreateGameSessionResponse response = CreateGameSessionResponse.session(deploymentID(), context);
				connections.replace(request.getGameId(), response);
				contexts.put(request.getGameId(), context);
				// Plays the game context in its own fiber
				context.play(true);
				return response;
			} catch (RuntimeException any) {
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
	}

	@Suspendable
	private void onGameOver(ServerGameContext session) {
		Games.LOGGER.debug("onGameOver: Handling on game over for session " + session.getGameId());
		final GameId gameOverId = new GameId(session.getGameId());
		// The players should not accidentally wind back up in games
		try {
			endGame(gameOverId);
		} catch (InterruptedException | SuspendExecution e) {
			throw new RuntimeException(e);
		}
	}

	@Suspendable
	private void endGame(ActivityMonitor monitor) throws InterruptedException, SuspendExecution {
		endGame(monitor.getGameId());
	}

	@Suspendable
	private void endGame(GameId gameId) throws InterruptedException, SuspendExecution {
		if (!contexts.containsKey(gameId)) {
			Games.LOGGER.debug("endGame {}: This deployment with deploymentId {} does not contain the gameId", gameId, deploymentID());
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
				if (Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner, "deckType", DeckType.DRAFT.toString()),
						json("$inc", json("totalGames", 1, "wins", 1))).getDocModified() > 0L) {
					Mongo.mongo().updateCollection(Draft.DRAFTS, json("_id", userIdWinner), json("$inc", json("publicDraftState.wins", 1)));
					LOGGER.trace("endGame {}: Marked {} as winner in draft", gameId, userIdWinner);
				} else {
					Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdWinner),
							json("$inc", json("totalGames", 1, "wins", 1)));
					LOGGER.trace("endGame {}: Marked {} as winner in other", gameId, userIdWinner);
				}

				// Check if this deck was a draft deck
				if (Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser, "deckType", DeckType.DRAFT.toString()),
						json("$inc", json("totalGames", 1))).getDocModified() > 0L) {
					Mongo.mongo().updateCollection(Draft.DRAFTS, json("_id", userIdLoser), json("$inc", json("publicDraftState.losses", 1)));
					LOGGER.trace("endGame {}: Marked {} as loser in draft", gameId, userIdLoser);
				} else {
					Mongo.mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckIdLoser),
							json("$inc", json("totalGames", 1)));
					LOGGER.trace("endGame {}: Marked {} as loser in other", gameId, userIdLoser);
				}
			}
		} catch (Throwable ex) {
			LOGGER.error("endGame {}: Could not get winner due to {}", gameId, ex.getMessage(), ex);
		}

		// If the game is still running when this is called, make sure to force end the game
		if (gameContext.getStatus() == GameStatus.RUNNING) {
			gameContext.loseBothPlayers();
		}
	}

	@Override
	public DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request) {
		GameId key = new GameId(request.getGameId());
		if (contexts.containsKey(key)) {
			Games.LOGGER.debug("describeGameSession: Describing gameId " + request.getGameId());
			return DescribeGameSessionResponse.fromGameContext(contexts.get(key));
		} else {
			Games.LOGGER.debug("describeGameSession: This game session does not contain the gameId " + request.getGameId());
			return new DescribeGameSessionResponse();
		}
	}

	@Override
	public EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution {
		final GameId key = new GameId(request.getGameId());
		if (contexts.containsKey(key)) {
			Games.LOGGER.debug("endGameSession: Ending the game session for gameId " + request.getGameId());
			endGame(new GameId(request.getGameId()));
		} else {
			Games.LOGGER.debug("endGameSession: This instance does not contain the gameId " + request.getGameId()
					+ ". Redirecting your request to the correct deployment.");
			SuspendableMap<GameId, CreateGameSessionResponse> connections = Games.getConnections();
			CreateGameSessionResponse connection = connections.get(key);
			if (connection == null) {
				Games.LOGGER.error("endGameSession: No gameId " + key.toString() + " was found to be ended. Aborting.");
				return new EndGameSessionResponse();
			}

			Rpc.connect(Games.class).sync(connection.deploymentId).endGameSession(request);
		}
		return new EndGameSessionResponse();
	}

	@Override
	public UpdateEntityResponse updateEntity(UpdateEntityRequest request) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PerformGameActionResponse performGameAction(PerformGameActionRequest request) throws InterruptedException, SuspendExecution {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConcedeGameSessionResponse concedeGameSession(ConcedeGameSessionRequest request) throws InterruptedException, SuspendExecution {
		final GameId key = new GameId(request.getGameId());
		if (contexts.containsKey(key)) {
			Games.LOGGER.debug("concedeGameSession: Conceding game for gameId " + request.getGameId());
			endGame(new GameId(request.getGameId()));
		} else {
			Games.LOGGER.debug("concedeGameSession: This instance does not contain the gameId " + request.getGameId()
					+ ". Redirecting your request to the correct deployment.");
			SuspendableMap<GameId, CreateGameSessionResponse> connections = Games.getConnections();
			CreateGameSessionResponse connection = connections.get(key);
			if (connection == null) {
				Games.LOGGER.error("concedeGameSession: No gameId " + key.toString() + " was found to be ended. Aborting.");
				return new ConcedeGameSessionResponse();
			}

			Rpc.connect(Games.class).sync(connection.deploymentId).concedeGameSession(request);
		}
		return new ConcedeGameSessionResponse();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		Games.LOGGER.debug("stop: Stopping the ClusteredGamesImpl.");
		super.stop();
		Rpc.unregister(registration);
		Games.LOGGER.debug("stop: Activity monitors unregistered");
		for (GameId gameId : contexts.keySet()) {
			endGame(new GameId(gameId.toString()));
		}
		Games.LOGGER.debug("stop: Sessions killed");
	}
}
