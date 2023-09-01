package net.demilich.metastone.game.environment;

import com.hiddenswitch.spellsource.common.GameState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * A collection of environment variables.
 * <p>
 * This enum contains the keys to the {@link GameState#environment} field, and they help implement various card texts
 * that modify other card texts.
 */
public enum Environment {
	/**
	 * This variable stores the player that was chosen as the starting player
	 */
	STARTING_PLAYER,
	/**
	 * This variable stores the minions that are pending a summon.
	 * <p>
	 * This implements Faceless Manipulator.
	 */
	SUMMON_REFERENCE_STACK,
	/**
	 * This variable stores the last minion that was killed.
	 */
	KILLED_MINION,
	/**
	 * This variable stores the number of minions that have been destroyed so far during this sequence.
	 */
	DESTROYED_THIS_SEQUENCE_COUNT,
	/**
	 * This variable, when defined, specifies that the target of a spell or physical attack should be overriden.
	 */
	TARGET_OVERRIDE,
	/**
	 * This variable stores a reference to the last card played for each player
	 */
	LAST_CARD_PLAYED,
	/**
	 * This variable stores a reference to the last card played for each player before the card this attribute is written
	 * on. Helps implement Study.
	 */
	LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE,
	/**
	 * This variable stores a reference to the last spell played by each player this turn.
	 * <p>
	 * Implements Recurring Torrent.
	 */
	LAST_SPELL_PLAYED_THIS_TURN,
	/**
	 * This variable stores a stack of attackers.
	 */
	ATTACKER_REFERENCE_STACK,
	/**
	 * This variable stores a stack of event targets, as events are executed one after another until all events are
	 * processed.
	 * <p>
	 * This implements card texts that read, "take double damage," and implements
	 * {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}.
	 */
	EVENT_TARGET_REFERENCE_STACK,
	/**
	 * This variable stores the current target of an event or action. This variable is consulted for
	 * {@link net.demilich.metastone.game.targeting.EntityReference#TARGET}.
	 */
	TARGET,
	/**
	 * This variable stores the current target of a spell.
	 */
	SPELL_TARGET,
	/**
	 * This variable stores a reference to the new minion as a result of a transform.
	 * <p>
	 * This implements Faceless Manipulator.
	 */
	TRANSFORM_REFERENCE,
	/**
	 * This variable stores overrides to the next amount of damage dealt by a
	 * {@link net.demilich.metastone.game.spells.DamageSpell}.
	 * <p>
	 * This implements cards like Magic Armor.
	 */
	DAMAGE_STACK,
	/**
	 * This variable stores a stack of entities that were output by spells. The variable can be referenced with
	 * {@link net.demilich.metastone.game.targeting.EntityReference#OUTPUT}.
	 * <p>
	 * This implements Ivory Knight.
	 */
	OUTPUTS,
	/**
	 * This variable stores the mana cost of the last card played.
	 * <p>
	 * This implements Atiesh, the weapon granted by Medivh, the Guardian
	 */
	LAST_MANA_COST,
	/**
	 * This variable stores how many minions have been summoned this turn.
	 */
	LAST_SUMMON_THIS_TURN,
	/**
	 * This implements Living Mana and Earthen Scales.
	 */
	SPELL_VALUE_STACK,
	/**
	 * This implements Spellstones
	 */
	EVENT_VALUE_STACK,
	/**
	 * This implements Frostmourne and other entity tracking effects. For example, it keeps track of which particular
	 * entities a particular Frostmourne has destroyed.
	 *
	 * @see net.demilich.metastone.game.spells.custom.ResurrectFromEntityStorageSpell for resurrecting minions from this
	 * list.
	 * @see net.demilich.metastone.game.spells.custom.StoreEntitySpell for adding entities to the list.
	 */
	ENTITY_LIST,
	/**
	 * This entity list implements Lynessa Sunsorrow, keeping track of each spell cast a player cast on his own minions.
	 */
	@Deprecated
	LYNESSA_SUNSORROW_ENTITY_LIST,
	/**
	 * This entity list implements Bonefetcher, keeping track of the cards that were shuffled in each player's deck.
	 */
	SHUFFLED_CARDS_LIST,
	/**
	 * Stores a stack of event sources.
	 */
	EVENT_SOURCE_REFERENCE_STACK,
	/**
	 * Stores the stack of trigger hosts as triggers are processed
	 */
	TRIGGER_HOST_STACK,
	/**
	 * Stores the next target of an
	 * {@link net.demilich.metastone.game.logic.GameLogic#castSpell(int, SpellDesc, EntityReference, EntityReference,
	 * TargetSelection, boolean, GameAction)} invocation to allow it to be modified.
	 */
	TARGET_OVERRIDE_STACK,
	/**
	 * Stores the deathrattles triggered throughout the game.
	 */
	DEATHRATTLES_TRIGGERED,
	/**
	 * Stores the cards that are currently selected to be randomly played, preventing them from being randomly played more
	 * than once per sequence.
	 */
	RANDOMLY_PLAYED_QUEUE,
	/**
	 * Stores the currently processing aftermaths.
	 * <p>
	 * This prevents aftermaths from recursively triggering themselves.
	 */
	PROCESSING_AFTERMATHS_STACK,
}
