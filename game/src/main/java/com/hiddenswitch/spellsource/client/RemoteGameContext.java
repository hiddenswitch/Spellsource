package com.hiddenswitch.spellsource.client;

import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.NetworkBehaviour;
import com.hiddenswitch.spellsource.common.Client;
import com.hiddenswitch.spellsource.util.LoggerUtils;
import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.human.HumanBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.visuals.GameContextVisuals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class RemoteGameContext extends GameContext implements GameContextVisuals, Client {
	private final List<GameEvent> gameEvents = new ArrayList<>();
	private boolean blockedByAnimation;
	private int localPlayerId = -1;
	private volatile boolean gameDecided = false;
	private int remoteTurn;
	private TurnState remoteTurnState;
	private volatile List<GameAction> remoteValidActions;
	private String host;
	private int port;
	private boolean mulligan;
	private ClientConnectionConfiguration connectionConfiguration;
	private ClientCommunicationSend ccs;
	private ClientCommunicationReceive ccr;
	public boolean ignoreEventOverride = false;
	private final SocketClientConnection socketClientConnection;
	private final PriorityBlockingQueue<RequestedAction> actionQueue = new PriorityBlockingQueue<>(2, (o1, o2) -> Long.compare(o1.state.timestamp, o2.state.timestamp));
	private long lastUpdatedAt = Long.MIN_VALUE;
	private long serverTimeDiff = 0;

	public RemoteGameContext(ClientConnectionConfiguration connectionConfiguration) {
		super(connectionConfiguration.getFirstMessage().getPlayer1(), null, new GameLogic(), new DeckFormat()
				.withCardSets(CardSet.values()));
		this.connectionConfiguration = connectionConfiguration;
		this.host = connectionConfiguration.getHost();
		this.port = connectionConfiguration.getPort();
		this.socketClientConnection = new SocketClientConnection(host, port);
		this.ccs = socketClientConnection;
		this.ccr = socketClientConnection;
		ccr.RegisterListener(this);
		Thread networkingThread = new Thread(socketClientConnection);
		networkingThread.start();
	}

	@Override
	protected boolean acceptAction(GameAction nextAction) {
		throw new RuntimeException("should not be called");
	}

	@Override
	public void addTrigger(Trigger trigger) {
		throw new RuntimeException("should not be called");
	}


	@Override
	public synchronized GameContext clone() {
		return super.clone();
	}

	@Override
	public synchronized void dispose() {
		actionQueue.clear();
		socketClientConnection.kill();
	}

	@Override
	protected void endGame() {
		for (Player player : getPlayers()) {
			player.getBehaviour().onGameOver(this, player.getId(), getWinner() != null ? getWinner().getId() : -1);
		}

		calculateStatistics();
	}

	@Override
	public void endTurn() {
		//do nothing;
	}

	@Override
	public void fireGameEvent(GameEvent gameEvent) {
		throw new RuntimeException("should not be called");
	}

	private void addGameEvent(GameEvent gameEvent) {
		getGameEvents().add(gameEvent);
	}

	public synchronized List<GameEvent> getGameEvents() {
		return gameEvents;
	}

	@Override
	public int getTotalMinionCount() {
		throw new RuntimeException("should not be called");
	}

	@Override
	public List<Trigger> getTriggersAssociatedWith(EntityReference entityReference) {
		throw new RuntimeException("should not be called");
	}

	public int getTurn() {
		return getRemoteTurn();
	}

	public TurnState getTurnState() {
		return getRemoteTurnState();
	}

	public List<GameAction> getValidActions() {
		return getRemoteValidActions();
	}


	@Override
	public boolean updateAndGetGameOver() {
		//TODO: return localGameDecided;
		return gameDecided;
	}

	@Override
	public GameAction getAutoHeroPowerAction() {
		throw new RuntimeException("should not be called");
	}

	@Override
	public boolean ignoreEvents() {
		//Patch around lack of good notification when testing
		//TODO: refactor this patch
		if (ignoreEventOverride) {
			return true;
		}
		return (!mulligan && getActivePlayerId() == -1);
	}

	@Override
	public void init() {
		ccs.getSendToServer().sendGenericMessage(connectionConfiguration.getFirstMessage());
	}

	@Override
	public GameLogic getLogic() {
		//TODO: FIX
		return super.getLogic();
	}

	@Override
	public void play() {
		logger.debug("Game starts.");
		init();
		logger.debug("Waiting for players to connect...");
		while (getActivePlayerId() == -1) {
			try {
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
			} catch (InterruptedException ignored) {
			}
		}
		logger.debug("Players connected, waiting for action.");
		while (!updateAndGetGameOver()) {
			requestLocalAction();
			if (isHumanPlayer()) {
				try {
					Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
				} catch (InterruptedException ignored) {
				}
			}
		}
		endGame();
	}

	protected boolean isHumanPlayer() {
		final Player localPlayer = getLocalPlayer();
		if (localPlayer == null) {
			return true;
		}
		final Behaviour behaviour = localPlayer.getBehaviour();
		if (behaviour == null) {
			return true;
		}
		return behaviour instanceof NetworkBehaviour ?
				(((NetworkBehaviour) behaviour).getWrapBehaviour() instanceof HumanBehaviour)
				: behaviour instanceof HumanBehaviour;
	}

	protected void requestLocalAction() {
		if (hasPlayer(PLAYER_1)
				&& hasPlayer(PLAYER_2)
				&& getLocalPlayer() != null
				&& isActivePlayer()) {
			RequestedAction action = actionQueue.poll();
			if (action == null) {
				return;
			}
			updateWithState(action.state);
			this.setRemoteValidActions(action.availableActions);
			this.onGameStateChanged();
			logger.debug("Action was requested from player.");
			GameAction choice;

			if (isHumanPlayer()) {
				choice = getGameAction();
			} else {
				choice = getGameActionSync();
			}

			if (choice == null) {
				LoggerUtils.log(this, this, new RuntimeException("Requested local action failed."));
			}

			ccs.getSendToServer().sendAction(action.id, choice);
		}
	}

	private GameAction getGameAction() {
		return getLocalPlayer().getBehaviour().requestAction(this.clone(), getActivePlayer(), getValidActions());
	}


	private synchronized GameAction getGameActionSync() {
		return getLocalPlayer().getBehaviour().requestAction(this.clone(), getActivePlayer(), getValidActions());
	}

	public boolean isBlockedByAnimation() {
		return blockedByAnimation;
	}

	public void setBlockedByAnimation(boolean blockedByAnimation) {
		this.blockedByAnimation = blockedByAnimation;
	}

	@Override
	protected void onGameStateChanged() {
		if (ignoreEvents()) {
			return;
		}

		setBlockedByAnimation(true);
		NotificationProxy.sendNotification(GameNotification.GAME_STATE_UPDATE, this);

		while (blockedByAnimation) {
			try {
				Thread.sleep(BuildConfig.DEFAULT_SLEEP_DELAY);
			} catch (InterruptedException ignored) {
			}
		}
		NotificationProxy.sendNotification(GameNotification.GAME_STATE_LATE_UPDATE, this);
	}

	@Override
	public boolean takeActionInTurn() {
		throw new RuntimeException("should not be called");
	}

	public TurnState getRemoteTurnState() {
		return remoteTurnState;
	}

	public void setRemoteTurnState(TurnState remoteTurnState) {
		setTurnState(remoteTurnState);
		this.remoteTurnState = remoteTurnState;
	}

	public int getRemoteTurn() {
		return remoteTurn;
	}

	public void setRemoteTurn(int remoteTurn) {
		this.remoteTurn = remoteTurn;
	}

	public Player getLocalPlayer() {
		try {
			if (getLocalPlayerId() == -1) {
				return null;
			}

			if (!hasPlayer(getLocalPlayerId())) {
				return null;
			}
			return getPlayer(getLocalPlayerId());
		} catch (Exception ignored) {
			return null;
		}
	}

	public void setLocalPlayer(Player localPlayer) {
		this.localPlayerId = localPlayer.getId();
	}

	public List<GameAction> getRemoteValidActions() {
		return remoteValidActions;
	}

	public void setRemoteValidActions(List<GameAction> remoteValidActions) {
		this.remoteValidActions = remoteValidActions;
	}

	@Override
	public void onNotification(Notification event, GameState gameState) {
		if (event instanceof GameEvent) {
			this.addGameEvent((GameEvent) event);
		}

		this.onGameStateChanged();
	}

	@Override
	public void onGameEnd(Player w) {
		this.setWinner(w);
		this.gameDecided = true;
		this.onGameStateChanged();
		this.endGame();
	}

	@Override
	public void onActivePlayer(Player ap) {
		logger.debug("On active player {}", ap.getId());
		this.setActivePlayerId(ap.getId());
		this.onGameStateChanged();
		logger.debug("End active player {}", ap.getId());
	}

	@Override
	public void setPlayers(Player localPlayer, Player remotePlayer) {
		this.setLocalPlayer(localPlayer);
		this.setPlayer(localPlayer.getId(), localPlayer);
		this.setPlayer(remotePlayer.getId(), remotePlayer);

		hideCards();
	}

	protected void hideCards() {
		getLocalPlayer().setHideCards(false);
		getOpponent(getLocalPlayer()).setHideCards(true);
	}

	@Override
	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		actionQueue.offer(new RequestedAction(id, state, availableActions));
	}

	@Override
	public void onTurnEnd(Player ap, int turnNumber, TurnState turnState) {
		logger.debug("new active player: " + ap.getId());
		this.setActivePlayerId(ap.getId());
		this.setRemoteTurn(turnNumber);
		this.setRemoteTurnState(turnState);
		this.onGameStateChanged();
	}

	@Override
	public void onUpdate(GameState state) {
		updateWithState(state);
		this.onGameStateChanged();
	}

	protected synchronized void updateWithState(GameState state) {
		if (lastUpdatedAt == Long.MIN_VALUE) {
			// Update the time diff with the server
			serverTimeDiff = System.nanoTime() - state.timestamp;
		}
		if (lastUpdatedAt > state.timestamp) {
			logger.error("State from wire out of date!");
			return;
		}
		this.lastUpdatedAt = state.timestamp;
		setGameState(state);
		hideCards();
	}

	@Override
	public void setGameState(GameState state) {
		super.setGameState(state);
		setRemoteTurnState(state.turnState);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public void onMulligan(String messageId, GameState state, List<Card> cards, int playerId) {
		mulligan = false;
		logger.debug("Mulligan requested.");
		final Player player = getPlayer(playerId);
		List<Card> discardedCards = player.getBehaviour().mulligan(this, player, cards);
		mulligan = true;
		ccs.getSendToServer().sendMulligan(messageId, player, discardedCards);
	}

	@Override
	public void close() {
		dispose();
	}

	@Override
	public Object getPrivateSocket() {
		return socketClientConnection;
	}

	@Override
	public void lastEvent() {
	}

	@Override
	public void setPlayer(int index, Player player) {
		// Don't override the existing behaviour
		if (hasPlayer(index)) {
			Behaviour existingBehaviour = getPlayer(index).getBehaviour();
			player.setBehaviour(existingBehaviour);
		}
		super.setPlayer(index, player);
	}

	@Override
	public int getLocalPlayerId() {
		return localPlayerId;
	}

	public long getLastUpdatedAt() {
		return lastUpdatedAt;
	}

	public boolean isTimedOut() {
		return isTimedOut((long) 40e9);
	}

	public boolean isTimedOut(long toleranceNanoseconds) {
		return !updateAndGetGameOver()
				&& lastUpdatedAt != Long.MIN_VALUE
				&& ((System.nanoTime() - serverTimeDiff) > (lastUpdatedAt + toleranceNanoseconds));
	}

	public long clientDelay() {
		return System.nanoTime() - serverTimeDiff - lastUpdatedAt;
	}

	public boolean isActivePlayer() {
		return getActivePlayerId() == getLocalPlayerId();
	}

	public RemoteGameContext withIgnoreEventOverride(boolean b) {
		ignoreEventOverride = b;
		return this;
	}
}
