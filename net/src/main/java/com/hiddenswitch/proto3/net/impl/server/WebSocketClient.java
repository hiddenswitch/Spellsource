package com.hiddenswitch.proto3.net.impl.server;

import com.google.common.collect.MapDifference;
import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.models.*;
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
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class WebSocketClient implements Client {
	private final String userId;
	private final int playerId;
	private final Queue<ServerToClientMessage> messageEventBuffer;
	private ServerWebSocket privateSocket;
	private GameState lastStateSent;
	private boolean open = true;

	public WebSocketClient(ServerWebSocket socket, String userId, int playerId) {
		// Be notified when the socket is closed
		socket.endHandler(this::onSocketClosed);
		this.playerId = playerId;
		this.setPrivateSocket(socket);
		this.userId = userId;
		this.messageEventBuffer = new ConcurrentLinkedQueue<>();
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
	public void onGameEvent(net.demilich.metastone.game.events.GameEvent event) {
		final GameEvent clientEvent = Games.getClientEvent(event, playerId);
		final GameState state = event.getGameContext().getGameStateCopy();
		final ServerToClientMessage message = new ServerToClientMessage()
				.messageType(MessageType.ON_GAME_EVENT)
				.changes(getChangeSet(state))
				.gameState(getClientGameState(state))
				.event(clientEvent);
		messageEventBuffer.offer(message);
	}

	@Override
	public void onGameEnd(Player winner) {
		flushEvents();
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
		return Games.getGameState(simulatedContext, local, opponent);
	}

	@Override
	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		flushEvents();
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
		flushEvents();
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
		flushEvents();
	}

	private void flushEvents() {
		while (!messageEventBuffer.isEmpty()) {
			sendMessage(messageEventBuffer.poll());
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
