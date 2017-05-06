package com.hiddenswitch.proto3.net.common;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import net.demilich.metastone.game.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.targeting.Zones;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState implements Serializable {
	private static final long serialVersionUID = 1L;

	public final List<GameAction> actionStack;
	public final List<GameEvent> eventStack;
	public final Player player1;
	public final Player player2;
	public final CardCollection tempCards;
	public final HashMap<Environment, Object> environment;
	public final List<CardCostModifier> cardCostModifiers;
	public final TriggerManager triggerManager;
	public final int currentId;
	public final int activePlayerId;
	public final TurnState turnState;
	public final long timestamp;

	public GameState(GameContext fromContext) {
		this(fromContext, fromContext.getTurnState());
	}

	public GameState(GameContext fromContext, TurnState turnState) {
		GameContext clone = fromContext.clone();
		this.timestamp = System.nanoTime();
		player1 = clone.getPlayer1();
		player2 = clone.getPlayer2();
		tempCards = clone.getTempCards();
		environment = clone.getEnvironment();
		currentId = clone.getLogic().getIdFactory().getInternalId();
		triggerManager = clone.getTriggerManager();
		cardCostModifiers = clone.getCardCostModifiers();
		actionStack = clone.getActionStack();
		eventStack = clone.getEventStack();
		activePlayerId = clone.getActivePlayerId();
		this.turnState = turnState;
	}

	public boolean isValid() {
		return player1 != null
				&& player2 != null
				&& environment != null
				&& triggerManager != null
				&& turnState != null;
	}

	@SuppressWarnings("unchecked")
	protected Stream<Entity> getEntities() {
		return Stream.of(player1, player2).flatMap(p -> Stream.of(new Zones[]{
				Zones.PLAYER,
				Zones.BATTLEFIELD,
				Zones.DECK,
				Zones.GRAVEYARD,
				Zones.HAND,
				Zones.HERO,
				Zones.HERO_POWER,
				Zones.SET_ASIDE_ZONE,
				Zones.DISCOVER,
				Zones.WEAPON,
				Zones.SECRET
		}).flatMap(z -> ((EntityZone<Entity>) p.getZone(z)).stream()));
	}

	protected Map<Integer, EntityLocation> getMap() {
		return getEntities().collect(Collectors.toMap(Entity::getId, Entity::getEntityLocation));
	}

	public MapDifference<Integer, EntityLocation> to(GameState nextState) {
		return Maps.difference(getMap(), nextState.getMap());
	}

	public MapDifference<Integer, EntityLocation> start() {
		return Maps.difference(Collections.emptyMap(), getMap());
	}
}
