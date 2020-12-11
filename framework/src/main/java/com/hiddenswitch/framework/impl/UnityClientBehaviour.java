package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.StrandLocalRandom;
import co.paralleluniverse.strands.SuspendableAction1;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.protobuf.Int32Value;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.rpc.*;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.Sync;
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

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hiddenswitch.framework.impl.Games.toProto;
import static io.vertx.ext.sync.Sync.awaitEvent;
import static java.util.stream.Collectors.toList;
import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * Represents a behaviour that converts requests from {@link ActionListener} and game event updates from {@link
 * EventListener} into messages on a {@link ReadStream} and {@link WriteStream}, decoding the read buffers as {@link
 * ClientToServerMessage} and encoding the sent buffers with {@link ServerToClientMessage}.
 */
public class UnityClientBehaviour extends UtilityBehaviour implements Client, Closeable, HasElapsableTurns {
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

	private GameState lastStateSent;
	private boolean inboundMessagesClosed;
	private boolean elapsed;
	private GameOver gameOver;
	private boolean needsPowerHistory;


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
		if (noActivityTimeout > 0L) {
			var activityMonitor = new ActivityMonitor(scheduler, noActivityTimeout, this::noActivity, null);
			activityMonitor.activity();
			getActivityMonitors().add(activityMonitor);
		} else if (noActivityTimeout < 0L) {
			throw new IllegalArgumentException("noActivityTimeout must be positive");
		}

		reader.handler(Sync.fiber(this::handleWebSocketMessage));
	}

	private void secondIntervalElapsed(Long timer) {
		var millisRemaining = server.getMillisRemaining();
		if (millisRemaining == null) {
			return;
		}

		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.MESSAGE_TYPE_TIMER)
				.setTimers(Timers.newBuilder()
						.setMillisRemaining(millisRemaining)));
	}

	@Suspendable
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
	@Suspendable
	public void elapseAwaitingRequests() {
		requestsLock.lock();
		elapsed = true;
		try {
			// Prevent concurrent modification by locking access to this iterator
			GameplayRequest request;
			while ((request = requests.poll()) != null) {
				if (request.getType() == GameplayRequestType.ACTION) {
					@SuppressWarnings("unchecked")
					var callback = (Handler<GameAction>) request.getCallback();
					processActionForElapsedTurn(request.getActions(), callback::handle);
				} else if (request.getType() == GameplayRequestType.MULLIGAN) {
					@SuppressWarnings("unchecked")
					var handler = (Handler<List<Card>>) request.getCallback();
					handler.handle(new ArrayList<>());
				}
			}
		} finally {
			requestsLock.unlock();
		}
	}

	@Suspendable
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

	@Suspendable
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
	@Suspendable
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
				case MESSAGE_TYPE_PINGPONG:
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					// Server is responsible for replying
					sendMessage(ServerToClientMessage.newBuilder().setMessageType(MessageType.MESSAGE_TYPE_PINGPONG));
					break;
				case MESSAGE_TYPE_FIRST_MESSAGE:
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
				case MESSAGE_TYPE_UPDATE_ACTION:
					// Indicates the player has made a choice about which action to take.
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					final var messageId = message.getRepliesTo();
					onActionReceived(messageId, message.getActionIndex());
					break;
				case MESSAGE_TYPE_UPDATE_MULLIGAN:
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					onMulliganReceived(message.getDiscardedCardIndicesList());
					break;
				case MESSAGE_TYPE_EMOTE:
					server.onEmote(this, message.getEmote().getEntityId(), message.getEmote().getMessage().toString());
					break;
				case MESSAGE_TYPE_TOUCH:
					for (var activityMonitor : getActivityMonitors()) {
						activityMonitor.activity();
					}
					if (message.hasEntityTouch()) {
						server.onTouch(this, message.getEntityTouch().getValue());
					} else if (message.hasEntityUntouch()) {
						server.onUntouch(this, message.getEntityUntouch().getValue());
					}
					break;
				case MESSAGE_TYPE_CONCEDE:
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

	@Suspendable
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
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return awaitEvent(fut -> mulliganAsync(context, player, cards, fut));
	}


	@Override
	@Suspendable
	public void mulliganAsync(GameContext context, Player player, List<Card> cards, Handler<List<Card>> next) {
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
	 * The starting hand is also sent in the {@link net.demilich.metastone.game.targeting.Zones#SET_ASIDE_ZONE}, where the
	 * index in the set aside zone corresponds to the index that should be sent to discard.
	 *
	 * @param discardedCardIndices A list of indices in the list of starter cards that should be discarded.
	 */
	@Suspendable
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
			Handler<List<Card>> callback = request.getCallback();

			callback.handle(discardedCards);
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		return awaitEvent(fut -> requestActionAsync(context, player, validActions, fut));
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
	@Suspendable
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
	@Suspendable
	public void requestActionAsync(GameContext context, Player player, List<GameAction> actions, Handler<GameAction> callback) {
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
				processActionForElapsedTurn(actions, callback::handle);
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
	 * When a player's turn ends prematurely, this method will process a player's turn, choosing {@link
	 * ActionType#ACTION_TYPE_BATTLECRY} and {@link ActionType#ACTION_TYPE_DISCOVER} randomly and performing an {@link
	 * ActionType#ACTION_TYPE_END_TURN} as soon as possible.
	 *
	 * @param actions  The possible {@link GameAction} for this request.
	 * @param callback The callback for this request.
	 */
	@Suspendable
	private void processActionForElapsedTurn(List<GameAction> actions, SuspendableAction1<GameAction> callback) {
		// If the request contains an end turn action, execute it. Otherwise, choose an action
		// at random.
		final var action = actions.stream()
				.filter(ga -> toProto(ga.getActionType(), ActionType.class) == ActionType.ACTION_TYPE_BATTLECRY)
				.findFirst().orElse(getRandom(actions));

		if (action == null) {
			throw new IllegalStateException("No action was returned");
		}

		if (Fiber.isCurrentFiber()) {
			try {
				callback.call(action);
			} catch (SuspendExecution | InterruptedException execution) {
				throw new RuntimeException(execution);
			}
		} else {
			throw new UnsupportedOperationException("should not process elapsed turn outside of fiber");
		}
	}

	private GameAction getRandom(List<GameAction> actions) {
		// Consume a game logic random if possible, otherwise generate a random number
		if (server.getRandom() != null) {
			return actions.get(server.getRandom().nextInt(actions.size()));
		}
		return actions.get(StrandLocalRandom.current().nextInt(actions.size()));
	}


	/**
	 * Handles the chosen game action from a client.
	 *
	 * @param messageId   The ID of the message used to request the action.
	 * @param actionIndex The action chosen.
	 */
	@Suspendable
	public void onActionReceived(String messageId, int actionIndex) {
		// The action may have been removed due to the timer or because the game ended, so it's okay if it doesn't exist.
		requestsLock.lock();
		try {
			var request = getRequest(messageId);
			if (request == null) {
				return;
			}

			getRequests().remove(request);
			var action = request.getActions().get(actionIndex);

			@SuppressWarnings("unchecked")
			Handler<GameAction> callback = request.getCallback();

			if (!Fiber.isCurrentFiber()) {
				Sync.getContextScheduler().newFiber(() -> {
					callback.handle(action);
					return null;
				}).start();
			} else {
				callback.handle(action);
			}
		} finally {
			requestsLock.unlock();
		}
	}

	@Override
	@Suspendable
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
		for (var activityMonitor : getActivityMonitors()) {
			activityMonitor.cancel();
		}
		sendGameOver(context.getGameStateCopy(), context.getWinner());
	}


	@Suspendable
	private void sendMessage(ServerToClientMessage.Builder message) {
		sendMessage(getWriter(), message);
	}

	@Suspendable
	private void sendMessage(MessageProducer<ServerToClientMessage> socket, ServerToClientMessage.Builder message) {
		// Always include the playerId in the message
		message.setLocalPlayerId(playerId);
		// If the game is over, always include whether the current player has won
		switch (message.getMessageType()) {
			case MESSAGE_TYPE_TOUCH:
			case MESSAGE_TYPE_EMOTE:
			case MESSAGE_TYPE_PINGPONG:
			case MESSAGE_TYPE_TIMER:
				break;
			default:
				if (gameOver != null) {
					message.setGameOver(gameOver);
				}
		}
		socket.write(message.build());
	}

	@Override
	@Suspendable
	public void sendNotification(Notification event, GameState gameState) {
		if (!event.isClientInterested()) {
			return;
		}

		// Quickly send touch notifications
		if (TouchingNotification.class.isAssignableFrom(event.getClass())) {
			var touchingNotification = (TouchingNotification) event;
			// Only send touch notifications to the opponent
			if (touchingNotification.getPlayerId() == playerId) {
				return;
			}

			// Build a touch event
			var id = touchingNotification.getEntityReference().getId();
			var event1 = GameEvent.newBuilder()
					.setEventType(touchingNotification.isTouched() ? GameEventType.GAME_EVENT_TYPE_ENTITY_TOUCHED : GameEventType.GAME_EVENT_TYPE_ENTITY_UNTOUCHED);
			// Set the appropriate entity ID field.
			if (touchingNotification.isTouched()) {
				event1.setEntityTouched(id);
			} else {
				event1.setEntityUntouched(id);
			}
			var message = ServerToClientMessage.newBuilder()
					.setMessageType(MessageType.MESSAGE_TYPE_TOUCH)
					// Pack touch data into a game event object
					.setEvent(event1.build());


			// Immediately send the message
			sendMessage(message);
			return;
		}

		var workingContext = GameContext.fromState(gameState);
		var message = ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.MESSAGE_TYPE_ON_GAME_EVENT)
				.setChanges(getChangeSet(gameState))
				.setGameState(getClientGameState(playerId, gameState));
		var messageEvent = GameEvent.newBuilder();
		if (event instanceof net.demilich.metastone.game.events.GameEvent) {
			messageEvent = Games.getClientEvent((net.demilich.metastone.game.events.GameEvent) event, playerId);
		} else if (event instanceof TriggerFired) {
			var triggerEvent = (TriggerFired) event;
			var triggerFired = GameEvent.TriggerFiredMessage.newBuilder()
					.setTriggerSourceId(triggerEvent.getEnchantment().getHostReference().getId());
			messageEvent
					.setEventType(GameEventType.GAME_EVENT_TYPE_TRIGGER_FIRED)
					.setTriggerFired(triggerFired);
			var source = triggerEvent.getSource();
			var hasSource = source != null && source.getSourceCard() != null;
			// Send the source entity if there is one. Always send it if the source is owned by the receiving player or if
			// the source is in play.
			if (hasSource && (source.isInPlay() || source.getOwner() == playerId)) {
				messageEvent
						.setSource(Games.getEntity(workingContext, source, playerId));
				triggerFired.setTriggerSourceId(source.getId());
			}
		} else if (event instanceof GameAction) {
			var action = (GameAction) event;
			final var sourceEntity = event.getSource(workingContext);
			var source = Games.getEntity(workingContext, sourceEntity, playerId);

			if (sourceEntity.getEntityType() == com.hiddenswitch.spellsource.client.models.EntityType.CARD) {
				var card = (Card) sourceEntity;
				if (card.getCardType() == com.hiddenswitch.spellsource.client.models.CardType.SPELL
						&& card.isSecret()
						&& card.getOwner() != playerId) {
					source = Games.getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
				}
			}

			var targets = event.getTargets(workingContext, sourceEntity.getOwner())
					.stream().map(e -> Games.getEntity(workingContext, e, playerId)).collect(Collectors.toList());
			var target = targets.size() > 0 ? targets.get(0) : null;
			messageEvent
					.setDescription(event.getDescription(workingContext, playerId))
					.setIsSourcePlayerLocal(source == null ? workingContext.getActivePlayerId() == playerId : source.getOwner() == playerId)
					.setIsTargetPlayerLocal(target != null && target.getOwner() == playerId)
					.setEventType(GameEventType.GAME_EVENT_TYPE_PERFORMED_GAME_ACTION)
					.setPerformedGameAction(GameEvent.PerformedGameActionMessage.newBuilder()
							.setActionType(toProto(action.getActionType(), ActionType.class)).build());
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
					.setEventType(GameEventType.GAME_EVENT_TYPE_DESTROY_WILL_QUEUE)
					.setDestroy(GameEvent.DestroyMessage.newBuilder()
							.addAllObjects(destroyWillQueue.getDestroys()
									.stream()
									.map(destroy -> Destroy.newBuilder()
											.setSource(Games.getEntity(workingContext, destroy.getSource(), playerId))
											.setTarget(Games.getEntity(workingContext, destroy.getTarget(), playerId))
											.addAllAftermaths(destroy.getAftermaths().stream().map(aftermath -> Games.getEntity(workingContext, aftermath, playerId).build()).collect(toList())).build())
									.collect(toList())));
		} else {
			throw new RuntimeException("Unsupported notification.");
		}

		// Set the description on this event.
		messageEvent
				.setIsPowerHistory(event.isPowerHistory());

		message.setEvent(messageEvent);
		if (event.isPowerHistory()) {
			messageEvent.setId(eventCounter.getAndIncrement())
					.setDescription((event.getDescription(workingContext, playerId)));

			if (powerHistory.size() > MAX_POWER_HISTORY_SIZE) {
				powerHistory.pop();
			}
			var build = messageEvent.build();
			powerHistory.add(build);
		}

		messageBuffer.offer(message);
	}

	@Override
	@Suspendable
	public void sendGameOver(GameState state, Player winner) {
		flush();
		if (state == null || lastStateSent == null) {
			this.gameOver = GameOver.newBuilder()
					.setLocalPlayerWon(false).build();
			sendMessage(ServerToClientMessage.newBuilder()
					.setMessageType(MessageType.MESSAGE_TYPE_ON_GAME_END)
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
				.setMessageType(MessageType.MESSAGE_TYPE_ON_GAME_END)
				.setChanges(getChangeSet(state))
				.setGameState(gameState)
				.setGameOver(gameOver);
		sendMessage(message);
	}

	@Override
	@Suspendable
	public void onConnectionStarted(Player activePlayer) {
		needsPowerHistory = true;
	}

	@Override
	@Suspendable
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {
	}

	@Override
	@Suspendable
	public void onUpdate(GameState state) {
		var gameState = getClientGameState(playerId, state);
		if (needsPowerHistory) {
			gameState.addAllPowerHistory(powerHistory);
			needsPowerHistory = false;
		}
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.MESSAGE_TYPE_ON_UPDATE)
				.setChanges(getChangeSet(state))
				.setGameState(gameState));
	}

	public static com.hiddenswitch.spellsource.rpc.GameState.Builder getClientGameState(int playerId, GameState state) {
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
		return Games.getGameState(simulatedContext, local, opponent);
	}

	@Override
	@Suspendable
	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		flush();
		// Set the ids on the available actions
		for (var i = 0; i < availableActions.size(); i++) {
			availableActions.get(i).setId(i);
		}

		sendMessage(ServerToClientMessage.newBuilder()
				.setId(id)
				.setMessageType(MessageType.MESSAGE_TYPE_ON_REQUEST_ACTION)
				.setChanges(getChangeSet(state))
				.setGameState(getClientGameState(playerId, state))
				.setActions(Games.getClientActions(GameContext.fromState(state), availableActions, playerId)));
	}

	/**
	 * Sends a mulligan request to a Unity client.
	 * <p>
	 * The {@link net.demilich.metastone.game.targeting.Zones#SET_ASIDE_ZONE} will contain the cards that can be
	 * mulliganned.
	 *
	 * @param id       Mulligan ID
	 * @param state    The game state
	 * @param cards    The cards that can be discarded
	 * @param playerId The player doing the discards
	 */
	@Override
	@Suspendable
	public void onMulligan(String id, GameState state, List<Card> cards, int playerId) {
		flush();
		var simulatedContext = new GameContext();
		simulatedContext.setGameState(state);
		sendMessage(ServerToClientMessage.newBuilder()
				.setId(id)
				.setMessageType(MessageType.MESSAGE_TYPE_ON_MULLIGAN)
				.setChanges(getChangeSet(state))
				.setGameState(getClientGameState(playerId, state))
				.addAllStartingCards(cards.stream().map(c -> Games.getEntity(simulatedContext, c, playerId).build()).collect(Collectors.toList())));
	}

	@Override
	@Suspendable
	public void sendEmote(int entityId, String emote) {
		sendMessage(ServerToClientMessage.newBuilder()
				.setMessageType(MessageType.MESSAGE_TYPE_EMOTE)
				.setEmote(Emote.newBuilder()
						.setEntityId(entityId)
						// TODO: bring back / fix emotes
						.setMessage(Emote.EmoteMessage.EMOTE_MESSAGE_AMAZING)));
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
		var changes = Games.computeChangeSet(current);
		lastStateSent = current;
		return changes;
	}

	@Override
	public boolean isHuman() {
		return true;
	}

	@Override
	@Suspendable
	public void close(Promise<Void> completionHandler) {
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
