package com.hiddenswitch.proto3.net.impl.server;

import com.google.common.collect.MapDifference;
import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.Entity;
import com.hiddenswitch.proto3.net.client.models.GameEvent;
import com.hiddenswitch.proto3.net.common.Client;
import com.hiddenswitch.proto3.net.common.GameState;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.SecretCard;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.visuals.TriggerFired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WebSocketClient implements Client {
	private final String userId;
	private final int playerId;
	private final Queue<ServerToClientMessage> messageBuffer;
	private ServerWebSocket privateSocket;
	private GameState lastStateSent;
	private boolean open = true;
	private AtomicInteger eventCounter = new AtomicInteger();
	private Deque<GameEvent> powerHistory = new ArrayDeque<>();

	public WebSocketClient(ServerWebSocket socket, String userId, int playerId) {
		// Be notified when the socket is closed
		socket.endHandler(this::onSocketClosed);
		this.playerId = playerId;
		this.setPrivateSocket(socket);
		this.userId = userId;
		this.messageBuffer = new ConcurrentLinkedQueue<>();
	}

	private void onSocketClosed(Void ignored) {
		open = false;
	}

	private void sendMessage(ServerToClientMessage message) {
		try {
			sendMessage(getPrivateSocket(), message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(ServerWebSocket socket, ServerToClientMessage message) throws IOException {
		// Always include the playerId in the message
		message.setLocalPlayerId(playerId);
		// Don't send the message if the socket is closed
		if (!open) {
			return;
		}
		socket.write(Buffer.buffer(Configuration.getDefaultApiClient().getJSON().serialize(message)));
	}

	public void close() {
		try {
			if (!open) {
				return;
			}
			privateSocket.close();
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onNotification(Notification event, GameState gameState) {
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
					.messageType(MessageType.TOUCH)
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

		final GameState state = gameState;
		GameContext workingContext = new GameContext(state);
		ServerToClientMessage message = new ServerToClientMessage()
				.messageType(MessageType.ON_GAME_EVENT)
				.changes(getChangeSet(state))
				.gameState(getClientGameState(state));

		final Class<? extends Notification> eventClass = event.getClass();

		if (net.demilich.metastone.game.events.GameEvent.class.isAssignableFrom(eventClass)) {
			message.event(Games.getClientEvent((net.demilich.metastone.game.events.GameEvent) event, playerId));
		} else if (TriggerFired.class.isAssignableFrom(eventClass)) {
			TriggerFired triggerEvent = (TriggerFired) event;
			message.event(new GameEvent()
					.eventType(GameEvent.EventTypeEnum.TRIGGER_FIRED)
					.triggerFired(new GameEventTriggerFired()
							.triggerSourceId(triggerEvent.getSpellTrigger().getHostReference().getId())));
		} else if (GameAction.class.isAssignableFrom(eventClass)) {
			final net.demilich.metastone.game.entities.Entity sourceEntity = event.getSource(workingContext);
			Entity source = Games.getEntity(workingContext, sourceEntity, playerId);

			if (sourceEntity.getEntityType() == EntityType.CARD) {
				Card card = (Card) sourceEntity;
				if (card.getCardType() == CardType.SPELL
						&& card instanceof SecretCard
						&& card.getOwner() != playerId) {
					source = Games.getSecretCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
				}
			}
			
			List<Entity> targets = event.getTargets(workingContext, sourceEntity.getOwner())
					.stream().map(e -> Games.getEntity(workingContext, e, playerId)).collect(Collectors.toList());
			final Entity target = targets.size() > 0 ? targets.get(0) : null;
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
	public void onGameEnd(Player winner) {
		flush();
		GameOver gameOver = new GameOver();
		if (winner == null) {
			gameOver.localPlayerWon(false)
					.winningPlayerId(null);
		} else {
			gameOver.localPlayerWon(winner.getId() == playerId)
					.winningPlayerId(winner.getId());
		}
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_GAME_END)
				.gameOver(gameOver));
	}

	@Override
	public void setPlayers(Player localPlayer, Player remotePlayer) {
		// Skip this for websocket clients. It's deprecated.
	}

	@Override
	public void onActivePlayer(Player activePlayer) {
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_ACTIVE_PLAYER));
	}

	@Override
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {
		// TODO: Do nothing?
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_TURN_END));
	}

	@Override
	public void onUpdate(GameState state) {
		final com.hiddenswitch.proto3.net.client.models.GameState gameState = getClientGameState(state);
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_UPDATE)
				.changes(getChangeSet(state))
				.gameState(gameState));
	}

	private com.hiddenswitch.proto3.net.client.models.GameState getClientGameState(GameState state) {
		GameContext simulatedContext = new GameContext(state.player1, state.player2, new GameLogic(), new DeckFormat());
		simulatedContext.setGameState(state);

		// Compute the local player
		Player local;
		Player opponent;
		if (state.player1.getUserId().equals(userId)) {
			local = state.player1;
			opponent = state.player2;
		} else {
			local = state.player2;
			opponent = state.player1;
		}
		simulatedContext.setIgnoreEvents(true);
		return Games.getGameState(simulatedContext, local, opponent)
				.powerHistory(powerHistory.stream().collect(Collectors.toList()));
	}

	@Override
	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		flush();
		// Set the ids on the available actions
		for (int i = 0; i < availableActions.size(); i++) {
			availableActions.get(i).setId(i);
		}

		sendMessage(new ServerToClientMessage()
				.id(id)
				.messageType(MessageType.ON_REQUEST_ACTION)
				.changes(getChangeSet(state))
				.gameState(getClientGameState(state))
				.actions(Games.getClientActions(new GameContext(state), availableActions, playerId)));
	}

	@Override
	public void onMulligan(String id, GameState state, List<Card> cards, int playerId) {
		flush();
		final GameContext simulatedContext = new GameContext();
		simulatedContext.setGameState(state);
		sendMessage(new ServerToClientMessage()
				.id(id)
				.messageType(MessageType.ON_MULLIGAN)
				.startingCards(cards.stream().map(c -> Games.getEntity(simulatedContext, c, playerId)).collect(Collectors.toList())));
	}

	@Override
	public void onEmote(int entityId, Emote.MessageEnum emote) {
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.EMOTE)
				.emote(new Emote()
						.entityId(entityId)
						.message(emote)));
	}

	public ServerWebSocket getPrivateSocket() {
		return privateSocket;
	}

	@Override
	public void lastEvent() {
		flush();
	}

	private void flush() {
		while (!messageBuffer.isEmpty()) {
			sendMessage(messageBuffer.poll());
		}
	}

	private void setPrivateSocket(ServerWebSocket privateSocket) {
		this.privateSocket = privateSocket;
	}

	private EntityChangeSet getChangeSet(GameState current) {
		final MapDifference<Integer, EntityLocation> difference;
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
}
