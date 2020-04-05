package com.hiddenswitch.spellsource.common;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.targeting.IdFactoryImpl;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.logic.TurnState;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The fields that correspond to a complete state of the game.
 * <p>
 * This game state is used, in practice, as a snapshot of the game in a specific point in time using {@link
 * GameContext#getGameStateCopy()} and to copy games using {@link GameContext#setGameState(GameState)}.
 * <p>
 * Creating a game state using {@link GameContext#getGameState()} produces an instance whose objects are shared with the
 * {@code GameContext}. Mutating the player objects, for example, in such a game state will also mutate the player
 * objects in the context. To retrieve a copy, use {@link GameContext#getGameState()} or {@link #clone()} the state.
 * <p>
 * While the fields in this instance are immutable, the engine does not prevent changes to fields within the fields.
 * This is <b>not</b> an immutable data type.
 *
 * @see GameContext for more about how state in Spellsource works
 */
public class GameState implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private final Player player1;
	private final Player player2;
	private final CardList tempCards;
	private final Map<Environment, Object> environment;
	private final Map<String, AtomicInteger> variables;
	private final TriggerManager triggerManager;
	private final int currentId;
	private final int activePlayerId;
	private final TurnState turnState;
	private final long timestamp;
	private final int turnNumber;
	private final DeckFormat deckFormat;
	private final Long millisRemaining;

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
		deckFormat = fromContext.getDeckFormat();
		millisRemaining = fromContext.getMillisRemaining();
		variables = fromContext.getVariables();
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
	                  DeckFormat deckFormat,
	                  Long millisRemaining,
	                  Map<String, AtomicInteger> variables) {
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
		this.millisRemaining = millisRemaining;
		this.variables = variables;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


	@SuppressWarnings("unchecked")
	protected Stream<Entity> getEntities() {
		return Stream.of(getPlayer1(), getPlayer2()).flatMap(p -> Stream.of(Zones.values()).flatMap(z -> ((EntityZone<Entity>) p.getZone(z)).stream()));
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
	 * @return A {@link Maps#difference(Map, Map)} call where an empty map is the left argument and this game state is the
	 * right argument.
	 */
	public MapDifference<Integer, EntityLocation> start() {
		return Maps.difference(Collections.emptyMap(), getMap());
	}

	@Override
	public GameState clone() {
		return new GameState(
				getPlayer1(),
				getPlayer2(),
				getTempCards(),
				getEnvironment(),
				getTriggerManager(),
				getCurrentId(),
				getActivePlayerId(),
				getTurnState(),
				getTimestamp(),
				getTurnNumber(),
				getDeckFormat(),
				getMillisRemaining(),
				getVariables());
	}


	/**
	 * A player object corresponding to the arbitrarily-decided first player of the game.
	 *
	 * @see Player for more about players.
	 */
	public Player getPlayer1() {
		return player1;
	}

	/**
	 * A player object corresponding to the arbitrarily-decided second player of the game.
	 *
	 * @see Player for more about players.
	 */
	public Player getPlayer2() {
		return player2;
	}

	/**
	 * A {@link CardList} of cards that are temporarily created in this game.
	 */
	public CardList getTempCards() {
		return tempCards;
	}

	/**
	 * Gets a reference to the game context's environment, a piece of game state that keeps tracks of which minions are
	 * currently being summoned, which targets are being targeted, how much damage is set to be dealt, etc.
	 * <p>
	 * This helps implement a variety of complex rules in the game.
	 *
	 * @see Environment for more about the environment variables.
	 */
	public Map<Environment, Object> getEnvironment() {
		return environment;
	}

	/**
	 * An instance of the class that manages and stores the state for {@link Trigger} objects.
	 *
	 * @see Trigger for more about triggers.
	 * @see GameContext#fireGameEvent(GameEvent) for more about firing triggers and raising events.
	 */
	public TriggerManager getTriggerManager() {
		return triggerManager;
	}

	/**
	 * The next ID to generate in an {@link IdFactoryImpl}/
	 */
	public int getCurrentId() {
		return currentId;
	}

	/**
	 * The currently active player.
	 */
	public int getActivePlayerId() {
		return activePlayerId;
	}

	/**
	 * The current {@link TurnState} of the game.
	 */
	public TurnState getTurnState() {
		return turnState;
	}

	/**
	 * The timestamp of when this {@link GameState} was accessed.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * The current turn number.
	 */
	public int getTurnNumber() {
		return turnNumber;
	}

	/**
	 * The deck format of this game.
	 */
	public DeckFormat getDeckFormat() {
		return deckFormat;
	}

	/**
	 * The amount of time left in a timer, such as an end of turn or mulligan timer, until the player's actions are
	 * automatically terminated. When {@code null}, no timer is set.
	 */
	public Long getMillisRemaining() {
		return millisRemaining;
	}

	/**
	 * Gets the variables in the game. Spells are responsible for managing their own state here.
	 */
	public Map<String, AtomicInteger> getVariables() {
		return variables;
	}
}
