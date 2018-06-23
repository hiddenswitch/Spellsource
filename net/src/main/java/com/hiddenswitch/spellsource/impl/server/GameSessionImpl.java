package com.hiddenswitch.spellsource.impl.server;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.Matchmaking;
import com.hiddenswitch.spellsource.client.models.Emote;
import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import com.hiddenswitch.spellsource.common.ClientConnectionConfigurationImpl;
import com.hiddenswitch.spellsource.common.NetworkBehaviour;
import com.hiddenswitch.spellsource.common.Writer;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.models.MatchExpireRequest;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static net.demilich.metastone.game.targeting.IdFactory.PLAYER_1;
import static net.demilich.metastone.game.targeting.IdFactory.PLAYER_2;

public class GameSessionImpl implements GameSession {
	private String host;
	private int websocketPort;
	private Writer c1;
	private Writer c2;
	private Configuration configuration1;
	private Configuration configuration2;
	private ServerGameContext gameContext;
	private final String gameId;
	//	private final Map<String, String> secretForUserId = new HashMap<>();
	private Logger logger = LoggerFactory.getLogger(GameSessionImpl.class);
	private final HashSet<SuspendableAction1<GameSessionImpl>> gameOverHandlers = new HashSet<>();
	private final Vertx vertx;

	public GameSessionImpl(String host, int websocketPort, Configuration p1, Configuration p2, String gameId, Vertx vertx) {
		setHost(host);
		this.configuration1 = p1;
		this.configuration2 = p2;
		this.gameId = gameId;
		this.vertx = vertx;
		this.websocketPort = websocketPort;
		if (p1.getUserId().equals(p2.getUserId())) {
			throw new RuntimeException();
		}
	}

	private ClientConnectionConfiguration getConfigurationFor(Configuration player) {
		return new ClientConnectionConfigurationImpl(player.getUserId());
	}

	@Override
	@Suspendable
	public void onPlayerConnected(int playerId, final Writer writer) {
		logger.debug("Receive connections from {}", Integer.toString(playerId));
		if (playerId == PLAYER_1) {
			if (Writer.isOpen(c1)) {
				c1.close();
			}
			setClient1(writer);
		} else if (playerId == IdFactory.PLAYER_2) {
			if (Writer.isOpen(c2)) {
				c2.close();
			}
			setClient2(writer);
		} else {
			throw new RuntimeException("A player without an ID set has attempted to connect.");
		}

		if (isGameReady()) {
			startGame();
		}
	}

	@Override
	@Suspendable
	public void onPlayerReconnected(int playerId, Writer writer) {
		checkContext();
		if (playerId == PLAYER_1) {
			if (getClient1() != null) {
				getClient1().close();
			}
			setClient1(writer);
		} else if (playerId == IdFactory.PLAYER_2) {
			if (getClient2() != null) {
				getClient2().close();
			}

			setClient2(writer);
		} else {
			throw new RuntimeException("A player without an ID set has attempted to connect.");
		}

		getGameContext().onPlayerReconnected(getGameContext().getPlayer(playerId), writer);
	}

	@Override
	@Suspendable
	public void onActionReceived(String id, int actionIndex) {
		onActionReceived(id, getActionForMessage(id, actionIndex));
	}

	@Override
	@Suspendable
	public void onActionReceived(String id, GameAction action) {
		checkContext();
		getGameContext().onActionReceived(id, action);
	}

	@Override
	public boolean isGameReady() {
		final boolean aiReady = isAgainstAI() &&
				(Writer.isOpen(getClient1()) || Writer.isOpen(getClient2()));
		return aiReady
				|| (Writer.isOpen(getClient1()) && Writer.isOpen(getClient2()));
	}

	/**
	 * This is where a game is actually started in the networked engine.
	 */
	@Suspendable
	private void startGame() {
		logger.debug("startGame: Starting game {}", gameId);
		DeckFormat deckFormat = DeckFormat.getSmallestSupersetFormat(
				Arrays.asList(configuration1.getDeck(), configuration2.getDeck()));
		logger.debug("startGame: Selected sets {} for play", deckFormat.getCardSets());

		// Configure the network behaviours on the players
		Player player1 = getPlayer(configuration1.getUserId());
		Player player2 = getPlayer(configuration2.getUserId());
		this.gameContext = new ServerGameContext(player1, player2, deckFormat, getGameId(), new VertxScheduler(vertx));
		this.gameContext.setBehaviours(new Behaviour[]{new NetworkBehaviour(), new NetworkBehaviour()});
		final Writer listener1;
		final Writer listener2;

		if (isAgainstAI()) {
			if (configuration1.isAI()) {
				listener1 = new BotsWriter(getGameContext(), vertx.eventBus(), PLAYER_1);
				getGameContext().getPlayer(0).setAttribute(Attribute.AI_OPPONENT);
				listener2 = getPlayerListener(PLAYER_2);
				((NetworkBehaviour) getGameContext().getBehaviours().get(0)).setHuman(false);
				setClient1(listener1);
			} else if (configuration2.isAI()) {
				listener1 = getPlayerListener(PLAYER_1);
				listener2 = new BotsWriter(getGameContext(), vertx.eventBus(), PLAYER_2);
				getGameContext().getPlayer(1).setAttribute(Attribute.AI_OPPONENT);
				((NetworkBehaviour) getGameContext().getBehaviours().get(1)).setHuman(false);
				setClient2(listener2);
			} else {
				throw new RuntimeException();
			}
		} else {
			listener1 = getClient1();
			listener2 = getClient2();
		}

		// Merge in attributes
		for (int i = 0; i < 2; i++) {
			Configuration config = new Configuration[]{configuration1, configuration2}[i];
			final AttributeMap attributes = config.getAttributes();
			if (attributes == null) {
				continue;
			}
			for (Map.Entry<Attribute, Object> kv : attributes.entrySet()) {
				getGameContext().getPlayer(i).getAttributes().put(kv.getKey(), kv.getValue());
			}
		}


		getGameContext().setUpdateListener(player1, listener1);
		getGameContext().setUpdateListener(player2, listener2);
		getGameContext().handleEndGame(sgc -> {
			for (SuspendableAction1<GameSessionImpl> h : gameOverHandlers) {
				h.call(this);
			}
		});
		getGameContext().play();
	}

	@Override
	public ClientConnectionConfiguration getConfigurationForPlayer1() {
		return getConfigurationFor(configuration1);
	}

	@Override
	public ClientConnectionConfiguration getConfigurationForPlayer2() {
		return getConfigurationFor(configuration2);
	}

	private Writer getPlayerListener(int player) {
		if (player == 0) {
			return getClient1();
		} else {
			return getClient2();
		}
	}

	@Suspendable
	@Override
	public void kill() {
		// The game never started if this were null
		final boolean open1 = Writer.isOpen(getClient1());
		final boolean open2 = Writer.isOpen(getClient2());
		if (getGameContext() != null && isGameReady()) {
			getGameContext().kill();
		} else if (!isGameReady()) {
			// Send a game over message to the players that may have connected
			if (open1) {
				getClient1().onGameEnd(null, null);
			}
			if (open2) {
				getClient2().onGameEnd(null, null);
			}
		}

		if (open1) {
			getClient1().close();
		}
		if (open2) {
			getClient2().close();
		}
	}

	private void checkContext() {
		if (getGameContext() == null) {
			throw new NullPointerException(String.format("The game context for this game session is null. gameId: %s", getGameId()));
		}
	}

	private boolean isAgainstAI() {
		return configuration1.isAI()
				|| configuration2.isAI();
	}

	private Player createAIPlayer(Configuration pregame, int id) {
		Player player = new Player(pregame.getDeck(), pregame.getName());
		player.setId(id);
		return player;
	}

	public String getHost() {
		return host;
	}

	private void setHost(String host) {
		this.host = host;
	}

	public Writer getClient1() {
		return c1;
	}

	private void setClient1(Writer c1) {
		this.c1 = c1;
	}

	public Writer getClient2() {
		return c2;
	}

	private void setClient2(Writer c2) {
		this.c2 = c2;
	}

	@Override
	public ServerGameContext getGameContext() {
		return gameContext;
	}

	@Override
	public Player getPlayer(String userId) {
		final Configuration[] configs = {configuration1, configuration2};

		for (int i = 0; i < 2; i++) {
			Configuration pregame = configs[i];
			if (pregame.getUserId().equals(userId)) {
				if (pregame.isAI()) {
					return createAIPlayer(pregame, i);
				} else {
					return Player.forUser(userId, i, pregame.getDeck());
				}
			}
		}
		throw new IllegalStateException("Session was not configured with valid players.");
	}

	@Override
	public GameAction getActionForMessage(String messageId, int actionIndex) {
		if (!isGameReady()) {
			throw new RuntimeException("Unexpectedly trying to receive a game action for a game that isn't ready yet");
		}
		return getGameContext().getActionForMessage(messageId, actionIndex);
	}

	@Override
	public int getPlayerIdForSocket(Object socket) {
		if (getClient1() != null
				&& getClient1().getPrivateSocket().equals(socket)) {
			return GameContext.PLAYER_1;
		} else if (getClient2() != null
				&& getClient2().getPrivateSocket().equals(socket)) {
			return GameContext.PLAYER_2;
		}
		// TODO: We probably should be able to concede a player that hasn't connected
		throw new RuntimeException("Cannot get a player that hasn't connected.");
	}

	@Override
	@Suspendable
	public void onMulliganReceived(String messageId, List<Integer> discardedCardIndices) {
		if (!isGameReady()) {
			throw new RuntimeException("Unexpectedly trying to mulligan a game that isn't ready yet");
		}
		getGameContext().onMulliganReceived(messageId, discardedCardIndices);
	}

	@Override
	public void onEmote(int entityId, Emote.MessageEnum message) {
		if (!isGameReady()) {
			return;
		}
		if (getClient1() != null) {
			getClient1().onEmote(entityId, message);
		}

		if (getClient2() != null) {
			getClient2().onEmote(entityId, message);
		}
	}

	@Override
	@Suspendable
	public void onConcede(int playerId) {
		if (!isGameReady()) {
			return;
		}
		if (getGameContext() != null) {
			MatchExpireRequest request = new MatchExpireRequest(getGameId());
			request.setUsers(getUserIds()).setWinner(getUserIds().get(getOpponent(playerId)));
			try {
				Matchmaking.expireOrEndMatch(request);
			} catch (SuspendExecution | InterruptedException execution) {
				throw new RuntimeException(execution);
			}
			getGameContext().concede(playerId);
		}
	}

	@Override
	@Suspendable
	public void onTouch(int playerId, int entityId) {
		if (!isGameReady()) {
			return;
		}
		getPlayerListener(getOpponent(playerId))
				.onNotification(
						new TouchingNotification(playerId, entityId, true),
						getGameContext().getGameState());
	}

	@Override
	@Suspendable
	public void onUntouch(int playerId, int entityId) {
		if (!isGameReady()) {
			return;
		}
		getPlayerListener(getOpponent(playerId))
				.onNotification(
						new TouchingNotification(playerId, entityId, false),
						getGameContext().getGameState());
	}

	public String getGameId() {
		return gameId;
	}

	@Override
	public void handleGameOver(SuspendableAction1<GameSessionImpl> handler) {
		gameOverHandlers.add(handler);
	}

	public String getUrl() {
		return "ws://" + getHost() + ":" + Integer.toString(websocketPort) + "/" + Games.WEBSOCKET_PATH + "-clustered";
	}

	private int getOpponent(int playerId) {
		return playerId == PLAYER_1 ? PLAYER_2 : PLAYER_1;
	}
}
