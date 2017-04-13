package com.hiddenswitch.proto3.net.impl.server;

import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.GameEvent;
import com.hiddenswitch.proto3.net.client.models.GameEvent.EventTypeEnum;
import com.hiddenswitch.proto3.net.client.models.PhysicalAttackEvent;
import com.hiddenswitch.proto3.net.common.Client;
import com.hiddenswitch.proto3.net.common.GameState;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WebsocketClient implements Client {
	private final String userId;
	private ServerWebSocket privateSocket;

	public WebsocketClient(ServerWebSocket socket, String userId) {
		this.setPrivateSocket(socket);
		this.userId = userId;
	}

	private void sendMessage(ServerToClientMessage message) {
		try {
			sendMessage(getPrivateSocket(), message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(ServerWebSocket socket, ServerToClientMessage message) throws IOException {
		socket.write(Buffer.buffer(Configuration.getDefaultApiClient().getJSON().serialize(message)));
	}

	public void close() {
		try {
			privateSocket.close();
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onGameEvent(net.demilich.metastone.game.events.GameEvent event) {
		final GameEvent clientEvent = new GameEvent();

		clientEvent.eventType(EventTypeEnum.valueOf(event.getEventType().toString()));

		GameContext workingContext = event.getGameContext().clone();

		// Handle the event types here.
		if (event instanceof net.demilich.metastone.game.events.PhysicalAttackEvent) {
			final net.demilich.metastone.game.events.PhysicalAttackEvent physicalAttackEvent
					= (net.demilich.metastone.game.events.PhysicalAttackEvent) event;
			clientEvent.physicalAttack(new com.hiddenswitch.proto3.net.client.models.PhysicalAttackEvent()
					.attacker(Games.getEntity(workingContext, physicalAttackEvent.getAttacker()))
					.defender(Games.getEntity(workingContext, physicalAttackEvent.getDefender()))
					.damageDealt(physicalAttackEvent.getDamageDealt()));
		} else if (event instanceof DrawCardEvent) {
			final DrawCardEvent drawCardEvent = (DrawCardEvent) event;
			clientEvent.drawCard(new GameEventDrawCard()
					.card(Games.getEntity(workingContext, drawCardEvent.getCard()))
					.drawn(drawCardEvent.isDrawn()));
		} else if (event instanceof KillEvent) {
			final KillEvent killEvent = (KillEvent) event;
			clientEvent.kill(new GameEventKill()
					.victim(Games.getEntity(workingContext, killEvent.getVictim())));
		} else if (event instanceof CardPlayedEvent) {
			final CardPlayedEvent cardPlayedEvent = (CardPlayedEvent) event;
			clientEvent.cardPlayed(new GameEventCardPlayed()
					.card(Games.getEntity(workingContext, cardPlayedEvent.getCard())));
		} else if (event instanceof SummonEvent) {
			final SummonEvent summonEvent = (SummonEvent) event;
			clientEvent.summon(new GameEventBeforeSummon()
					.minion(Games.getEntity(workingContext, summonEvent.getMinion()))
					.source(Games.getEntity(workingContext, summonEvent.getSource())));
		} else if (event instanceof DamageEvent) {
			final DamageEvent damageEvent = (DamageEvent) event;
			clientEvent.damage(new GameEventDamage()
					.damage(damageEvent.getDamage())
					.source(Games.getEntity(workingContext, damageEvent.getSource()))
					.victim(Games.getEntity(workingContext, damageEvent.getVictim())));
		}

		clientEvent.eventSource(Games.getEntity(workingContext, event.getEventSource()));
		clientEvent.eventTarget(Games.getEntity(workingContext, event.getEventTarget()));
		clientEvent.targetPlayerId(event.getTargetPlayerId());
		clientEvent.sourcePlayerId(event.getSourcePlayerId());

		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_GAME_EVENT)
				.event(clientEvent));
	}

	@Override
	public void onGameEnd(Player winner) {
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_GAME_END));
	}

	@Override
	public void setPlayers(Player localPlayer, Player remotePlayer) {
		// TODO: For now, simulate a game state.
		GameContext simulatedContext = new GameContext(localPlayer.clone(), remotePlayer.clone(), new GameLogic(), new DeckFormat());
		simulatedContext.getPlayer1().setBehaviour(new DoNothingBehaviour());
		simulatedContext.getPlayer2().setBehaviour(new DoNothingBehaviour());
		simulatedContext.setIgnoreEvents(true);
		simulatedContext.init();
		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_UPDATE)
				.gameState(Games.getGameState(simulatedContext, localPlayer, remotePlayer)));
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
				.gameState(gameState));
	}

	private com.hiddenswitch.proto3.net.client.models.GameState getClientGameState(GameState state) {
		GameContext simulatedContext = new GameContext(state.player1, state.player2, new GameLogic(), new DeckFormat());
		simulatedContext.loadState(state);

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
		sendMessage(new ServerToClientMessage()
				.id(id)
				.messageType(MessageType.ON_REQUEST_ACTION)
				.gameState(getClientGameState(state))
				.actions(new GameActions()
						.actions(availableActions.stream().map(ga -> {
							Action action = new Action();
							final ActionType actionType = ActionType.valueOf(ga.getActionType().toString());
							action.actionType(actionType)
									.actionSuffix(ga.getActionSuffix());

							if (ga.getTargetRequirement() != null) {
								final Action.TargetRequirementEnum targetRequirement = Action.TargetRequirementEnum.valueOf(ga.getTargetRequirement().toString());
								action.targetRequirement(targetRequirement);
							}

							if (ga.getTargetKey() != null) {
								action.targetKey(ga.getTargetKey().getId());
							}

							if (ga.getSource() != null) {
								action.source(ga.getSource().getId());
							}

							return action;
						}).collect(Collectors.toList()))
				));
	}

	@Override
	public void onMulligan(String id, Player player, List<Card> cards) {
		final GameContext simulatedContext = new GameContext(player, null, new GameLogic(), new DeckFormat());
		sendMessage(new ServerToClientMessage()
				.id(id)
				.messageType(MessageType.ON_MULLIGAN)
				.startingCards(cards.stream().map(c -> Games.getEntity(simulatedContext, c)).collect(Collectors.toList())));
	}

	public ServerWebSocket getPrivateSocket() {
		return privateSocket;
	}

	private void setPrivateSocket(ServerWebSocket privateSocket) {
		this.privateSocket = privateSocket;
	}

}
