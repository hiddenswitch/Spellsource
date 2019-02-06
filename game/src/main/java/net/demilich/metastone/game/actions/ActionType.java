package net.demilich.metastone.game.actions;

/**
 * Action types correspond to things the player can do. They can be used in {@link
 * net.demilich.metastone.game.spells.trigger.TargetAcquisitionTrigger} to perform effects when players do certain
 * targeting actions.
 */
public enum ActionType {
	/**
	 * Indicates the action isn't player-initiated and is of a special system type. Currently unused.
	 */
	SYSTEM,
	/**
	 * Indicates the action ends the turn.
	 */
	END_TURN,
	/**
	 * Indicates the action is an attack of one {@link net.demilich.metastone.game.entities.Actor} against another.
	 */
	PHYSICAL_ATTACK,
	/**
	 * Indicates the action is casting a spell, possibly with a target.
	 */
	SPELL,
	/**
	 * Indicates the action is summoning a minion, possibly with a minion targeted to summon it "next to."
	 */
	SUMMON,
	/**
	 * Indicates the action is to use a hero power, possibly with a target.
	 */
	HERO_POWER,
	/**
	 * Indicates the action is a battlecry. When not targeted, the battlecry action is executed automatically.
	 */
	BATTLECRY,
	/**
	 * Indicates the action resulted in equipping a weapon.
	 */
	EQUIP_WEAPON,
	/**
	 * Indicates the action is to discover among choices.
	 */
	DISCOVER,
	/**
	 * Indicates the action is playing a hero card, replacing the current hero.
	 */
	HERO
}
