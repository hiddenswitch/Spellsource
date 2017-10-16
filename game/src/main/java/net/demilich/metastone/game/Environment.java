package net.demilich.metastone.game;

import com.hiddenswitch.spellsource.common.GameState;

/**
 * A collection of environment variables.
 * <p>
 * This enum contains the keys to the {@link GameState#environment} field, and they
 * help implement various card texts that modify other card texts.
 */
public enum Environment {
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
	 * This variable, when defined, specifies that the target of a spell or physical attack should be overriden.
	 */
	TARGET_OVERRIDE,
	/**
	 * This variable stores the current attacker.
	 */
	ATTACKER_REFERENCE,
	/**
	 * This variable stores a stack of event targets, as events are executed one after another until all events are
	 * processed.
	 * <p>
	 * This implements card texts that read, "take double damage."
	 */
	EVENT_TARGET_REFERENCE_STACK,
	/**
	 * This variable stores the current target of an event or action. This variable is consulted for {@link
	 * net.demilich.metastone.game.targeting.EntityReference#TARGET}.
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
	 * This variable stores overrides to the next amount of damage dealt by a {@link
	 * net.demilich.metastone.game.spells.DamageSpell}.
	 * <p>
	 * This implements cards like Magic Armor.
	 */
	DAMAGE_STACK,
	/**
	 * This variable stores the current card being played.
	 */
	PENDING_CARD,
	/**
	 * This variable stores a card for effects to reference with {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_CARD}.
	 * <p>
	 * This implements Ivory Knight.
	 */
	EVENT_CARD,
	/**
	 * This variable stores the parent {@link net.demilich.metastone.game.cards.ChooseOneCard}.
	 */
	CHOOSE_ONE_CARD,
	/**
	 * This variable stores the mana cost of the last card played.
	 * <p>
	 * This implements Atiesh, the weapon granted by Medivh, the Guardian
	 */
	LAST_MANA_COST,
	/**
	 * This implements Living Mana and Earthen Scales.
	 */
	SPELL_VALUE_STACK;

	public boolean customClone() {
		return (this == SUMMON_REFERENCE_STACK ||
				this == EVENT_TARGET_REFERENCE_STACK ||
				this == DAMAGE_STACK ||
				this == SPELL_VALUE_STACK);
	}
}
