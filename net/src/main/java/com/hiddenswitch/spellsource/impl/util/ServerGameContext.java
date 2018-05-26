package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.common.*;
import com.hiddenswitch.spellsource.impl.TimerId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.models.GetCollectionResponse;
import com.hiddenswitch.spellsource.models.LogicGetDeckRequest;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.NetworkDelegate;
import net.demilich.metastone.game.utils.TurnState;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
	private Map<Player, Writer> listenerMap = new ConcurrentHashMap<>();
	private final Map<CallbackId, GameplayRequest> requestCallbacks = new ConcurrentHashMap<>();
	private boolean isRunning = true;
	private final transient HashSet<Handler<ServerGameContext>> onGameEndHandlers = new HashSet<>();
	private final List<Trigger> gameTriggers = new ArrayList<>();
	private final transient RpcClient<Logic> logic;
	private final Scheduler scheduler;
	private AtomicInteger eventCounter = new AtomicInteger(0);
	private int timerElapsedForPlayerId;
	private Long timerStartTimeMillis;
	private Long timerLengthMillis;
	private TimerId turnTimerId;

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
		enableTriggers();
	}

	/**
	 * Enables this match to track persistence effects.
	 *
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	private void enablePersistenceEffects() {
		this.getGameTriggers().add(new PersistenceTrigger(this, this.gameId));
	}

	/**
	 * Enables this match to use custom networked triggers
	 */
	private void enableTriggers() {
		for (com.hiddenswitch.spellsource.Trigger trigger : Spellsource.spellsource().getGameTriggers().values()) {
			final Map<SpellArg, Object> arguments = new SpellDesc(DelegateSpell.class);
			arguments.put(SpellArg.NAME, trigger.getSpellId());
			SpellDesc spell = new SpellDesc(arguments);
			final Enchantment e = new Enchantment(trigger.getEventTriggerDesc().create(), spell);
			e.setOwner(0);
			this.getGameTriggers().add(e);
		}
	}

	@Override
	public GameLogicAsync getLogic() {
		return (GameLogicAsync) super.getLogic();
	}

	public void setUpdateListener(Player player, Writer listener) {
		listenerMap.put(player, listener);
	}

	@Override
	@Suspendable
	public void init() {
		logger.debug("{} networkedPlay: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
		getLogic().contextReady();
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		setActivePlayerId(getPlayer(startingPlayerId).getId());

		logger.debug("{} networkedPlay: Updating active players", getGameId());
		updateActivePlayers();
		getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));

		// Make sure the players are initialized before sending the original player updates.
		getLogic().initializePlayer(IdFactory.PLAYER_1);
		getLogic().initializePlayer(IdFactory.PLAYER_2);
		logger.debug("{} networkedPlay: Players initialized", getGameId());

		Future<Void> init1 = Future.future();
		Future<Void> init2 = Future.future();

		// Set the mulligan timer
		final TimerId mulliganTimerId;
		if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
			timerLengthMillis = getLogic().getMulliganTimeMillis();
			timerStartTimeMillis = System.currentTimeMillis();
			mulliganTimerId = scheduler.setTimer(timerLengthMillis, Sync.fiberHandler(this::endMulligans));
		} else {
			logger.debug("{} networkPlay: No mulligan timer set for game because all players are not human", getGameId());
			timerLengthMillis = null;
			timerStartTimeMillis = null;
			mulliganTimerId = null;
		}

		updateClientsWithGameState();

		getLogic().initAsync(getActivePlayerId(), true, p -> init1.complete());
		getLogic().initAsync(getOpponent(getActivePlayer()).getId(), false, p -> init2.complete());

		// Mulligan simultaneously now
		try {
			CompositeFuture done = Sync.awaitResult(h -> CompositeFuture.all(init1, init2).setHandler(h), 2 * GameLogic.DEFAULT_MULLIGAN_TIME * 1000);
		} catch (VertxException ex) {
			logger.error("{} init: Failed to mulligan due to unknown error: {}", getGameId(), ex);
		}

		finishMulliganTimer(mulliganTimerId);
	}

	@Override
	@Suspendable
	public void startTurn(int playerId) {
		processTurnTimers(getActivePlayerId());

		super.startTurn(playerId);
		GameState state = new GameState(this, TurnState.TURN_IN_PROGRESS);
		getListenerMap().get(getPlayer1()).onUpdate(state);
		getListenerMap().get(getPlayer2()).onUpdate(state);
	}

	@Suspendable
	public void endTurn() {
		super.endTurn();
		if (turnTimerId != null) {
			scheduler.cancelTimer(turnTimerId);
		}
		getListenerMap().get(getPlayer1()).onTurnEnd(getActivePlayer(), getTurn(), getTurnState());
		getListenerMap().get(getPlayer2()).onTurnEnd(getActivePlayer(), getTurn(), getTurnState());
	}

	private Player getNonActivePlayer() {
		return getOpponent(getActivePlayer());
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

	@Override
	@Suspendable
	public void resume() {
		while (!updateAndGetGameOver()) {
			startTurn(getActivePlayerId());
			while (takeActionInTurn()) {
				if (!isRunning) {
					break;
				}
			}
			if (getTurn() > GameLogic.TURN_LIMIT) {
				break;
			}
		}
		endGame();
	}

	@Suspendable
	private void finishMulliganTimer(TimerId mulliganTimerId) {
		logger.debug("{} networkPlay: Received mulligans", getGameId());
		if (mulliganTimerId != null) {
			scheduler.cancelTimer(mulliganTimerId);
		}
	}

	@Suspendable
	private void processTurnTimers(int activePlayerId) {
		// Start the turn timer
		if (turnTimerId != null) {
			scheduler.cancelTimer(turnTimerId);
		}
		timerElapsedForPlayerId = -1;
		if (getBehaviours().get(getNonActivePlayer().getId()).isHuman()) {
			timerLengthMillis = (long) getTurnTimeForPlayer(activePlayerId);
			timerStartTimeMillis = System.currentTimeMillis();

			turnTimerId = scheduler.setTimer(timerLengthMillis, Sync.fiberHandler(this::elapseTurn));
		} else {
			timerLengthMillis = null;
			timerStartTimeMillis = null;
			logger.debug("{} networkedPlay: Not setting timer because opponent is not human.", getGameId());
		}
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

	@Suspendable
	protected void updateActivePlayers() {
		getListenerMap().get(getActivePlayer()).onActivePlayer(getActivePlayer());
		getListenerMap().get(getNonActivePlayer()).onActivePlayer(getActivePlayer());
	}

	@Override
	@Suspendable
	protected void onGameStateChanged() {
		updateClientsWithGameState();
	}

	@Suspendable
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
		if (!Fiber.isCurrentFiber()) {
			Sync.fiberHandler((Handler<GameAction>) handler).handle(action);
		} else {
			((Handler<GameAction>) handler).handle(action);
		}
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
		getListenerMap().get(recipient).onGameEnd(getGameStateCopy(), winner);
	}

	@Override
	protected void notifyPlayersGameOver() {
		for (int i = 0; i < 2; i++) {
			Behaviour networkBehaviour = getBehaviours().get(i);
			networkBehaviour.onGameOverAuthoritative(this, i, getWinner() != null ? getWinner().getId() : -1);
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

	/**
	 * Retrieves the deck using the player's {@link Player#getUserId()}
	 *
	 * @param player The player whose deck collections should be queried.
	 * @param name   The name of the deck to retrieve
	 * @return A {@link Deck} with valid but not located entities, or {@code null} if the deck could not be found.
	 */
	@Override
	@Suspendable
	public Deck getDeck(Player player, String name) {
		GetCollectionResponse response = Logic.getDeck(new LogicGetDeckRequest()
				.withUserId(new UserId(player.getUserId()))
				.withDeckName(name));

		if (response.equals(GetCollectionResponse.empty())) {
			return null;
		}

		return response.asDeck(player.getUserId());
	}
}