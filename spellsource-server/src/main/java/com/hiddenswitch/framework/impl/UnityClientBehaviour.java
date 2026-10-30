package com.hiddenswitch.framework.impl;

import com.google.common.base.Throwables;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
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
import net.demilich.metastone.game.logic.TurnState;
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
import static java.util.stream.Collectors.toList;
import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * The virtual-thread player verticle that bridges a human player's EventBus channels
 * ({@code games:reader:{userId}} and {@code games:writer:{userId}}) to the {@link Behaviour}
 * and {@link Client} contracts of a {@link ServerGameContext}.
 */
public class UnityClientBehaviour extends AbstractVirtualThreadVerticle implements Client {
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
	private final Server server;
	private final Deque<GameEvent> powerHistory = new ArrayDeque<>();
	private final AtomicBoolean inboundMessagesClosed = new AtomicBoolean(false);
	private final AtomicBoolean elapsed = new AtomicBoolean(false);
	private final AtomicBoolean conceded = new AtomicBoolean(false);
	private MessageConsumer<ClientToServerMessage> consumer;
	private RetryMessageProducer<ServerToClientMessage> writer;
	private Long turnTimer;
	private ActivityMonitor activityMonitor;
	private volatile Future<Void> lastWriteFuture = Future.succeededFuture();
	private volatile GameState lastStateSent;
	private volatile GameOver gameOver;
	private volatile boolean needsPowerHistory;

	public UnityClientBehaviour(Server server,
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

	@Override
	public void startVirtual() {
		var bus = vertx.eventBus();
		this.consumer = bus.consumer(Games.getMessagesFromClientAddress(userId), msg -> {
			try {
				handleWebSocketMessage(msg.body());
			} catch (Throwable t) {
				if (Throwables.getRootCause(t) instanceof InterruptedException) {
					return;
				}
				LOGGER.warn("error while handling message ", t);
			}
		});
		this.writer = new RetryMessageProducer<>(
				bus.publisher(Games.getMessagesFromServerAddress(userId)), 50, 100);
		this.turnTimer = vertx.setPeriodic(1000L, this::secondIntervalElapsed);
		this.activityMonitor = new ActivityMonitor(vertx, noActivityTimeout, this::noActivity);
		this.activityMonitor.activity();
		await(this.consumer.completion());
	}

	@Override
	public void stopVirtual() {
		closeInboundMessages();
		if (turnTimer != null && vertx != null) {
			vertx.cancelTimer(turnTimer);
		}
		cancelActivity();
		requests.clear();
		messageBuffer.clear();
		synchronized (powerHistory) {
			powerHistory.clear();
		}
		lastStateSent = null;
		if (consumer != null) {
			await(consumer.unregister().otherwiseEmpty());
		}
		if (writer != null) {
			await(writer.close().otherwiseEmpty());
		}
	}

	@Override
	public Future<Void> unregisterConsumer() {
		return consumer != null ? consumer.unregister().otherwiseEmpty() : Future.succeededFuture();
	}

	private void recordActivity() {
		if (activityMonitor != null) {
			activityMonitor.activity();
		}
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
		elapseAwaitingRequests();
		server.onConcede(this);
	}

	@Override
	public boolean isElapsed() {
		return elapsed.get();
	}

	@Override
	public UnityClientBehaviour setElapsed(boolean elapsed) {
		this.elapsed.set(elapsed);
		return this;
	}

	@Override
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
		if (inboundMessagesClosed.get()) {
			var exceptionHandler = vertx != null ? vertx.exceptionHandler() : null;
			if (exceptionHandler != null) {
				exceptionHandler.handle(new IllegalStateException("inbound messaged closed"));
			}
			return;
		}

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
				} else if (message.hasEntityUntouch()) {
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
		try {
			return (Behaviour) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "Unity client behaviour";
	}

	private <T> void submitRequest(GameplayRequest<T> request, GameContext context, boolean singleActiveRequest) {
		requestsLock.lock();
		try {
			if (isElapsed()) {
				request.elapse(server.getRandom());
				return;
			}
			if (singleActiveRequest && !requests.isEmpty()) {
				throw new IllegalStateException("requesting action outside resume() loop");
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
		submitRequest(new GameplayRequest.Mulligan(id, cards, next), context, false);
	}

	public void onMulliganReceived(List<Integer> discardedCardIndices) {
		GameplayRequest.Mulligan mulligan = null;
		requestsLock.lock();
		try {
			for (var request : requests) {
				if (request instanceof GameplayRequest.Mulligan m) {
					mulligan = m;
					requests.remove(request);
					break;
				}
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
		try {
			var id = Integer.toString(callbackIdCounter.getAndIncrement());
			submitRequest(new GameplayRequest.Action(id, validActions, promise::tryComplete), context, true);
			return await(promise.future());
		} catch (Throwable t) {
			if (t instanceof InterruptedException) {
				LOGGER.error("requestAction interrupted, likely due to conceding or timing out");
				throw t;
			} else {
				LOGGER.error("requestAction error:", t);
			}
			return null;
		}
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
						actionIndex, server.getGameId(), actionRequest.getActions() == null ? 0 : actionRequest.getActions().size());
				return;
			}
			requests.remove(actionRequest);
		} finally {
			requestsLock.unlock();
		}
		try {
			actionRequest.resolveIndex(actionIndex);
		} catch (Throwable recoverFromError) {
			LOGGER.warn("recovering from error while processing callback, gameId={}", server.getGameId(), recoverFromError);
			Tracing.error(recoverFromError);
			elapseAwaitingRequests();
		}
	}

	@Override
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
		cancelActivity();
		flush();
		var state = context != null ? context.getGameStateCopy() : null;
		var winner = context != null ? context.getWinner() : null;
		if (state == null || lastStateSent == null) {
			this.gameOver = GameOver.newBuilder()
					.setLocalPlayerWon(false).build();
			sendMessage(ServerToClientMessage.newBuilder()
					.setMessageType(MessageType.ON_GAME_END)
					.setGameOver(gameOver));
			return;
		}

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

	@Override
	public Future<Void> lastWriteFuture() {
		return lastWriteFuture;
	}

	@Override
	public void sendNotification(Notification event, GameState gameState) {
		if (!event.isClientInterested()) {
			return;
		}

		if (event instanceof TouchingNotification touchingNotification) {
			if (touchingNotification.getPlayerId() == playerId) {
				return;
			}
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
			var hasSource = source != null && source.getSourceCard() != null;
			if (hasSource && (source.isInPlay() || source.getOwner() == playerId)) {
				messageEvent.setSource(ModelConversions.getEntity(workingContext, source, playerId));
				triggerFired.setTriggerSourceId(source.getId());
			}
		} else if (event instanceof GameAction action) {
			var sourceEntity = event.getSource(workingContext);
			Entity.Builder source;

			if (sourceEntity != null && sourceEntity.getEntityType() == EntityType.CARD) {
				var card = (Card) sourceEntity;
				if (card.getCardType() == CardType.SPELL
						&& card.isSecret()
						&& card.getOwner() != playerId) {
					String[] cardClasses = card.getHeroClasses();
					source = ModelConversions.getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), cardClasses != null && cardClasses.length > 0 ? cardClasses : new String[]{HeroClass.ANY});
				} else {
					source = ModelConversions.getEntity(workingContext, card, playerId);
				}
			} else if (sourceEntity != null) {
				source = ModelConversions.getEntity(workingContext, sourceEntity, playerId);
			} else {
				source = null;
			}

			var owner = sourceEntity != null ? sourceEntity.getOwner() : gameState.getActivePlayerId();
			var targets = event.getTargets(workingContext, owner)
					.stream().map(e -> ModelConversions.getEntity(workingContext, e, playerId)).collect(Collectors.toList());
			var target = !targets.isEmpty() ? targets.get(0) : null;
			messageEvent
					.setDescription(event.getDescription(workingContext, playerId))
					.setIsSourcePlayerLocal(source == null ? gameState.getActivePlayerId() == playerId : source.getOwner() == playerId)
					.setIsTargetPlayerLocal(target != null && target.getOwner() == playerId)
					.setEventType(GameEventType.PERFORMED_GAME_ACTION)
					.setPerformedGameAction(GameEvent.PerformedGameActionMessage.newBuilder()
							.setActionType((ActionType) action.getActionType()).build());
			if (source != null) {
				messageEvent.setSource(source);
			}
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
		} else if (event instanceof net.demilich.metastone.game.events.GameEvent gameEvent) {
			messageEvent = ModelConversions.getClientEvent(workingContext, gameEvent, playerId);
		} else {
			throw new RuntimeException("Unsupported notification.");
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

	@Override
	public void onConnectionStarted(Player activePlayer) {
		needsPowerHistory = true;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public void sendEmote(int entityId, String emote) {
		Emote.EmoteMessage emoteMessage;
		try {
			emoteMessage = emote != null ? Emote.EmoteMessage.valueOf(emote) : Emote.EmoteMessage.AMAZING;
		} catch (IllegalArgumentException e) {
			emoteMessage = Emote.EmoteMessage.AMAZING;
		}
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.EMOTE)
				.setEmote(Emote.newBuilder()
						.setEntityId(entityId)
						.setMessage(emoteMessage)));
	}

	@Override
	public void lastEvent() {
		flush();
	}

	@Override
	public int getPlayerId() {
		return playerId;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public void closeInboundMessages() {
		inboundMessagesClosed.set(true);
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
