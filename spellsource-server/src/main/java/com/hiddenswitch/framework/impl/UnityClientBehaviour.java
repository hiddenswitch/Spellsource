package com.hiddenswitch.framework.impl;

import com.google.common.base.Throwables;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.*;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import com.hiddenswitch.spellsource.rpc.Spellsource.MessageTypeMessage.MessageType;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.events.DestroyWillQueue;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.events.TriggerFired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.vertx.await.Async.await;
import static io.vertx.await.Async.awaitAll;
import static java.util.stream.Collectors.toList;
import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * Bridges a human player's EventBus channels ({@code games:reader:{userId}} and
 * {@code games:writer:{userId}}) to the {@link Behaviour} contract of a
 * {@link ServerGameContext}, sharing the game's lifecycle domain rather than deploying a separate
 * per-user verticle actor.
 */
public class UnityClientBehaviour implements Behaviour {
	public static final int MAX_POWER_HISTORY_SIZE = 10;
	private static final Logger LOGGER = LoggerFactory.getLogger(UnityClientBehaviour.class);
	private final Queue<ServerToClientMessage.Builder> messageBuffer = new ConcurrentLinkedQueue<>();
	private final AtomicInteger eventCounter = new AtomicInteger();
	private final AtomicInteger callbackIdCounter = new AtomicInteger();
	private final Deque<GameplayRequest<?>> requests = new ConcurrentLinkedDeque<>();
	private final String userId;
	private final int playerId;
	private final long noActivityTimeout;
	private final Lock requestsLock = new ReentrantLock();
	private final ServerGameContext server;
	private final Deque<GameEvent> powerHistory = new ArrayDeque<>();
	private final AtomicBoolean elapsed = new AtomicBoolean(false);
	private final AtomicBoolean conceded = new AtomicBoolean(false);
	private Vertx vertx;
	private MessageConsumer<ClientToServerMessage> consumer;
	private RetryMessageProducer<ServerToClientMessage> writer;
	private Long turnTimer;
	private ActivityMonitor activityMonitor;
	private volatile Future<Void> lastWriteFuture = Future.succeededFuture();
	private volatile GameState lastStateSent;
	private volatile GameOver gameOver;
	private volatile boolean needsPowerHistory;

	public UnityClientBehaviour(ServerGameContext server,
	                            String userId,
	                            int playerId,
	                            long noActivityTimeout) {
		if (noActivityTimeout < 0L) {
			throw new IllegalArgumentException("noActivityTimeout must be positive");
		}
		this.userId = userId;
		this.playerId = playerId;
		this.server = server;
		this.noActivityTimeout = noActivityTimeout;
	}

	public void start(Vertx vertx) {
		this.vertx = vertx;
		var bus = vertx.eventBus();
		this.consumer = bus.consumer(Games.getMessagesFromClientAddress(userId), msg -> handleWebSocketMessage(msg.body()));
		this.writer = new RetryMessageProducer<>(
				bus.publisher(Games.getMessagesFromServerAddress(userId)), 50, 100);
		this.turnTimer = vertx.setPeriodic(1000L, this::secondIntervalElapsed);
		this.activityMonitor = new ActivityMonitor(vertx, noActivityTimeout, this::noActivity);
		this.activityMonitor.activity();
		if (Thread.currentThread().isVirtual()) {
			await(this.consumer.completion());
		}
	}

	public void stop() {
		if (turnTimer != null) {
			vertx.cancelTimer(turnTimer);
			turnTimer = null;
		}
		cancelActivity();
		requests.clear();
		messageBuffer.clear();
		synchronized (powerHistory) {
			powerHistory.clear();
		}
		lastStateSent = null;
		var futures = new ArrayList<Future<?>>(2);
		if (consumer != null) {
			futures.add(consumer.unregister().otherwiseEmpty());
			consumer = null;
		}
		if (writer != null) {
			futures.add(writer.close().otherwiseEmpty());
			writer = null;
		}
		if (!futures.isEmpty() && Thread.currentThread().isVirtual() && !Thread.currentThread().isInterrupted()) {
			awaitAll(futures);
		}
	}

	public Future<Void> unregisterConsumer() {
		cancelActivity();
		var c = consumer;
		consumer = null;
		return c != null ? c.unregister().otherwiseEmpty() : Future.succeededFuture();
	}

	private void recordActivity() {
		activityMonitor.activity();
	}

	private void cancelActivity() {
		if (activityMonitor != null) {
			activityMonitor.cancel();
		}
	}

	public static Spellsource.GameState.Builder getClientGameState(int playerId, GameState state) {
		return getClientGameState(playerId, GameContext.fromState(state));
	}

	public static Spellsource.GameState.Builder getClientGameState(int playerId, GameContext simulatedContext) {
		Player local;
		Player opponent;
		if (playerId == PLAYER_1) {
			local = simulatedContext.getPlayer1();
			opponent = simulatedContext.getPlayer2();
		} else if (playerId == PLAYER_2) {
			local = simulatedContext.getPlayer2();
			opponent = simulatedContext.getPlayer1();
		} else {
			throw new IllegalStateException("playerId");
		}
		simulatedContext.setIgnoreEvents(true);
		return ModelConversions.getGameState(simulatedContext, local, opponent);
	}

	private void secondIntervalElapsed(Long timer) {
		if (!server.isGameReady() || server.isGameOver() || lastStateSent == null) {
			return;
		}
		var millisRemaining = server.getMillisRemaining();
		if (millisRemaining == null) {
			return;
		}
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.TIMER)
				.setTimers(Timers.newBuilder()
						.setMillisRemaining(millisRemaining)));
	}

	private void noActivity(ActivityMonitor monitor) {
		server.onConcede(this);
	}

	public boolean isElapsed() {
		return elapsed.get();
	}

	public UnityClientBehaviour setElapsed(boolean elapsed) {
		this.elapsed.set(elapsed);
		return this;
	}

	public void elapseAwaitingRequests() {
		var toElapse = new ArrayList<GameplayRequest<?>>();
		requestsLock.lock();
		elapsed.set(true);
		try {
			GameplayRequest<?> request;
			LOGGER.warn("elapsing {} requests", requests.size());
			while ((request = requests.poll()) != null) {
				toElapse.add(request);
			}
		} finally {
			requestsLock.unlock();
		}
		for (var request : toElapse) {
			request.elapse(server.getRandom());
		}
	}

	protected void handleWebSocketMessage(ClientToServerMessage message) {
		switch (message.getMessageType()) {
			case PINGPONG:
				recordActivity();
				sendMessage(ServerToClientMessage.newBuilder().setMessageType(MessageType.PINGPONG).setId(message.getRepliesTo()));
				break;
			case FIRST_MESSAGE:
				LOGGER.trace("received first message");
				lastStateSent = null;
				recordActivity();
				if (server.isGameReady()) {
					server.onPlayerReconnected(this);
					retryRequests();
				} else {
					server.onPlayerReady(this);
				}
				break;
			case UPDATE_ACTION:
				recordActivity();
				onActionReceived(message.getRepliesTo(), message.getActionIndex());
				break;
			case UPDATE_MULLIGAN:
				recordActivity();
				onMulliganReceived(message.getDiscardedCardIndicesList());
				break;
			case EMOTE:
				recordActivity();
				server.onEmote(this, message.getEmote().getEntityId(), message.getEmote().getMessage().name());
				break;
			case TOUCH:
				recordActivity();
				if (message.hasEntityTouch()) {
					server.onTouch(this, message.getEntityTouch());
				} else {
					server.onUntouch(this, message.getEntityUntouch());
				}
				break;
			case CONCEDE:
				if (this.conceded.compareAndSet(false, true)) {
					server.onConcede(this);
				}
				break;
		}
	}

	public void retryRequests() {
		requestsLock.lock();
		try {
			for (var request : requests) {
				request.send(this, lastStateSent);
			}
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	public Behaviour clone() {
		return this;
	}

	@Override
	public String getName() {
		return "Unity client behaviour";
	}

	private <T> void submitRequest(GameplayRequest<T> request, GameContext context) {
		requestsLock.lock();
		try {
			if (isElapsed()) {
				request.elapse(server.getRandom());
				return;
			}
			requests.add(request);
			request.send(this, context.getGameStateCopy());
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		var promise = Promise.<List<Card>>promise();
		mulliganAsync(context, player, cards, promise::tryComplete);
		return await(promise.future());
	}

	@Override
	public void mulliganAsync(GameContext context, Player player, List<Card> cards, Consumer<List<Card>> next) {
		var id = Integer.toString(callbackIdCounter.getAndIncrement());
		submitRequest(new GameplayRequest.Mulligan(id, cards, next), context);
	}

	public void onMulliganReceived(List<Integer> discardedCardIndices) {
		GameplayRequest.Mulligan mulligan = null;
		requestsLock.lock();
		try {
			if (requests.peekFirst() instanceof GameplayRequest.Mulligan) {
				mulligan = (GameplayRequest.Mulligan) requests.pollFirst();
			}
		} finally {
			requestsLock.unlock();
		}
		if (mulligan != null) {
			mulligan.resolveIndices(discardedCardIndices);
		}
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		var promise = Promise.<GameAction>promise();
		var id = Integer.toString(callbackIdCounter.getAndIncrement());
		submitRequest(new GameplayRequest.Action(id, validActions, promise::tryComplete), context);
		var action = await(promise.future());
		if (!server.isRunning()) {
			throw new java.util.concurrent.CancellationException("game ended");
		}
		return action;
	}

	public void onActionReceived(String messageId, int actionIndex) {
		GameplayRequest.Action actionRequest = null;
		requestsLock.lock();
		try {
			for (var request : requests) {
				if (request.getCallbackId().equals(messageId) && request instanceof GameplayRequest.Action a) {
					actionRequest = a;
					break;
				}
			}
			if (actionRequest == null) {
				LOGGER.warn("unexpectedly received a null request");
				return;
			}
			if (!actionRequest.isValidIndex(actionIndex)) {
				LOGGER.warn("ignoring out-of-bounds actionIndex={} for gameId={}, actions.size={}",
						actionIndex, server.getGameId(), actionRequest.getActions().size());
				return;
			}
			requests.remove(actionRequest);
		} finally {
			requestsLock.unlock();
		}
		actionRequest.resolveIndex(actionIndex);
	}

	@Override
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
		cancelActivity();
		flush();
		var state = context.getGameStateCopy();
		var winner = context.getWinner();
		var gameState = getClientGameState(this.playerId, state);
		var gameOverBuilder = GameOver.newBuilder();
		if (winner == null) {
			gameOverBuilder.setLocalPlayerWon(false);
		} else {
			gameOverBuilder.setLocalPlayerWon(winner.getId() == this.playerId)
					.setWinningPlayerId(winner.getId());
		}
		gameOver = gameOverBuilder.build();

		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.ON_GAME_END)
				.setChanges(visibleEntities(state))
				.setGameState(gameState)
				.setGameOver(gameOver));
	}

	private void sendMessage(ServerToClientMessage.Builder message) {
		sendMessage(writer, message);
	}

	private void sendMessage(MessageProducer<ServerToClientMessage> socket, ServerToClientMessage.Builder message) {
		message.setLocalPlayerId(playerId);
		switch (message.getMessageType()) {
			case TOUCH:
			case EMOTE:
			case PINGPONG:
			case TIMER:
				break;
			default:
				if (gameOver != null) {
					message.setGameOver(gameOver);
				}
		}
		this.lastWriteFuture = socket.write(message.build()).onFailure(Environment.onFailure());
	}

	public Future<Void> lastWriteFuture() {
		return lastWriteFuture;
	}

	public void sendNotification(Notification event, GameState gameState) {
		if (event instanceof TouchingNotification touchingNotification) {
			var id = touchingNotification.getEntityReference().getId();
			var event1 = GameEvent.newBuilder()
					.setEventType(touchingNotification.isTouched() ? GameEventType.ENTITY_TOUCHED : GameEventType.ENTITY_UNTOUCHED);
			if (touchingNotification.isTouched()) {
				event1.setEntityTouched(id);
			} else {
				event1.setEntityUntouched(id);
			}
			sendMessage(ServerToClientMessage.newBuilder()
					.setMessageType(MessageType.TOUCH)
					.setEvent(event1.build()));
			return;
		}

		var workingContext = GameContext.fromState(gameState);
		var message = ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.ON_GAME_EVENT)
				.setChanges(visibleEntities(gameState))
				.setGameState(getClientGameState(playerId, workingContext));
		var messageEvent = GameEvent.newBuilder();
		if (event instanceof TriggerFired triggerEvent) {
			var triggerFired = GameEvent.TriggerFiredMessage.newBuilder()
					.setTriggerSourceId(triggerEvent.getEnchantment().getHostReference().getId());
			messageEvent
					.setEventType(GameEventType.TRIGGER_FIRED)
					.setTriggerFired(triggerFired);
			var source = triggerEvent.getSource();
			if (source != null && (source.isInPlay() || source.getOwner() == playerId)) {
				messageEvent.setSource(ModelConversions.getEntity(workingContext, source, playerId));
				triggerFired.setTriggerSourceId(source.getId());
			}
		} else if (event instanceof GameAction action) {
			var sourceEntity = event.getSource(workingContext);
			Entity.Builder source;
			if (sourceEntity instanceof Card card
					&& card.getCardType() == CardType.SPELL
					&& card.isSecret()
					&& card.getOwner() != playerId) {
				source = ModelConversions.getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClasses());
			} else {
				source = ModelConversions.getEntity(workingContext, sourceEntity, playerId);
			}

			var targets = event.getTargets(workingContext, sourceEntity.getOwner())
					.stream().map(e -> ModelConversions.getEntity(workingContext, e, playerId)).collect(Collectors.toList());
			var target = !targets.isEmpty() ? targets.get(0) : null;
			messageEvent
					.setDescription(event.getDescription(workingContext, playerId))
					.setIsSourcePlayerLocal(source.getOwner() == playerId)
					.setIsTargetPlayerLocal(target != null && target.getOwner() == playerId)
					.setEventType(GameEventType.PERFORMED_GAME_ACTION)
					.setPerformedGameAction(GameEvent.PerformedGameActionMessage.newBuilder()
							.setActionType((ActionType) action.getActionType()).build())
					.setSource(source);
			if (target != null) {
				messageEvent.setTarget(target);
			}
		} else if (event instanceof DestroyWillQueue destroyWillQueue) {
			messageEvent
					.setDescription(destroyWillQueue.getDescription(workingContext, playerId))
					.setIsSourcePlayerLocal(workingContext.getActivePlayerId() == playerId)
					.setIsTargetPlayerLocal(false)
					.setEventType(GameEventType.DESTROY_WILL_QUEUE)
					.setDestroy(GameEvent.DestroyMessage.newBuilder()
							.addAllObjects(destroyWillQueue.getDestroys()
									.stream()
									.map(destroy -> Destroy.newBuilder()
											.setSource(ModelConversions.getEntity(workingContext, destroy.getSource(), playerId))
											.setTarget(ModelConversions.getEntity(workingContext, destroy.getTarget(), playerId))
											.addAllAftermaths(destroy.getAftermaths().stream().map(aftermath -> ModelConversions.getEntity(workingContext, aftermath, playerId).build()).collect(toList())).build())
									.collect(toList())));
		} else {
			messageEvent = ModelConversions.getClientEvent(workingContext, (net.demilich.metastone.game.events.GameEvent) event, playerId);
		}

		messageEvent.setIsPowerHistory(event.isPowerHistory());
		if (event.isPowerHistory()) {
			messageEvent.setId(eventCounter.getAndIncrement());
			if (!(event instanceof net.demilich.metastone.game.events.GameEvent)) {
				messageEvent.setDescription(event.getDescription(workingContext, playerId));
			}
			var build = messageEvent.build();
			synchronized (powerHistory) {
				if (powerHistory.size() > MAX_POWER_HISTORY_SIZE) {
					powerHistory.pop();
				}
				powerHistory.add(build);
			}
		}

		message.setEvent(messageEvent);
		messageBuffer.offer(message);
	}

	public void onConnectionStarted(Player activePlayer) {
		needsPowerHistory = true;
	}

	public void onUpdate(GameState state) {
		flush();
		var gameState = getClientGameState(playerId, state);
		if (needsPowerHistory) {
			synchronized (powerHistory) {
				gameState.addAllPowerHistory(powerHistory);
			}
			gameState.setHasPowerHistory(true);
			needsPowerHistory = false;
		}
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.ON_UPDATE)
				.setChanges(visibleEntities(state))
				.setGameState(gameState));
	}

	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		flush();
		for (var i = 0; i < availableActions.size(); i++) {
			availableActions.get(i).setId(i);
		}
		var workingContext = GameContext.fromState(state);
		sendMessage(ServerToClientMessage.newBuilder()
				.setId(id)
				.setMessageType(MessageType.ON_REQUEST_ACTION)
				.setChanges(visibleEntities(state))
				.setGameState(getClientGameState(playerId, workingContext))
				.setActions(ModelConversions.getClientActions(workingContext, availableActions, playerId)));
	}

	public void onMulligan(String id, GameState state, List<Card> cards, int playerId) {
		flush();
		var simulatedContext = GameContext.fromState(state);
		sendMessage(ServerToClientMessage.newBuilder()
				.setId(id)
				.setMessageType(MessageType.ON_MULLIGAN)
				.setChanges(visibleEntities(state))
				.setGameState(getClientGameState(playerId, simulatedContext))
				.addAllStartingCards(cards.stream().map(c -> ModelConversions.getEntity(simulatedContext, c, playerId).build()).collect(Collectors.toList())));
	}

	public void sendEmote(int entityId, String emote) {
		var emoteMessage = com.google.common.base.Enums
				.getIfPresent(Emote.EmoteMessage.class, Objects.requireNonNullElse(emote, ""))
				.or(Emote.EmoteMessage.AMAZING);
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.EMOTE)
				.setEmote(Emote.newBuilder()
						.setEntityId(entityId)
						.setMessage(emoteMessage)));
	}

	public void lastEvent() {
		flush();
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getUserId() {
		return userId;
	}

	private void flush() {
		while (!messageBuffer.isEmpty()) {
			sendMessage(messageBuffer.poll());
		}
	}

	private EntityChangeSet visibleEntities(GameState current) {
		var changes = ModelConversions.visibleEntities(current);
		lastStateSent = current;
		return changes;
	}

	@Override
	public boolean isHuman() {
		return true;
	}

	@Override
	public String toString() {
		return "UnityClientBehaviour{" +
				"userId=" + userId +
				", playerId=" + playerId +
				", server=" + server +
				'}';
	}
}
