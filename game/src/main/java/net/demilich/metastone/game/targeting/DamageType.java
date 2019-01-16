package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.entities.Actor;

/**
 * Types of damage.
 */
public enum DamageType {
	/**
	 * Physical damage is caused by physical attacks by actors.
	 *
	 * @see net.demilich.metastone.game.logic.GameLogic#fight(Player, Actor, Actor, PhysicalAttackAction) for more about
	 * 		physical damage.
	 */
	PHYSICAL,
	/**
	 * Fatigue damage is caused by drawing from an empty deck.
	 *
	 * @see net.demilich.metastone.game.logic.GameLogic#checkAndDealFatigue(Player) for more about fatigue damage.
	 */
	FATIGUE,
	/**
	 * Magical damage is caused by spells and effects. It is typically rendered by missiles in the client.
	 */
	MAGICAL
}
