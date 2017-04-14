package com.hiddenswitch.proto3.net.impl.server;

import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.Entity;
import com.hiddenswitch.proto3.net.client.models.GameEvent;
import com.hiddenswitch.proto3.net.client.models.GameEvent.EventTypeEnum;
import com.hiddenswitch.proto3.net.client.models.PhysicalAttackEvent;
import com.hiddenswitch.proto3.net.common.Client;
import com.hiddenswitch.proto3.net.common.GameState;
import com.hiddenswitch.proto3.net.impl.util.Zones;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.DoNothingBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
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
		final int localPlayerId = userId.equals(workingContext.getPlayer1().getUserId()) ? workingContext.getPlayer1().getId() : workingContext.getPlayer2().getId();
		// Handle the event types here.
		if (event instanceof net.demilich.metastone.game.events.PhysicalAttackEvent) {
			final net.demilich.metastone.game.events.PhysicalAttackEvent physicalAttackEvent
					= (net.demilich.metastone.game.events.PhysicalAttackEvent) event;
			final Actor attacker = physicalAttackEvent.getAttacker();
			final Actor defender = physicalAttackEvent.getDefender();
			final int damageDealt = physicalAttackEvent.getDamageDealt();
			final PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt);
			clientEvent.physicalAttack(physicalAttack);
		} else if (event instanceof AfterPhysicalAttackEvent) {
			final AfterPhysicalAttackEvent physicalAttackEvent = (AfterPhysicalAttackEvent) event;
			final Actor attacker = physicalAttackEvent.getAttacker();
			final Actor defender = physicalAttackEvent.getDefender();
			final int damageDealt = physicalAttackEvent.getDamageDealt();
			final PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt);
			clientEvent.afterPhysicalAttack(physicalAttack);
		} else if (event instanceof DrawCardEvent) {
			final DrawCardEvent drawCardEvent = (DrawCardEvent) event;
			clientEvent.drawCard(new GameEventDrawCard()
					.to(Games.getLocation(workingContext, userId, drawCardEvent.getCard()))
					.card(Games.getEntity(workingContext, drawCardEvent.getCard()))
					.drawn(drawCardEvent.isDrawn()));
		} else if (event instanceof KillEvent) {
			final KillEvent killEvent = (KillEvent) event;
			final net.demilich.metastone.game.entities.Entity victim = killEvent.getVictim();
			final Entity entity = Games.getEntity(workingContext, victim);
			CardLocation location = null;
			if (victim instanceof Minion) {
				location = localPlayerId == victim.getOwner()
						? new CardLocation().zone(Zones.LOCAL_BATTLEFIELD).position(killEvent.getFormerBoardPosition())
						: new CardLocation().zone(Zones.OPPONENT_BATTLEFIELD).position(killEvent.getFormerBoardPosition());
			} else if (victim instanceof Weapon) {
				location = localPlayerId == victim.getOwner()
						? new CardLocation().zone(Zones.LOCAL_WEAPON).position(0)
						: new CardLocation().zone(Zones.OPPONENT_WEAPON).position(0);
			} else if (victim instanceof Hero) {
				location = localPlayerId == victim.getOwner()
						? new CardLocation().zone(Zones.LOCAL_HERO).position(0)
						: new CardLocation().zone(Zones.OPPONENT_HERO).position(0);
			}

			clientEvent.kill(new GameEventKill()
					.location(location)
					.victim(entity));
		} else if (event instanceof CardPlayedEvent) {
			final CardPlayedEvent cardPlayedEvent = (CardPlayedEvent) event;
			final Card card = cardPlayedEvent.getCard();
			clientEvent.cardPlayed(new GameEventCardPlayed()
					.location(Games.getLocation(workingContext, userId, card))
					.card(Games.getEntity(workingContext, card)));
		} else if (event instanceof SummonEvent) {
			final SummonEvent summonEvent = (SummonEvent) event;
			final Minion minion = (Minion) summonEvent.getMinion();

			clientEvent.summon(new GameEventSummon()
					.to(Games.getLocation(workingContext, userId, minion))
					.minion(Games.getEntity(workingContext, summonEvent.getMinion()))
					.source(Games.getEntity(workingContext, summonEvent.getSource())));

			if (summonEvent.getSource() != null) {
				clientEvent.getSummon().from(Games.getLocation(workingContext, userId, summonEvent.getSource()));
			}
		} else if (event instanceof DamageEvent) {
			final DamageEvent damageEvent = (DamageEvent) event;
			clientEvent.damage(new GameEventDamage()
					.damage(damageEvent.getDamage())
					.source(Games.getEntity(workingContext, damageEvent.getSource()))
					.victim(Games.getEntity(workingContext, damageEvent.getVictim())));
		} else if (event instanceof AfterSpellCastedEvent) {
			final AfterSpellCastedEvent afterSpellCastedEvent = (AfterSpellCastedEvent) event;
			clientEvent.afterSpellCasted(new GameEventAfterSpellCasted()
					.sourceCard(Games.getEntity(workingContext, afterSpellCastedEvent.getSourceCard()))
					.spellTarget(Games.getEntity(workingContext, afterSpellCastedEvent.getEventTarget())));
		}

		clientEvent.eventSource(Games.getEntity(workingContext, event.getEventSource()));
		clientEvent.eventTarget(Games.getEntity(workingContext, event.getEventTarget()));
		clientEvent.targetPlayerId(event.getTargetPlayerId());
		clientEvent.sourcePlayerId(event.getSourcePlayerId());

		sendMessage(new ServerToClientMessage()
				.messageType(MessageType.ON_GAME_EVENT)
				.event(clientEvent));
	}

	private PhysicalAttackEvent getPhysicalAttack(GameContext workingContext, Actor attacker, Actor defender, int damageDealt) {
		return new PhysicalAttackEvent()
				.attackerLocation(Games.getLocation(workingContext, userId, attacker))
				.attacker(Games.getEntity(workingContext, attacker))
				.defenderLocation(Games.getLocation(workingContext, userId, defender))
				.defender(Games.getEntity(workingContext, defender))
				.damageDealt(damageDealt);
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
