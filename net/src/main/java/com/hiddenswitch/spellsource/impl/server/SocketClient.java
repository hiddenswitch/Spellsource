package com.hiddenswitch.spellsource.impl.server;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.Client;
import com.hiddenswitch.spellsource.common.ServerToClientMessage;
import com.hiddenswitch.spellsource.util.IncomingMessage;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.VertxBufferOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.Notification;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketClient implements Client {
	private NetSocket privateSocket;
	private ConcurrentLinkedQueue<Buffer> messageBuffer = new ConcurrentLinkedQueue<>();

	public SocketClient(NetSocket socket) {
		this.setPrivateSocket(socket);
	}

	private void sendMessage(ServerToClientMessage message) {
		try {
			sendMessage(getPrivateSocket(), message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void sendMessage(NetSocket socket, ServerToClientMessage message) throws IOException {
		final Buffer buffer = getBuffer(message);
		socket.write(buffer);
	}

	private Buffer getBuffer(ServerToClientMessage message) {
		// Serialize message
		VertxBufferOutputStream out = new VertxBufferOutputStream();
		// Write the magic header
		out.getBuffer().appendBytes(IncomingMessage.MAGIC_BYTES);
		// Write an integer's worth of space
		out.getBuffer().appendInt(0);
		// Serialize the message
		int before = out.size();

		try {
			Serialization.serialize(message, out);
		} catch (Exception ignored) {
		}

		int messageSize = out.size() - before;
		// Set the second set of four bytes to the message length.
		return out.getBuffer().setInt(4, messageSize);
	}

	public void close() {
		flushEvents();
		privateSocket.close();
	}

	@Override
	public void onNotification(Notification event, GameState gameState) {
		if (event instanceof GameEvent) {
			messageBuffer.offer(getBuffer(new ServerToClientMessage((GameEvent) event)));
		}
	}

	@Override
	public void onGameEnd(Player winner) {
		flushEvents();
		sendMessage(new ServerToClientMessage(winner, true));
	}

	@Override
	public void setPlayers(Player localPlayer, Player remotePlayer) {
		flushEvents();
		sendMessage(new ServerToClientMessage(localPlayer, remotePlayer));
	}

	@Override
	public void onActivePlayer(Player activePlayer) {
		flushEvents();
		sendMessage(new ServerToClientMessage(activePlayer, false));
	}

	@Override
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {
		flushEvents();
		sendMessage(new ServerToClientMessage(activePlayer, turnNumber, turnState));
	}

	@Override
	public void onUpdate(GameState state) {
		sendMessage(new ServerToClientMessage(state));
	}

	@Override
	public void onRequestAction(String id, GameState state, List<GameAction> availableActions) {
		flushEvents();
		sendMessage(new ServerToClientMessage(id, state, availableActions));
	}

	@Override
	public void onMulligan(String id, GameState state, List<Card> cards, int playerId) {
		flushEvents();
		sendMessage(new ServerToClientMessage(id, state.player1.getId() == playerId ? state.player1 : state.player2, cards, state));
	}

	public NetSocket getPrivateSocket() {
		return privateSocket;
	}

	@Override
	public void lastEvent() {
		// flush events
		flushEvents();
	}

	private void setPrivateSocket(NetSocket privateSocket) {
		this.privateSocket = privateSocket;
	}

	private void flushEvents() {
		while (!messageBuffer.isEmpty()) {
			privateSocket.write(messageBuffer.poll());
		}
	}
}
