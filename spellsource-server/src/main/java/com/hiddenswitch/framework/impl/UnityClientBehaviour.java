package com.hiddenswitch.framework.impl;

import com.google.protobuf.Int32Value;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.*;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import com.hiddenswitch.spellsource.rpc.Spellsource.MessageTypeMessage.MessageType;
import io.opentracing.util.GlobalTracer;
import io.vertx.await.Async;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.DestroyWillQueue;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.await.Async.await;
import static java.util.stream.Collectors.toList;
import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * Represents a behaviour that converts requests from {@link ActionListener} and game event updates from
 * {@link EventListener} into messages on a {@link ReadStream} and {@link WriteStream}, decoding the read buffers as
 * {@link ClientToServerMessage} and encoding the sent buffers with {@link ServerToClientMessage}.
 */
public class UnityClientBehaviour extends UtilityBehaviour implements Client, Closeable, HasElapsableTurns {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnityClientBehaviour.class);
	public static final int MAX_POWER_HISTORY_SIZE = 10;

	private final Queue<ServerToClientMessage.Builder> messageBuffer = new ConcurrentLinkedQueue<>();
	private final AtomicInteger eventCounter = new AtomicInteger();
	private final AtomicInteger callbackIdCounter = new AtomicInteger();
	private final Deque<GameplayRequest> requests = new ConcurrentLinkedDeque<>();
	private final List<ActivityMonitor> activityMonitors = new ArrayList<>();
	private final String userId;
	private final int playerId;
	private final Scheduler scheduler;
	private final ReentrantLock requestsLock = new ReentrantLock();
	private final MessageProducer<ServerToClientMessage> writer;
	private final Server server;
	private final Long turnTimer;
	private final Deque<GameEvent> powerHistory = new ArrayDeque<>();
	private final Async async;

	private GameState lastStateSent;
	private boolean inboundMessagesClosed;
	private boolean elapsed;
	private GameOver gameOver;
	private boolean needsPowerHistory;
	private Thread readingThread;


	public UnityClientBehaviour(Server server,
	                            Scheduler scheduler,
	                            ReadStream<ClientToServerMessage> reader,
	                            MessageProducer<ServerToClientMessage> writer,
	                            String userId,
	                            int playerId,
	                            long noActivityTimeout) {
		this.scheduler = scheduler;
		this.writer = writer;
		this.userId = userId;
		this.playerId = playerId;
		this.server = server;
		this.turnTimer = scheduler.setInterval(1000L, this::secondIntervalElapsed);
		this.async = Environment.async();
		if (noActivityTimeout > 0L) {
			var activityMonitor = new ActivityMonitor(scheduler, noActivityTimeout, this::noActivity, null);
			activityMonitor.activity();
			getActivityMonitors().add(activityMonitor);
		} else if (noActivityTimeout < 0L) {
			throw new IllegalArgumentException("noActivityTimeout must be positive");
		}


		var queue = new LinkedBlockingQueue<ClientToServerMessage>();
		reader.handler(queue::offer);
		async.run(v -> {
			readingThread = Thread.currentThread();
			readingThread.setName("UnityClientBehavior/handleWebSocketMessage{userId=%s}".formatted(userId));
			while (true) {
				try {
					var message = queue.take();
					this.handleWebSocketMessage(message);
				} catch (InterruptedException e) {
					// peacefully closing
					break;
				} catch (Throwable t) {
					LOGGER.warn("unitybehavior did error with ", t);
				}
			}
		});
	}

	private void secondIntervalElapsed(Long timer) {
		var millisRemaining = server.getMillisRemaining();
		if (millisRemaining == null) {
			return;
		}

		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.TIMER)
				.setTimers(Timers.newBuilder()
						.setMillisRemaining(millisRemaining)));
	}


	private void noActivity(ActivityMonitor activityMonitor) {
		elapseAwaitingRequests();
		server.onConcede(this);
	}

	@Override
	public boolean isElapsed() {
		return elapsed;
	}

	@Override
	public UnityClientBehaviour setElapsed(boolean elapsed) {
		this.elapsed = elapsed;
		return this;
	}

	/**
	 * Elapses this client's turn, typically due to a timeout.
	 */
	@Override
	public void elapseAwaitingRequests() {
		requestsLock.lock();
		elapsed = true;
		try {
			// Prevent concurrent modification by locking access to this iterator
			GameplayRequest request;
			while ((request = requests.poll()) != null) {
				if (request.getType() == GameplayRequestType.ACTION) {
					@SuppressWarnings("unchecked")
					var callback = (Consumer<GameAction>) request.getCallback();
					processActionForElapsedTurn(request.getActions(), callback);
				} else if (request.getType() == GameplayRequestType.MULLIGAN) {
					@SuppressWarnings("unchecked")
					var handler = (Consumer<List<Card>>) request.getCallback();
					handler.accept(new ArrayList<>());
				}
			}
		} finally {
			requestsLock.unlock();
		}
	}


	private GameplayRequest getMulliganRequest() {
		requestsLock.lock();
		try {
			for (var request : getRequests()) {
				if (request.getType() == GameplayRequestType.MULLIGAN) {
					return request;
				}
			}
			return null;
		} finally {
			requestsLock.unlock();
		}
	}


	private GameplayRequest getRequest(String messageId) {
		requestsLock.lock();
		try {
			for (var request : getRequests()) {
				if (request.getCallbackId().equals(messageId)) {
					return request;
				}
			}
			return null;
		} finally {
			requestsLock.unlock();
		}
	}

	/**
	 * Handles a web socket message (message from the event bus), decoding it into a {@link ClientToServerMessage}.
	 *
	 * @param message The buffer containing the JSON of the message.
	 */

	protected void handleWebSocketMessage(ClientToServerMessage message) {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("UnityClientBehaviour/handleWebSocketMessage")
				.withTag("message.messageType", message.getMessageType().name())
				.withTag("playerId", getPlayerId())
				.withTag("userId", getUserId())
				.withTag("gameId", server.getGameId())
				.asChildOf(server.getSpanContext())
				.start();
		try (var ignored = tracer.activateSpan(span)) {
			if (inboundMessagesClosed) {
				Tracing.error(new IllegalStateException("inbound messages closed"), span, false);
				return;
			}

			switch (message.getMessageType()) {
				case PINGPONG:
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					// Server is responsible for replying
					sendMessage(ServerToClientMessage.newBuilder().setMessageType(MessageType.PINGPONG));
					break;
				case FIRST_MESSAGE:
					LOGGER.trace("received first message");
					lastStateSent = null;
					// The first message indicates the player has connected or reconnected.
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}


					if (server.isGameReady()) {
						// Replace the client
						span.log("receiveFirstMessage/reconnected");
						server.onPlayerReconnected(this);
						// Since the player may have pending requests, we're going to send the data the client needs again.
						retryRequests();
					} else {
						span.log("receiveFirstMessage/playerReady");
						server.onPlayerReady(this);
					}
					break;
				case UPDATE_ACTION:
					// Indicates the player has made a choice about which action to take.
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					final var messageId = message.getRepliesTo();
					onActionReceived(messageId, message.getActionIndex());
					break;
				case UPDATE_MULLIGAN:
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					onMulliganReceived(message.getDiscardedCardIndicesList());
					break;
				case EMOTE:
					server.onEmote(this, message.getEmote().getEntityId(), message.getEmote().getMessage().toString());
					break;
				case TOUCH:
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					if (message.hasEntityTouch()) {
						server.onTouch(this, message.getEntityTouch().getValue());
					} else if (message.hasEntityUntouch()) {
						server.onUntouch(this, message.getEntityUntouch().getValue());
					}
					break;
				case CONCEDE:
					server.onConcede(this);
					break;
			}
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
		}
	}


	protected void retryRequests() {
		requestsLock.lock();
		try {
			for (var request : getRequests()) {
				switch (request.getType()) {
					case ACTION:
						onRequestAction(request.getCallbackId(), lastStateSent, request.getActions());
						break;
					case MULLIGAN:
						onMulligan(request.getCallbackId(), lastStateSent, request.getStarterCards(), playerId);
						break;
				}
			}
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	public String getName() {
		return "Unity client behaviour";
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
		requestsLock.lock();
		try {
			getRequests().add(new GameplayRequest()
					.setCallbackId(id)
					.setType(GameplayRequestType.MULLIGAN)
					.setStarterCards(cards)
					.setCallback(next));
			onMulligan(id, context.getGameStateCopy(), cards, playerId);
		} finally {
			requestsLock.unlock();
		}
	}

	/**
	 * Handles a mulligan from a Unity client.
	 * <p>
	 * The starting hand is also sent in the
	 * {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#SET_ASIDE_ZONE}, where the index in the set
	 * aside zone corresponds to the index that should be sent to discard.
	 *
	 * @param discardedCardIndices A list of indices in the list of starter cards that should be discarded.
	 */

	public void onMulliganReceived(List<Integer> discardedCardIndices) {
		requestsLock.lock();
		try {
			var request = getMulliganRequest();
			if (request == null) {
				// The game may have ended, a mulligan is being received twice, or the game was conceded.
				return;
			}

			getRequests().remove(request);
			if (discardedCardIndices == null) {
				// the mulligan is being received twice
				discardedCardIndices = Collections.emptyList();
			}
			var discardedCards = discardedCardIndices
					.stream()
					.flatMap(i -> {
						if (request.getStarterCards() == null || request.getStarterCards().size() <= i) {
							// We already processed this card
							return Stream.empty();
						}
						return Stream.of(request.getStarterCards().get(i));
					})
					.collect(toList());

			@SuppressWarnings("unchecked")
			Consumer<List<Card>> callback = (Consumer<List<Card>>) request.getCallback();
			callback.accept(discardedCards);
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		var promise = Promise.<GameAction>promise();
		requestActionAsync(context, player, validActions, promise::tryComplete);
		return await(promise.future());
	}

	/**
	 * If there is an existing action request, update the available actions the player can take.
	 * <p>
	 * Because actions are requested in a blocking {@code resume()} loop on the {@link ServerGameContext#play(boolean)}
	 * strand, changing the actions that are requested requires (1) notifying the client of new actions and (2) replacing
	 * the existing valid game actions. The request callback ID is not changed, because the player is still responding to
	 * the same request for actions, just for different actions.
	 *
	 * @param context the game context
	 * @param actions the new actions
	 */
	public void updateActions(GameContext context, List<GameAction> actions) {
		requestsLock.lock();
		try {
			if (requests.isEmpty()) {
				throw new RuntimeException("no existing requests");
			}

			var request = getRequests().getLast();
			if (request.getType() == GameplayRequestType.ACTION) {
				request.setActions(actions);
				onRequestAction(request.getCallbackId(), context.getGameStateCopy(), actions);
			}
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	public void requestActionAsync(GameContext context, Player player, List<GameAction> actions, Consumer<GameAction> callback) {
		requestsLock.lock();
		try {
			var id = Integer.toString(callbackIdCounter.getAndIncrement());
			var request = new GameplayRequest()
					.setCallbackId(id)
					.setType(GameplayRequestType.ACTION)
					.setActions(actions)
					.setCallback(callback);

			// The player's turn may have ended, so handle the action immediately in this case.
			if (isElapsed()) {
				processActionForElapsedTurn(actions, callback);
			} else {
				// If there is an existing action, it's almost definitely an error, because we should only be requesting actions
				// inside a resume() loop
				if (!getRequests().isEmpty()) {
					throw new RuntimeException("requesting action outside resume() loop");
				}
				var state = context.getGameStateCopy();
				getRequests().add(request);
				onRequestAction(id, state, actions);
			}
		} finally {
			requestsLock.unlock();
		}
	}


	/**
	 * When a player's turn ends prematurely, this method will process a player's turn, choosing
	 * {@link ActionType#BATTLECRY} and {@link ActionType#DISCOVER} randomly and performing an {@link ActionType#END_TURN}
	 * as soon as possible.
	 *
	 * @param actions  The possible {@link GameAction} for this request.
	 * @param callback The callback for this request.
	 */

	private void processActionForElapsedTurn(List<GameAction> actions, Consumer<GameAction> callback) {
		// If the request contains an end turn action, execute it. Otherwise, choose an action
		// at random.
		final var action = actions.stream()
				.filter(ga -> (ActionType) ga.getActionType() == ActionType.BATTLECRY)
				.findFirst().orElse(getRandom(actions));

		if (action == null) {
			throw new IllegalStateException("No action was returned");
		}

		if (Thread.currentThread().isVirtual()) {
			callback.accept(action);
		} else {
			throw new UnsupportedOperationException("should not process elapsed turn outside of fiber");
		}
	}

	private GameAction getRandom(List<GameAction> actions) {
		// Consume a game logic random if possible, otherwise generate a random number
		if (server.getRandom() != null) {
			return actions.get(server.getRandom().nextInt(actions.size()));
		}
		return actions.get(ThreadLocalRandom.current().nextInt(actions.size()));
	}


	/**
	 * Handles the chosen game action from a client.
	 *
	 * @param messageId   The ID of the message used to request the action.
	 * @param actionIndex The action chosen.
	 */

	public void onActionReceived(String messageId, int actionIndex) {
		// The action may have been removed due to the timer or because the game ended, so it's okay if it doesn't exist.
		var request = getRequest(messageId);
		if (request == null) {
			return;
		}

		requestsLock.lock();
		try {
			var action = request.getActions().get(actionIndex);

			@SuppressWarnings("unchecked")
			Consumer<GameAction> callback = (Consumer<GameAction>) request.getCallback();
			callback.accept(action);
		} catch (Throwable recoverFromError) {
			LOGGER.warn("recovering from error while processing callback, gameId={}", server.getGameId(), recoverFromError);
			Tracing.error(recoverFromError);

			elapseAwaitingRequests();
		} finally {
			getRequests().remove(request);
			requestsLock.unlock();
		}
	}

	@Override
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
		for (var activityMonitor : getActivityMonitors()) {
			activityMonitor.cancel();
		}
		sendGameOver(context.getGameStateCopy(), context.getWinner());
	}


	private void sendMessage(ServerToClientMessage.Builder message) {
		sendMessage(getWriter(), message);
	}


	private void sendMessage(MessageProducer<ServerToClientMessage> socket, ServerToClientMessage.Builder message) {
		// Always include the playerId in the message
		message.setLocalPlayerId(playerId);
		// If the game is over, always include whether the current player has won
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
		socket.write(message.build());
	}

	@Override
	public void sendNotification(Notification event, GameState gameState) {
		if (!event.isClientInterested()) {
			return;
		}

		// Quickly send touch notifications
		if (event instanceof TouchingNotification) {
			var touchingNotification = (TouchingNotification) event;
			// Only send touch notifications to the opponent
			if (touchingNotification.getPlayerId() == playerId) {
				return;
			}

			// Build a touch event
			var id = touchingNotification.getEntityReference().getId();
			var event1 = GameEvent.newBuilder()
					.setEventType(touchingNotification.isTouched() ? GameEventType.ENTITY_TOUCHED : GameEventType.ENTITY_UNTOUCHED);
			// Set the appropriate entity ID field.
			if (touchingNotification.isTouched()) {
				event1.setEntityTouched(id);
			} else {
				event1.setEntityUntouched(id);
			}
			var message = ServerToClientMessage.newBuilder()
					.setMessageType(MessageType.TOUCH)
					// Pack touch data into a game event object
					.setEvent(event1.build());


			// Immediately send the message
			sendMessage(message);
			return;
		}

		var workingContext = GameContext.fromState(gameState);
		var message = ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.ON_GAME_EVENT)
				.setChanges(getChangeSet(gameState))
				.setGameState(getClientGameState(playerId, gameState));
		var messageEvent = GameEvent.newBuilder();
		if (event instanceof TriggerFired) {
			var triggerEvent = (TriggerFired) event;
			var triggerFired = GameEvent.TriggerFiredMessage.newBuilder()
					.setTriggerSourceId(triggerEvent.getEnchantment().getHostReference().getId());
			messageEvent
					.setEventType(GameEventType.TRIGGER_FIRED)
					.setTriggerFired(triggerFired);
			var source = triggerEvent.getSource();
			var hasSource = source != null && source.getSourceCard() != null;
			// Send the source entity if there is one. Always send it if the source is owned by the receiving player or if
			// the source is in play.
			if (hasSource && (source.isInPlay() || source.getOwner() == playerId)) {
				messageEvent
						.setSource(ModelConversions.getEntity(workingContext, source, playerId));
				triggerFired.setTriggerSourceId(source.getId());
			}
		} else if (event instanceof GameAction) {
			var action = (GameAction) event;
			var sourceEntity = event.getSource(workingContext);
			Entity.Builder source;

			if (sourceEntity != null && sourceEntity.getEntityType() == EntityType.CARD) {
				var card = (Card) sourceEntity;
				if (card.getCardType() == CardType.SPELL
						&& card.isSecret()
						&& card.getOwner() != playerId) {
					source = ModelConversions.getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
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
			var target = targets.size() > 0 ? targets.get(0) : null;
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
		} else if (event instanceof DestroyWillQueue) {
			var destroyWillQueue = (DestroyWillQueue) event;
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
		} else if (event instanceof net.demilich.metastone.game.events.GameEvent) {
			messageEvent = ModelConversions.getClientEvent((net.demilich.metastone.game.events.GameEvent) event, playerId);
		} else {
			throw new RuntimeException("Unsupported notification.");
		}

		// Set the description on this event.
		messageEvent
				.setIsPowerHistory(event.isPowerHistory());

		if (event.isPowerHistory()) {
			messageEvent
					.setId(eventCounter.getAndIncrement())
					.setDescription((event.getDescription(workingContext, playerId)));

			if (powerHistory.size() > MAX_POWER_HISTORY_SIZE) {
				powerHistory.pop();
			}
			var build = messageEvent.build();
			powerHistory.add(build);
		}

		message.setEvent(messageEvent);
		messageBuffer.offer(message);
	}

	@Override
	public void sendGameOver(GameState state, Player winner) {
		flush();
		if (state == null || lastStateSent == null) {
			this.gameOver = GameOver.newBuilder()
					.setLocalPlayerWon(false).build();
			sendMessage(ServerToClientMessage.newBuilder()
					.setMessageType(MessageType.ON_GAME_END)
					.setGameOver(gameOver));
			return;
		}

		var gameState = getClientGameState(playerId, state);
		var gameOverBuilder = GameOver.newBuilder();
		if (winner == null) {
			gameOverBuilder.setLocalPlayerWon(false);
		} else {
			gameOverBuilder.setLocalPlayerWon(winner.getId() == playerId)
					.setWinningPlayerId(Int32Value.of(winner.getId()));
		}
		gameOver = gameOverBuilder.build();

		var message = ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.ON_GAME_END)
				.setChanges(getChangeSet(state))
				.setGameState(gameState)
				.setGameOver(gameOver);
		sendMessage(message);
	}

	@Override
	public void onConnectionStarted(Player activePlayer) {
		needsPowerHistory = true;
	}

	@Override
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {
	}

	@Override
	public void onUpdate(GameState state) {
		var gameState = getClientGameState(playerId, state);
		if (needsPowerHistory) {
			gameState.addAllPowerHistory(powerHistory);
			gameState.setHasPowerHistory(true);
			needsPowerHistory = false;
		}
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.ON_UPDATE)
				.setChanges(getChangeSet(state))
				.setGameState(gameState));
	}

	public static Spellsource.GameState.Builder getClientGameState(int playerId, GameState state) {
		var simulatedContext = new GameContext();
		simulatedContext.setGameState(state);

		// Compute the local player
		Player local;
		Player opponent;
		if (playerId == PLAYER_1) {
			local = state.getPlayer1();
			opponent = state.getPlayer2();
		} else if (playerId == PLAYER_2) {
			local = state.getPlayer2();
			opponent = state.getPlayer1();
		} else {
			// TODO: How should we define spectators?
			throw new IllegalStateException("playerId");
		}
		simulatedContext.setIgnoreEvents(true);
		return ModelConversions.getGameState(simulatedContext, local, opponent);
	}

	@Override
	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		flush();
		// Set the ids on the available actions
		for (var i = 0; i < availableActions.size(); i++) {
			availableActions.get(i).setId(i);
		}

		sendMessage(ServerToClientMessage.newBuilder()
				.setId(id)
				.setMessageType(MessageType.ON_REQUEST_ACTION)
				.setChanges(getChangeSet(state))
				.setGameState(getClientGameState(playerId, state))
				.setActions(ModelConversions.getClientActions(GameContext.fromState(state), availableActions, playerId)));
	}

	/**
	 * Sends a mulligan request to a Unity client.
	 * <p>
	 * The {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#SET_ASIDE_ZONE} will contain the cards
	 * that can be mulliganned.
	 *
	 * @param id       Mulligan ID
	 * @param state    The game state
	 * @param cards    The cards that can be discarded
	 * @param playerId The player doing the discards
	 */
	@Override
	public void onMulligan(String id, GameState state, List<Card> cards, int playerId) {
		flush();
		var simulatedContext = new GameContext();
		simulatedContext.setGameState(state);
		sendMessage(ServerToClientMessage.newBuilder()
				.setId(id)
				.setMessageType(MessageType.ON_MULLIGAN)
				.setChanges(getChangeSet(state))
				.setGameState(getClientGameState(playerId, state))
				.addAllStartingCards(cards.stream().map(c -> ModelConversions.getEntity(simulatedContext, c, playerId).build()).collect(Collectors.toList())));
	}

	@Override
	public void sendEmote(int entityId, String emote) {
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.EMOTE)
				.setEmote(Emote.newBuilder()
						.setEntityId(entityId)
						// TODO: bring back / fix emotes
						.setMessage(Emote.EmoteMessage.AMAZING)));
	}

	public MessageProducer<ServerToClientMessage> getWriter() {
		return writer;
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
		inboundMessagesClosed = true;
	}

	private void flush() {
		while (!messageBuffer.isEmpty()) {
			sendMessage(messageBuffer.poll());
		}
	}

	private EntityChangeSet getChangeSet(GameState current) {
		var changes = ModelConversions.computeChangeSet(current);
		lastStateSent = current;
		return changes;
	}

	@Override
	public boolean isHuman() {
		return true;
	}

	@Override
	public void close(Promise<Void> completionHandler) {
		readingThread.interrupt();
		closeInboundMessages();
		scheduler.cancelTimer(turnTimer);
		for (var activityMonitor : getActivityMonitors()) {
			activityMonitor.cancel();
		}
		getActivityMonitors().clear();
		requests.clear();
		messageBuffer.clear();
		completionHandler.handle(Future.succeededFuture());
	}

	public Deque<GameplayRequest> getRequests() {
		return requests;
	}

	public List<ActivityMonitor> getActivityMonitors() {
		return activityMonitors;
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
