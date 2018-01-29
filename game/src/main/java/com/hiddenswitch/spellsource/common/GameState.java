package com.hiddenswitch.spellsource.common;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.IdFactoryImpl;
import net.demilich.metastone.game.utils.TurnState;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.targeting.Zones;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The fields that correspond to a complete state of the game.
 * <p>
 * Notably, this class contains {@link Player} objects, whose {@link net.demilich.metastone.game.behaviour.Behaviour}
 * fields are not strictly state. These can be safely serialized since behaviours generally do not contain any state.
 */
public class GameState implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	/**
	 * A player object corresponding to the arbitrarily-decided first player of the game.
	 *
	 * @see Player for more about players.
	 */
	public final Player player1;
	/**
	 * A player object corresponding to the arbitrarily-decided second player of the game.
	 *
	 * @see Player for more about players.
	 */
	public final Player player2;
	/**
	 * A {@link CardList} of cards that are temporarily created in this game.
	 */
	public final CardList tempCards;
	/**
	 * Gets a reference to the game context's environment, a piece of game state that keeps tracks of which minions
	 * are currently being summoned, which targets are being targeted, how much damage is set to be dealt, etc.
	 * <p>
	 * This helps implement a variety of complex rules in the game.
	 *
	 * @see Environment for more about the environment variables.
	 */
	public final Map<Environment, Object> environment;
	/**
	 * An instance of the class that manages and stores the state for {@link Trigger}
	 * objects.
	 *
	 * @see Trigger for more about triggers.
	 * @see GameContext#fireGameEvent(GameEvent) for more about firing triggers and raising events.
	 */
	public final TriggerManager triggerManager;
	/**
	 * The next ID to generate in an {@link IdFactoryImpl}/
	 */
	public final int currentId;
	/**
	 * The currently active player.
	 */
	public final int activePlayerId;
	/**
	 * The current {@link TurnState} of the game.
	 */
	public final TurnState turnState;
	/**
	 * The timestamp of when this {@link GameState} was accessed.
	 */
	public final long timestamp;
	/**
	 * The current turn number.
	 */
	public final int turnNumber;
	/**
	 * The deck format of this game.
	 */
	public final DeckFormat deckFormat;

	public GameState(GameContext fromContext) {
		this(fromContext, fromContext.getTurnState(), false);
	}

	public GameState(GameContext fromContext, TurnState turnState) {
		this(fromContext, turnState, false);
	}

	public GameState(GameContext fromContext, TurnState turnState, boolean byReference) {
		GameContext clone = fromContext;
		if (!byReference) {
			clone = fromContext.clone();
		}

		this.timestamp = System.nanoTime();
		player1 = clone.getPlayer1();
		player2 = clone.getPlayer2();
		tempCards = clone.getTempCards();
		environment = clone.getEnvironment();
		currentId = clone.getLogic().getInternalId();
		triggerManager = clone.getTriggerManager();
		activePlayerId = clone.getActivePlayerId();
		turnNumber = clone.getTurn();
		this.turnState = turnState;
		this.deckFormat = fromContext.getDeckFormat();
	}

	private GameState(Player player1,
	                  Player player2,
	                  CardList tempCards,
	                  Map<Environment, Object> environment,
	                  TriggerManager triggerManager,
	                  int currentId, int activePlayerId,
	                  TurnState turnState,
	                  long timestamp,
	                  int turnNumber,
	                  DeckFormat deckFormat) {
		this.player1 = player1;
		this.player2 = player2;
		this.tempCards = tempCards;
		this.environment = environment;
		this.triggerManager = triggerManager;
		this.currentId = currentId;
		this.activePlayerId = activePlayerId;
		this.turnState = turnState;
		this.timestamp = timestamp;
		this.turnNumber = turnNumber;
		this.deckFormat = deckFormat;
	}


	@SuppressWarnings("unchecked")
	protected Stream<Entity> getEntities() {
		return Stream.of(player1, player2).flatMap(p -> Stream.of(Zones.values()).flatMap(z -> ((EntityZone<Entity>) p.getZone(z)).stream()));
	}

	/**
	 * Gets a map containing all the {@link EntityLocation} objects in this game state.
	 *
	 * @return A map.
	 */
	protected Map<Integer, EntityLocation> getMap() {
		return getEntities().collect(Collectors.toMap(Entity::getId, Entity::getEntityLocation));
	}

	/**
	 * Gets a difference between this game state and the {@code nextState} in terms of entity locations.
	 *
	 * @param nextState A state in the future.
	 * @return The difference.
	 */
	public MapDifference<Integer, EntityLocation> to(GameState nextState) {
		return Maps.difference(getMap(), nextState.getMap());
	}

	/**
	 * Gets a {@link MapDifference} that corresponds to this state being the first state.
	 *
	 * @return A {@link Maps#difference(Map, Map)} call where an empty map is the left argument and this game state is
	 * the right argument.
	 */
	public MapDifference<Integer, EntityLocation> start() {
		return Maps.difference(Collections.emptyMap(), getMap());
	}

	@Override
	public GameState clone() {
		return new GameState(
				player1,
				player2,
				tempCards,
				environment,
				triggerManager,
				currentId,
				activePlayerId,
				turnState,
				timestamp,
				turnNumber,
				deckFormat
		);
	}
}
