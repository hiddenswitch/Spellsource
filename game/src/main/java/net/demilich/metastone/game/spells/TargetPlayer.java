package net.demilich.metastone.game.spells;

/**
 * Indicates a player reference.
 */
public enum TargetPlayer {
	/**
	 * The player that owns the {@code source} of the spell or action.
	 */
	SELF,
	/**
	 * The opponent of the player that owns the {@code source} of the spell or action.
	 */
	OPPONENT,
	/**
	 * When this is specified, the spell or action is typically repeated twice, once for each player and starting with the
	 * {@code source} owner.
	 */
	BOTH,
	/**
	 * The player that owns the {@code target} of the spell or action.
	 */
	OWNER,
	/**
	 * The player whose turn it currently is.
	 */
	ACTIVE,
	/**
	 * The player whose turn it is not.
	 */
	INACTIVE
}
