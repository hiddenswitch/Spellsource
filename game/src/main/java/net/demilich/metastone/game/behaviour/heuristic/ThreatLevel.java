package net.demilich.metastone.game.behaviour.heuristic;

/**
 * Indicates how to value a minion given the health of the player that owns it.
 */
enum ThreatLevel {
	/**
	 * A player with full health treats its minions as being the least threatening. This tends to disfavor playing taunt
	 * minions.
	 */
	GREEN,
	/**
	 * Corresponds to a wounded player. Taunt minions tend to be valued higher in this framework.
	 */
	YELLOW,
	/**
	 * Corresponds to a player that is likely to be killed in one turn (e.g., if the player is wounded to 15 or less
	 * health, or if the opponent has lethal on the board).
	 */
	RED
}
