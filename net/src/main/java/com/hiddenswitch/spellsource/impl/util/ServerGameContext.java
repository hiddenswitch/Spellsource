package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.common.*;
import com.hiddenswitch.spellsource.impl.TimerId;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.NetworkDelegate;
import net.demilich.metastone.game.utils.TurnState;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A networked game context from the server's point of view.
 * <p>
 * In addition to storing game state, this class also stores references to {@link Writer} objects that (1) get notified
 * when game state changes and how, and (2) allow this class to {@link net.demilich.metastone.game.behaviour.Behaviour#requestAction(GameContext,
 * Player, List)} and {@link net.demilich.metastone.game.behaviour.Behaviour#mulligan(GameContext, Player, List)} over a
 * network.
 * <p>
 * This class also automatically adds support for persistence effects written on cards using a {@link
 * PersistenceTrigger}.
 */
public class ServerGameContext extends GameContext {
	private final String gameId;
	private Map<Player, Writer> listenerMap = new HashMap<>();
	private final Map<CallbackId, GameplayRequest> requestCallbacks = Collections.synchronizedMap(new HashMap<>());
	private boolean isRunning = true;
	private final transient HashSet<Handler<ServerGameContext>> onGameEndHandlers = new HashSet<>();
	private final List<Trigger> gameTriggers = new ArrayList<>();
	private final transient RpcClient<Logic> logic;
	private final Scheduler scheduler;
	private AtomicInteger eventCounter = new AtomicInteger(0);
	private int timerElapsedForPlayerId;
	private Long timerStartTimeMillis;
	private Long timerLengthMillis;

	/**
	 * {@inheritDoc}
	 * <p>
	 * Additionally, this class uses the provided {@link RpcClient} to implement persistence effects.
	 *
	 * @param player1    The first player.
	 * @param player2    The second player.
	 * @param deckFormat The legal cards that can be played.
	 * @param gameId     The game ID that corresponds to this game context.
	 * @param logic      The {@link RpcClient} on which this trigger will make {@link Logic} requests.
	 * @param scheduler  The {@link Scheduler} instance to use for scheduling game events.
	 */
	public ServerGameContext(Player player1, Player player2, DeckFormat deckFormat, String gameId, RpcClient<Logic> logic, Scheduler scheduler) {
		// The player's IDs are set here
		super(player1, player2, new GameLogicAsync(), deckFormat);
		if (player1.getId() == player2.getId()
				|| player1.getId() == IdFactory.UNASSIGNED
				|| player2.getId() == IdFactory.UNASSIGNED) {
			player1.setId(IdFactory.PLAYER_1);
			player2.setId(IdFactory.PLAYER_2);
		}
		this.gameId = gameId;
		this.logic = logic;
		this.scheduler = scheduler;

		enablePersistenceEffects();
	}

	/**
	 * Enables this match to track persistence effects.
	 *
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	private void enablePersistenceEffects() {
		this.getGameTriggers().add(new PersistenceTrigger(logic, this, this.gameId));
	}

	public GameLogicAsync getNetworkGameLogic() {
		return (GameLogicAsync) getLogic();
	}

	public void setUpdateListener(Player player, Writer listener) {
		listenerMap.put(player, listener);
	}

	@Override
	public void init() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ServerGameContext::init is unsupported.");
	}

	@Override
	@Suspendable
	public void startTurn(int playerId) {
		super.startTurn(playerId);
		GameState state = new GameState(this, TurnState.TURN_IN_PROGRESS);
		getListenerMap().get(getPlayer1()).onUpdate(state);
		getListenerMap().get(getPlayer2()).onUpdate(state);
	}

	@Suspendable
	public void endTurn() {
		super.endTurn();
		this.onGameStateChanged();
		getListenerMap().get(getPlayer1()).onTurnEnd(getActivePlayer(), getTurn(), getTurnState());
		getListenerMap().get(getPlayer2()).onTurnEnd(getActivePlayer(), getTurn(), getTurnState());
	}

	private Player getNonActivePlayer() {
		return getOpponent(getActivePlayer());
	}

	@Override
	public void play() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ServerGameContext::play should not be called. Use ::networkPlay instead.");
	}

	/**
	 * Ends the mulligans early due to timer elapsing.
	 *
	 * @param ignored The ignored timer elapse result.
	 */
	@Suspendable
	@SuppressWarnings("unchecked")
	private void endMulligans(long ignored) {
		Iterator<Map.Entry<CallbackId, GameplayRequest>> requests = requestCallbacks.entrySet().iterator();
		while (requests.hasNext()) {
			Map.Entry<CallbackId, GameplayRequest> next = requests.next();
			if (next.getValue().type == GameplayRequestType.MULLIGAN) {
				requests.remove();
				// TODO: We should probably actually mulligan out the cards that the player checked a big X on
				((Handler<List<Card>>) next.getValue().handler).handle(Collections.emptyList());
			}
		}
	}

	/**
	 * Starts the game and initializes the turn loop.
	 */
	@Suspendable
	public void networkPlay() {
		logger.debug("{} networkedPlay: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
		getNetworkGameLogic().contextReady();
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		setActivePlayerId(getPlayer(startingPlayerId).getId());

		updateActivePlayers();
		getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));

		// Make sure the players are initialized before sending the original player updates.
		getNetworkGameLogic().initializePlayer(IdFactory.PLAYER_1);
		getNetworkGameLogic().initializePlayer(IdFactory.PLAYER_2);

		updateClientsWithGameState();

		Future<Void> init1 = Future.future();
		Future<Void> init2 = Future.future();

		// Set the mulligan timer
		final TimerId mulliganTimerId;
		if (getPlayers().stream().allMatch(Player::isHuman)) {
			timerLengthMillis = getLogic().getMulliganTimeMillis();
			timerStartTimeMillis = System.currentTimeMillis();
			mulliganTimerId = scheduler.setTimer(timerLengthMillis, Sync.fiberHandler(this::endMulligans));
		} else {
			logger.debug("{} networkPlay: No mulligan timer set for game because all players are not human", getGameId());
			mulliganTimerId = null;
		}


		getNetworkGameLogic().initAsync(getActivePlayerId(), true, p -> init1.complete());
		getNetworkGameLogic().initAsync(getOpponent(getActivePlayer()).getId(), false, p -> init2.complete());

		// Mulligan simultaneously now
		CompositeFuture.all(init1, init2).setHandler(cf -> {
			logger.debug("{} networkPlay: Received mulligans", getGameId());
			if (mulliganTimerId != null) {
				scheduler.cancelTimer(mulliganTimerId);
			}

			final TimerId[] turnTimerId = {null};
			Recursive<Runnable> playTurnLoop = new Recursive<>();
			playTurnLoop.func = () -> {
				// End the existing turn timer, if it's set
				if (turnTimerId[0] != null) {
					scheduler.cancelTimer(turnTimerId[0]);
				}

				if (!isRunning) {
					logger.debug("{} networkedPlay: Game no longer running, ending...", getGameId());
					endGame();
					return;
				}

				// Check if the game has been decided right at the end of the player's turn
				if (updateAndGetGameOver()) {
					logger.debug("{} networkedPlay: Game has ended with a normal resolution, ending...", getGameId());
					endGame();
					return;
				}

				final int activePlayerId = getActivePlayerId();
				startTurn(activePlayerId);

				// Start the turn timer
				timerElapsedForPlayerId = -1;
				if (getNonActivePlayer().isHuman()) {
					timerLengthMillis = (long) getTurnTimeForPlayer(activePlayerId);
					timerStartTimeMillis = System.currentTimeMillis();

					turnTimerId[0] = scheduler.setTimer(timerLengthMillis, Sync.fiberHandler(this::elapseTurn));
				} else {
					logger.debug("{} networkedPlay: Not setting timer because opponent is not human.", getGameId());
				}

				Recursive<Handler<Boolean>> actionLoop = new Recursive<>();

				actionLoop.func = hasMoreActions -> {
					if (!isRunning) {
						endGame();
						return;
					}
					if (hasMoreActions) {
						networkedPlayTurn(actionLoop.func);
					} else {
						if (getTurn() > GameLogic.TURN_LIMIT
								|| updateAndGetGameOver()) {
							endGame();
						} else {
							playTurnLoop.func.run();
						}
					}
				};

				networkedPlayTurn(actionLoop.func);
			};

			// Start the active player's turn once the game is initialized.
			playTurnLoop.func.run();
		});
	}

	@Suspendable
	protected void elapseTurn(long ignored) {
		// Since executing the callback may itself trigger more action requests, we'll indicate to
		// the NetworkDelegate (i.e., this ServerGameContext instance) that further
		// networkRequestActions should be executed immediately.
		timerElapsedForPlayerId = getActivePlayerId();

		// Enumerate the pending callbacks and remove them.
		Iterator<Map.Entry<CallbackId, GameplayRequest>> requests = requestCallbacks.entrySet().iterator();
		while (requests.hasNext()) {
			Map.Entry<CallbackId, GameplayRequest> next = requests.next();
			GameplayRequest request = next.getValue();
			if (request.type == GameplayRequestType.ACTION) {
				requests.remove();
				processActionForElapsedTurn(request.actions, request.handler);
			}
		}

		// At this point, end turn should have been called.
	}

	private int getTurnTimeForPlayer(int activePlayerId) {
		return getLogic().getTurnTimeMillis(activePlayerId);
	}

	protected void updateActivePlayers() {
		getListenerMap().get(getActivePlayer()).onActivePlayer(getActivePlayer());
		getListenerMap().get(getNonActivePlayer()).onActivePlayer(getActivePlayer());
	}

	@Override
	public boolean takeActionInTurn() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ServerGameContext::playTurn should not be called.");
	}

	@Suspendable
	protected void networkedPlayTurn(Handler<Boolean> callback) {
		if (!isRunning) {
			return;
		}

		try {
			setActionsThisTurn(getActionsThisTurn() + 1);

			if (getActionsThisTurn() > 99) {
				logger.warn("{} networkedPlayTurn: Turn has been forcefully ended after {} actions", getGameId(), getActionsThisTurn());
				endTurn();
				callback.handle(false);
				return;
			}

			if (getLogic().hasAutoHeroPower(getActivePlayerId())) {
				performAction(getActivePlayerId(), getAutoHeroPowerAction());
				callback.handle(true);
				return;
			}

			List<GameAction> validActions = getValidActions();
			if (validActions.size() == 0) {
				endTurn();
				callback.handle(false);
				return;
			}

			NetworkBehaviour networkBehaviour = (NetworkBehaviour) getActivePlayer().getBehaviour();
			networkBehaviour.requestActionAsync(this, getActivePlayer(), getValidActions(), action -> {
				if (action == null) {
					throw new RuntimeException("Behaviour " + getActivePlayer().getBehaviour().getName() + " selected NULL action while "
							+ getValidActions().size() + " actions were available");
				}
				performAction(getActivePlayerId(), action);
				callback.handle(action.getActionType() != ActionType.END_TURN);
			});
		} catch (NullPointerException e) {
			if (isRunning) {
				throw e;
			}
		}
	}

	@Override
	@Suspendable
	protected void onGameStateChanged() {
		updateClientsWithGameState();
	}

	public void updateClientsWithGameState() {
		GameState state = getGameStateCopy();
		getListenerMap().get(getPlayer1()).onUpdate(state);
		getListenerMap().get(getPlayer2()).onUpdate(state);
	}

	@Override
	@Suspendable
	public void fireGameEvent(GameEvent gameEvent) {
		eventCounter.incrementAndGet();
		final GameState gameStateCopy = getGameStateCopy();
		getListenerMap().get(getPlayer1()).onNotification(gameEvent, gameStateCopy);
		getListenerMap().get(getPlayer2()).onNotification(gameEvent, gameStateCopy);
		super.fireGameEvent(gameEvent, gameTriggers);
		if (eventCounter.decrementAndGet() == 0) {
			getListenerMap().get(getPlayer1()).lastEvent();
			getListenerMap().get(getPlayer2()).lastEvent();
		}
	}

	@Override
	@Suspendable
	public void onEnchantmentFired(Enchantment trigger) {
		super.onEnchantmentFired(trigger);

		TriggerFired triggerFired = new TriggerFired(this, trigger);
		final GameState gameStateCopy = getGameStateCopy();

		// If the trigger is in a private place, do not fire it for the public player
		if (trigger.getHostReference() != null) {
			Entity host = getEntities()
					.filter(e -> e.getId() == trigger.getHostReference().getId())
					.findFirst()
					.orElse(null);

			if (host != null && Zones.PRIVATE.contains(host.getZone())) {
				int owner = host.getOwner();
				getListenerMap().get(getPlayer(owner)).onNotification(triggerFired, gameStateCopy);
				return;
			}
		}

		getListenerMap().get(getPlayer2()).onNotification(triggerFired, gameStateCopy);
		getListenerMap().get(getPlayer1()).onNotification(triggerFired, gameStateCopy);
	}

	@Override
	public void onWillPerformGameAction(int playerId, GameAction action) {
		super.onWillPerformGameAction(playerId, action);

		final GameState gameStateCopy = getGameStateCopy();
		getListenerMap().get(getPlayer1()).onNotification(action, gameStateCopy);
		getListenerMap().get(getPlayer2()).onNotification(action, gameStateCopy);
	}

	/**
	 * Request an action from a {@link Writer} that corresponds to the given {@code playerId}.
	 *
	 * @param state    The game state to send.
	 * @param playerId The player ID to request from.
	 * @param actions  The valid actions to choose from.
	 * @param callback A handler for the response.
	 */
	@Suspendable
	@Override
	public void networkRequestAction(GameState state, int playerId, List<GameAction> actions, Handler<GameAction> callback) {
		String id = RandomStringUtils.randomAscii(8);
		logger.debug("{} networkRequestAction: Requesting actions {} with callback {} for playerId={} userId={}", getGameId(), actions, id, playerId, getPlayer(playerId).getUserId());
		final CallbackId callbackId = new CallbackId(id, playerId);
		requestCallbacks.put(callbackId, new GameplayRequest(GameplayRequestType.ACTION, state, actions, callback));
		// Send a state update for the other player too
		getListenerMap().get(getOpponent(getPlayer(playerId))).onUpdate(state);

		// The player's turn may have ended, so handle the action immediately in this case.
		if (timerElapsedForPlayerId == playerId) {
			getListenerMap().get(getPlayer(playerId)).onUpdate(state);
			requestCallbacks.remove(callbackId);
			processActionForElapsedTurn(actions, callback);
		} else {
			getListenerMap().get(getPlayer(playerId)).onRequestAction(id, state, actions);
		}


	}

	/**
	 * When a player's turn ends prematurely, this method will process a player's turn, choosing {@link
	 * ActionType#BATTLECRY} and {@link ActionType#DISCOVER} randomly and performing an {@link ActionType#END_TURN} as
	 * soon as possible.
	 *
	 * @param actions  The possible {@link GameAction} for this request.
	 * @param callback The callback for this request.
	 */
	@Suspendable
	@SuppressWarnings("unchecked")
	private void processActionForElapsedTurn(List<GameAction> actions, Handler callback) {
		// If the request contains an end turn action, execute it. Otherwise, choose an action
		// at random.
		final GameAction action = actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.END_TURN)
				.findFirst().orElse(getLogic().getRandom(actions));

		Sync.fiberHandler((Handler<GameAction>) callback).handle(action);
	}

	/**
	 * Request an action from the {@link Writer} that corresponds to the given {@code player}.
	 *
	 * @param player       The player to request from.
	 * @param starterCards The cards the player started with.
	 * @param callback     A handler for the response.
	 */
	@Override
	@Suspendable
	public void networkRequestMulligan(Player player, List<Card> starterCards, Handler<List<Card>> callback) {
		logger.debug("{} networkRequestMulligan: Requesting mulligan for playerId={} userId={}", getGameId(), player.getId(), player.getUserId());
		String id = RandomStringUtils.randomAscii(8);
		requestCallbacks.put(new CallbackId(id, player.getId()), new GameplayRequest(GameplayRequestType.MULLIGAN, starterCards, callback));
		getListenerMap().get(player).onMulligan(id, getGameStateCopy(), starterCards, player.getId());
	}

	/**
	 * Handles the chosen game action from a client.
	 *
	 * @param messageId The ID of the message used to request the action.
	 * @param action    The action chosen.
	 */
	@Suspendable
	@SuppressWarnings("unchecked")
	public void onActionReceived(String messageId, GameAction action) {
		// The action may have been removed due to the timer, so it's okay if it doesn't exist.
		if (!requestCallbacks.containsKey(CallbackId.of(messageId))) {
			return;
		}

		logger.debug("{} onActionReceived: Received action {} for callback {}", getGameId(), action, messageId);
		final Handler handler = requestCallbacks.get(CallbackId.of(messageId)).handler;
		requestCallbacks.remove(CallbackId.of(messageId));
		Sync.fiberHandler((Handler<GameAction>) handler).handle(action);
		logger.debug("{} onActionReceived: Executed action {} for callback {}", getGameId(), action, messageId);
	}

	/**
	 * Handles the cards that the player chose to discard.
	 *
	 * @param messageId      The ID of the message used to request the mulligan.
	 * @param player         The player that requested the mulligan.
	 * @param discardedCards The cards the player discarded.
	 */
	@SuppressWarnings("unchecked")
	@Suspendable
	public void onMulliganReceived(String messageId, Player player, List<Card> discardedCards) {
		// The mulligan might have been removed due to the timer, so it's okay if it doesn't exist.
		if (!requestCallbacks.containsKey(CallbackId.of(messageId))) {
			return;
		}

		logger.debug("{} onMulliganReceived: Mulligan {} received from userId={}", getGameId(), discardedCards, player.getUserId());
		final Handler handler = requestCallbacks.get(CallbackId.of(messageId)).handler;
		requestCallbacks.remove(CallbackId.of(messageId));
		((Handler<List<Card>>) handler).handle(discardedCards);
	}

	@Override
	public void sendGameOver(Player recipient, Player winner) {
		getListenerMap().get(recipient).onGameEnd(winner);
	}

	@Override
	protected void notifyPlayersGameOver() {
		for (Player player : getPlayers()) {
			NetworkBehaviour networkBehaviour = (NetworkBehaviour) player.getBehaviour();
			networkBehaviour.onGameOverAuthoritative(this, player.getId(), getWinner() != null ? getWinner().getId() : -1);
		}
	}

	@Override
	public String toString() {
		return String.format("[ServerGameContext gameId=%s turn=%d]", getGameId(), getTurn());
	}

	@Override
	public String getGameId() {
		return gameId;
	}

	public Map<Player, Writer> getListenerMap() {
		return Collections.unmodifiableMap(listenerMap);
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	public void onPlayerReconnected(Player player, Writer writer) {
		// Update the client
		setUpdateListener(player, writer);

		// Don't replace the player object! We don't need it
		// Resynchronize the game states
		if (player.getId() == PLAYER_1) {

		} else if (player.getId() == PLAYER_2) {

		}

		updateActivePlayers();
		onGameStateChanged();
		retryRequests(player);
	}

	@Suspendable
	@SuppressWarnings("unchecked")
	private void retryRequests(Player player) {
		List<Map.Entry<CallbackId, GameplayRequest>> requests = requestCallbacks.entrySet().stream().filter(e -> e.getKey().playerId == player.getId()).collect(Collectors.toList());
		if (requests.size() > 0) {
			requestCallbacks.entrySet().removeIf(e -> e.getKey().playerId == player.getId());
			requests.forEach(e -> {
				final GameplayRequest request = e.getValue();
				switch (request.type) {
					case ACTION:
						networkRequestAction(request.state, e.getKey().playerId, request.actions, request.handler);
						break;
					case MULLIGAN:
						networkRequestMulligan(getPlayer(e.getKey().playerId), request.starterCards, request.handler);
						break;
					default:
						logger.error("Unknown gameplay request was pending.");
						break;
				}
			});
		}
	}

	@Override
	@Suspendable
	public void endGame() {
		super.endGame();
		onGameEndHandlers.forEach(h -> {
			h.handle(this);
		});
	}

	@Suspendable
	public void handleEndGame(Handler<ServerGameContext> handler) {
		onGameEndHandlers.add(handler);
	}

	@Suspendable
	public void kill() {
		super.endGame();
		updateAndGetGameOver();
		isRunning = false;
		// Clear out even more stuff
		dispose();
	}

	@Override
	public void dispose() {
		super.dispose();
		// Clear out the request callbacks
		requestCallbacks.clear();
		// Clear the listeners
		listenerMap.clear();
		onGameEndHandlers.clear();
	}

	public List<Trigger> getGameTriggers() {
		return gameTriggers;
	}

	public GameAction getActionForMessage(String messageId, int actionIndex) {
		return requestCallbacks.get(CallbackId.of(messageId)).actions.get(actionIndex);
	}

	@Suspendable
	public void onMulliganReceived(String messageId, List<Integer> discardedCardIndices) {
		// Get the player reference
		final Optional<CallbackId> reqResult = requestCallbacks.keySet().stream().filter(ci -> ci.id.equals(messageId)).findFirst();
		if (!reqResult.isPresent()) {
			throw new RuntimeException();
		}
		CallbackId reqId = reqResult.get();
		GameplayRequest request = requestCallbacks.get(reqId);
		List<Card> discardedCards = discardedCardIndices.stream().map(i -> request.starterCards.get(i)).collect(Collectors.toList());
		onMulliganReceived(messageId, getPlayer(reqId.playerId), discardedCards);
	}

	public NetworkDelegate getNetworkDelegate() {
		return this;
	}

	@Override
	public Long getMillisRemaining() {
		if (timerStartTimeMillis == null
				|| timerLengthMillis == null) {
			return null;
		}

		return Math.max(0, timerLengthMillis - (System.currentTimeMillis() - timerStartTimeMillis));
	}
}