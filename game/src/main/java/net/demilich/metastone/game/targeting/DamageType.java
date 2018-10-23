package net.demilich.metastone.game.targeting;

/**
 * Types of damage. Currently only used for rendering, but eventually could be used for gameplay.
 */
public enum DamageType {
	/**
	 * Physical damage is caused by physical attacks by actors.
	 */
	PHYSICAL,
	/**
	 * Magical damage is caused by spells and effects. It is typically rendered by missiles in the client.
	 */
	MAGICAL
}
