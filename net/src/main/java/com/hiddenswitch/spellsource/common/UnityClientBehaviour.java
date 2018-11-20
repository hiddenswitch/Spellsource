package com.hiddenswitch.spellsource.common;

import com.github.fromage.quasi.fibers.Fiber;
import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.SuspendableAction1;
import com.google.common.collect.MapDifference;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.MessageType;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import com.hiddenswitch.spellsource.impl.util.Scheduler;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitEvent;
import static java.util.stream.Collectors.toList;
import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * Represents a behaviour that converts requests from {@link ActionListener} and game event updates from {@link
 * EventListener} into messages on a {@link ReadStream} and {@link WriteStream}, decoding the read
 * buffers as {@link ClientToServerMessage} and encoding the sent buffers with {@link ServerToClientMessage}.
 */
public class UnityClientBehaviour extends UtilityBehaviour implements Client, Closeable {
	private static Logger logger = LoggerFactory.getLogger(UnityClientBehaviour.class);

	private final Queue<ServerToClientMessage> messageBuffer = new ConcurrentLinkedQueue<>();
	private final AtomicInteger eventCounter = new AtomicInteger();
	private final AtomicInteger callbackIdCounter = new AtomicInteger();
	private final List<GameplayRequest> requests = new ArrayList<>();
	private final List<ActivityMonitor> activityMonitors = new ArrayList<>();
	private final UserId userId;
	private final int playerId;
	private final Scheduler scheduler;
	private ReadStream<Buffer> reader;
	private WriteStream<Buffer> writer;
	private Server server;

	private com.hiddenswitch.spellsource.common.GameState lastStateSent;
	private Deque<GameEvent> powerHistory = new ArrayDeque<>();
	private boolean inboundMessagesClosed;


	public UnityClientBehaviour(Server server,
	                            Scheduler scheduler,
	                            ReadStream<Buffer> reader,
	                            WriteStream<Buffer> writer,
	                            UserId userId,
	                            int playerId,
	                            long noActivityTimeout) {
		this.scheduler = scheduler;
		this.reader = reader;
		this.writer = writer;
		this.userId = userId;
		this.playerId = playerId;
		this.server = server;

		ActivityMonitor activityMonitor = new ActivityMonitor(scheduler, noActivityTimeout, this::noActivity, null);
		activityMonitor.activity();
		activityMonitors.add(activityMonitor);

		reader.handler(suspendableHandler(this::handleWebSocketMessage));
	}

	@Suspendable
	private void noActivity(ActivityMonitor activityMonitor) {
		elapseMulligan();
		elapseTurn();
		server.onConcede(this);
	}

	/**
	 * Elapses this client's turn, typically due to a timeout.
	 */
	@Override
	@Suspendable
	public void elapseTurn() {
		Iterator<GameplayRequest> requestsIter = this.getRequests().iterator();
		while (requestsIter.hasNext()) {
			GameplayRequest request = requestsIter.next();
			if (request.getType() == GameplayRequestType.ACTION) {
				requestsIter.remove();

				@SuppressWarnings("unchecked")
				Handler<GameAction> callback = (Handler<GameAction>) request.getCallback();

				processActionForElapsedTurn(request.getActions(), callback::handle);
			}
		}
	}

	/**
	 * Elapses this client's mulligan, typically due to a timeout.
	 */
	@Override
	@Suspendable
	public void elapseMulligan() {
		Iterator<GameplayRequest> requestsIter = this.getRequests().iterator();
		while (requestsIter.hasNext()) {
			GameplayRequest request = requestsIter.next();
			if (request.getType() == GameplayRequestType.MULLIGAN) {
				requestsIter.remove();

				@SuppressWarnings("unchecked")
				Handler<List<Card>> handler = (Handler<List<Card>>) request.getCallback();
				handler.handle(new ArrayList<>());
			}
		}
	}

	private GameplayRequest getRequest(String messageId) {
		for (GameplayRequest request : getRequests()) {
			if (request.getCallbackId().equals(messageId)) {
				return request;
			}
		}

		return null;
	}

	/**
	 * Handles a web socket message (message from the {@link #reader}), decoding it into a {@link ClientToServerMessage}.
	 *
	 * @param messageBuffer The buffer containing the JSON of the message.
	 * @throws SuspendExecution
	 */
	@Suspendable
	protected void handleWebSocketMessage(Buffer messageBuffer) throws SuspendExecution {
		if (inboundMessagesClosed) {
			return;
		}

		ClientToServerMessage message = Json.decodeValue(messageBuffer, ClientToServerMessage.class);

		switch (message.getMessageType()) {
			case PINGPONG:
				for (ActivityMonitor activityMonitor : activityMonitors) {
					activityMonitor.activity();
				}
				break;
			case FIRST_MESSAGE:
				lastStateSent = null;
				// The first message indicates the player has connected or reconnected.
				for (ActivityMonitor activityMonitor : activityMonitors) {
					activityMonitor.activity();
				}

				if (server.isGameReady()) {
					// Replace the client
					server.onPlayerReconnected(this);
					// Since the player may have pending requests, we're going to send the data the client needs again.
					retryRequests();
				} else {
					server.onPlayerReady(this);
				}
				break;
			case UPDATE_ACTION:
				// Indicates the player has made a chocie about which action to take.
				if (server == null) {
					throw new RuntimeException();
				}
				for (ActivityMonitor activityMonitor : activityMonitors) {
					activityMonitor.activity();
				}
				final String messageId = message.getRepliesTo();
				onActionReceived(messageId, message.getActionIndex());
				break;
			case UPDATE_MULLIGAN:
				if (server == null) {
					throw new RuntimeException();
				}
				for (ActivityMonitor activityMonitor : activityMonitors) {
					activityMonitor.activity();
				}
				final String messageId2 = message.getRepliesTo();
				onMulliganReceived(messageId2, message.getDiscardedCardIndices());
				break;
			case EMOTE:
				if (server == null) {
					break;
				}
				server.onEmote(this, message.getEmote().getEntityId(), message.getEmote().getMessage());
				break;
			case TOUCH:
				if (server == null) {
					break;
				}
				for (ActivityMonitor activityMonitor : activityMonitors) {
					activityMonitor.activity();
				}
				if (null != message.getEntityTouch()) {
					server.onTouch(this, message.getEntityTouch());
				} else if (null != message.getEntityUntouch()) {
					server.onUntouch(this, message.getEntityUntouch());
				}
				break;
			case CONCEDE:
				if (server == null) {
					break;
				}
				server.onConcede(this);
				break;
		}
	}

	@Suspendable
	protected void retryRequests() {
		for (GameplayRequest request : getRequests()) {
			switch (request.getType()) {
				case ACTION:
					onRequestAction(request.getCallbackId(), lastStateSent, request.getActions());
					break;
				case MULLIGAN:
					onMulligan(request.getCallbackId(), lastStateSent, request.getStarterCards(), playerId);
					break;
				default:
					logger.error("Unknown gameplay request was pending.");
					break;
			}
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
		String id = Integer.toString(callbackIdCounter.getAndIncrement());
		getRequests().add(new GameplayRequest()
				.setCallbackId(id)
				.setType(GameplayRequestType.MULLIGAN)
				.setStarterCards(cards)
				.setCallback(next));
		onMulligan(id, context.getGameStateCopy(), cards, playerId);
	}

	@Suspendable
	public void onMulliganReceived(String messageId, List<Integer> discardedCardIndices) {
		GameplayRequest request = getRequest(messageId);
		if (request == null) {
			// The game may have ended, a mulligan is being received twice, or the game was conceded.
			return;
		}

		getRequests().remove(request);
		List<Card> discardedCards = discardedCardIndices
				.stream()
				.map(i -> request.getStarterCards().get(i))
				.collect(toList());

		@SuppressWarnings("unchecked")
		Handler<List<Card>> callback = request.getCallback();

		callback.handle(discardedCards);
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		return awaitEvent(fut -> requestActionAsync(context, player, validActions, fut));
	}

	@Override
	@Suspendable
	public void requestActionAsync(GameContext context, Player player, List<GameAction> actions, Handler<GameAction> callback) {
		String id = Integer.toString(callbackIdCounter.getAndIncrement());
		GameplayRequest request = new GameplayRequest()
				.setCallbackId(id)
				.setType(GameplayRequestType.ACTION)
				.setActions(actions)
				.setCallback(callback);

		// The player's turn may have ended, so handle the action immediately in this case.
		if (isTimerElapsed()) {
			processActionForElapsedTurn(actions, callback::handle);
		} else {
			// Send a state update for the other player too
			GameState state = context.getGameStateCopy();
			onUpdate(state);

			getRequests().add(request);
			onRequestAction(id, state, actions);
		}
	}

	private boolean isTimerElapsed() {
		// TODO
		return false;
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
	private void processActionForElapsedTurn(List<GameAction> actions, SuspendableAction1<GameAction> callback) {
		// If the request contains an end turn action, execute it. Otherwise, choose an action
		// at random.
		final GameAction action = actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.END_TURN)
				.findFirst().orElse(getRandom(actions));

		if (Fiber.isCurrentFiber()) {
			try {
				callback.call(action);
			} catch (SuspendExecution | InterruptedException execution) {
				throw new RuntimeException(execution);
			}
		} else {
			suspendableHandler(callback).handle(action);
		}
	}

	private GameAction getRandom(List<GameAction> actions) {
		// TODO
		return null;
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
		GameplayRequest request = getRequest(messageId);
		if (request == null) {
			return;
		}

		getRequests().remove(request);
		GameAction action = request.getActions().get(actionIndex);

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
	}

	@Override
	@Suspendable
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
		for (ActivityMonitor activityMonitor : getActivityMonitors()) {
			activityMonitor.cancel();
		}
		sendGameOver(context.getGameStateCopy(), context.getWinner());
	}


	@Suspendable
	private void sendMessage(ServerToClientMessage message) {
		try {
			sendMessage(getWriter(), message);
		} catch (NullPointerException writerNull) {
		} catch (IOException connectionLost) {
			throw new RuntimeException(connectionLost);
		}
	}

	@Suspendable
	private void sendMessage(WriteStream<Buffer> socket, ServerToClientMessage message) throws IOException {
		// Always include the playerId in the message
		message.setLocalPlayerId(playerId);
		socket.write(Buffer.buffer(Json.encode(message)));
	}

	@Override
	@Suspendable
	public void sendNotification(Notification event, com.hiddenswitch.spellsource.common.GameState gameState) {
		if (!event.isClientInterested()) {
			return;
		}

		// Quickly send touch notifications
		if (TouchingNotification.class.isAssignableFrom(event.getClass())) {
			TouchingNotification touchingNotification = (TouchingNotification) event;
			// Only send touch notifications to the opponent
			if (touchingNotification.getPlayerId() == playerId) {
				return;
			}

			// Build a touch event
			final int id = touchingNotification.getEntityReference().getId();
			final ServerToClientMessage message = new ServerToClientMessage()
					.messageType(com.hiddenswitch.spellsource.client.models.MessageType.TOUCH)
					// Pack touch data into a game event object
					.event(new GameEvent()
							.eventType(touchingNotification.isTouched() ? GameEvent.EventTypeEnum.ENTITY_TOUCHED : GameEvent.EventTypeEnum.ENTITY_UNTOUCHED));

			// Set the appropriate entity ID field.
			if (touchingNotification.isTouched()) {
				message.getEvent().entityTouched(id);
			} else {
				message.getEvent().entityUntouched(id);
			}
			// Immediately send the message
			sendMessage(message);
			return;
		}

		final com.hiddenswitch.spellsource.common.GameState state = gameState;
		GameContext workingContext = GameContext.fromState(state);
		ServerToClientMessage message = new ServerToClientMessage()
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_GAME_EVENT)
				.changes(getChangeSet(state))
				.timers(new Timers()
						.millisRemaining(state.millisRemaining))
				.gameState(getClientGameState(state));

		final Class<? extends Notification> eventClass = event.getClass();

		if (net.demilich.metastone.game.events.GameEvent.class.isAssignableFrom(eventClass)) {
			message.event(Games.getClientEvent((net.demilich.metastone.game.events.GameEvent) event, playerId));
		} else if (TriggerFired.class.isAssignableFrom(eventClass)) {
			TriggerFired triggerEvent = (TriggerFired) event;
			message.event(new GameEvent()
					.eventType(GameEvent.EventTypeEnum.TRIGGER_FIRED)
					.triggerFired(new GameEventTriggerFired()
							.triggerSourceId(triggerEvent.getEnchantment().getHostReference().getId())));
		} else if (GameAction.class.isAssignableFrom(eventClass)) {
			final net.demilich.metastone.game.entities.Entity sourceEntity = event.getSource(workingContext);
			com.hiddenswitch.spellsource.client.models.Entity source = Games.getEntity(workingContext, sourceEntity, playerId);

			if (sourceEntity.getEntityType() == EntityType.CARD) {
				Card card = (Card) sourceEntity;
				if (card.getCardType() == CardType.SPELL
						&& card.isSecret()
						&& card.getOwner() != playerId) {
					source = Games.getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
				}
			}

			List<com.hiddenswitch.spellsource.client.models.Entity> targets = event.getTargets(workingContext, sourceEntity.getOwner())
					.stream().map(e -> Games.getEntity(workingContext, e, playerId)).collect(Collectors.toList());
			final com.hiddenswitch.spellsource.client.models.Entity target = targets.size() > 0 ? targets.get(0) : null;
			message.event(new GameEvent()
					.eventType(GameEvent.EventTypeEnum.PERFORMED_GAME_ACTION)
					.performedGameAction(new GameEventPerformedGameAction()
							.source(source)
							.target(target)));
		} else {
			throw new RuntimeException("Unsupported notification.");
		}

		// Set the description on this event.
		final GameEvent toClient = message.getEvent()
				.isPowerHistory(event.isPowerHistory());

		if (event.isPowerHistory()) {
			toClient.id(eventCounter.getAndIncrement())
					.description((event.getDescription(workingContext, playerId)));

			if (powerHistory.size() > 10) {
				powerHistory.pop();
			}
			powerHistory.add(toClient);
		}

		messageBuffer.offer(message);
	}

	@Override
	@Suspendable
	public void sendGameOver(com.hiddenswitch.spellsource.common.GameState state, Player winner) {
		flush();
		if (state == null || lastStateSent == null) {
			sendMessage(new ServerToClientMessage()
					.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_GAME_END)
					.gameOver(new GameOver()
							.localPlayerWon(false)));
			return;
		}

		final com.hiddenswitch.spellsource.client.models.GameState gameState = getClientGameState(state);
		GameOver gameOver = new GameOver();
		if (winner == null) {
			gameOver.localPlayerWon(false)
					.winningPlayerId(null);
		} else {
			gameOver.localPlayerWon(winner.getId() == playerId)
					.winningPlayerId(winner.getId());
		}
		sendMessage(new ServerToClientMessage()
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_GAME_END)
				.changes(getChangeSet(state))
				.gameState(gameState)
				.gameOver(gameOver));
	}

	@Override
	@Suspendable
	public void onActivePlayer(Player activePlayer) {
		sendMessage(new ServerToClientMessage()
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_ACTIVE_PLAYER));
	}

	@Override
	@Suspendable
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {
		// TODO: Do nothing?
		sendMessage(new ServerToClientMessage()
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_TURN_END));
	}

	@Override
	@Suspendable
	public void onUpdate(com.hiddenswitch.spellsource.common.GameState state) {
		final com.hiddenswitch.spellsource.client.models.GameState gameState = getClientGameState(state);
		sendMessage(new ServerToClientMessage()
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_UPDATE)
				.changes(getChangeSet(state))
				.timers(new Timers()
						.millisRemaining(state.millisRemaining))
				.gameState(gameState));
	}

	private com.hiddenswitch.spellsource.client.models.GameState getClientGameState(com.hiddenswitch.spellsource.common.GameState state) {
		GameContext simulatedContext = new GameContext(state.player1, state.player2, new GameLogic(), new DeckFormat());
		simulatedContext.setGameState(state);

		// Compute the local player
		Player local;
		Player opponent;
		if (playerId == PLAYER_1) {
			local = state.player1;
			opponent = state.player2;
		} else if (playerId == PLAYER_2) {
			local = state.player2;
			opponent = state.player1;
		} else {
			// TODO: How should we define spectators?
			throw new IllegalStateException("playerId");
		}
		simulatedContext.setIgnoreEvents(true);
		return Games.getGameState(simulatedContext, local, opponent)
				.powerHistory(new ArrayList<>(powerHistory));
	}

	@Override
	@Suspendable
	public void onRequestAction(String id, com.hiddenswitch.spellsource.common.GameState state, List<GameAction> availableActions) {
		flush();
		// Set the ids on the available actions
		for (int i = 0; i < availableActions.size(); i++) {
			availableActions.get(i).setId(i);
		}

		sendMessage(new ServerToClientMessage()
				.id(id)
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_REQUEST_ACTION)
				.changes(getChangeSet(state))
				.timers(new Timers()
						.millisRemaining(state.millisRemaining))
				.gameState(getClientGameState(state))
				.actions(Games.getClientActions(GameContext.fromState(state), availableActions, playerId)));
	}

	@Override
	@Suspendable
	public void onMulligan(String id, com.hiddenswitch.spellsource.common.GameState state, List<Card> cards, int playerId) {
		flush();
		final GameContext simulatedContext = new GameContext();
		simulatedContext.setGameState(state);
		sendMessage(new ServerToClientMessage()
				.id(id)
				.timers(new Timers()
						.millisRemaining(state.millisRemaining))
				.messageType(com.hiddenswitch.spellsource.client.models.MessageType.ON_MULLIGAN)
				.startingCards(cards.stream().map(c -> Games.getEntity(simulatedContext, c, playerId)).collect(Collectors.toList())));
	}

	@Override
	@Suspendable
	public void sendEmote(int entityId, Emote.MessageEnum emote) {
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.EMOTE)
				.emote(new Emote()
						.entityId(entityId)
						.message(emote)));
	}

	public WriteStream<Buffer> getWriter() {
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
		return userId.toString();
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

	private EntityChangeSet getChangeSet(com.hiddenswitch.spellsource.common.GameState current) {
		final MapDifference<Integer, net.demilich.metastone.game.entities.EntityLocation> difference;
		if (lastStateSent == null) {
			difference = current.start();
		} else {
			difference = lastStateSent.to(current);
		}

		EntityChangeSet changes = new EntityChangeSet();
		difference.entriesDiffering().entrySet().stream().map(i -> new EntityChangeSetInner()
				.id(i.getKey())
				.op(EntityChangeSetInner.OpEnum.C)
				.p1(new EntityState()
						.location(Games.toClientLocation(i.getValue().rightValue())))
				.p0(new EntityState()
						.location(Games.toClientLocation(i.getValue().leftValue()))))
				.forEach(changes::add);

		difference.entriesOnlyOnRight().entrySet().stream().map(i -> new EntityChangeSetInner().id(i.getKey())
				.op(EntityChangeSetInner.OpEnum.A)
				.p1(new EntityState()
						.location(Games.toClientLocation(i.getValue()))))
				.forEach(changes::add);

		difference.entriesOnlyOnLeft().entrySet().stream().map(i -> new EntityChangeSetInner().id(i.getKey())
				.op(EntityChangeSetInner.OpEnum.R)
				.p1(new EntityState()
						.location(Games.toClientLocation(i.getValue()))))
				.forEach(changes::add);

		lastStateSent = current;

		return changes;
	}

	@Override
	public boolean isHuman() {
		return true;
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		try {
			for (ActivityMonitor activityMonitor : activityMonitors) {
				activityMonitor.cancel();
			}

			requests.clear();
			messageBuffer.clear();
			server = null;
			reader = null;
			writer = null;
		} catch (Throwable ignore) {
			logger.error("close {}", getUserId(), ignore);
		} finally {
			completionHandler.handle(Future.succeededFuture());
		}
	}

	public List<GameplayRequest> getRequests() {
		return requests;
	}

	public List<ActivityMonitor> getActivityMonitors() {
		return activityMonitors;
	}
}
