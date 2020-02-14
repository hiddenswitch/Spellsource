package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;

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
	INACTIVE,
	/**
	 * The first player.
	 */
	PLAYER_1,
	/**
	 * The second player.
	 */
	PLAYER_2,
	/**
	 * Indicates either player. Used for filters and conditions.
	 */
	EITHER;

	/**
	 * Returns a target player specific to the specified owner.
	 *
	 * @param owner
	 * @return
	 */
	public static TargetPlayer getTargetPlayerForOwner(int owner) {
		switch (owner) {
			case GameContext.PLAYER_1:
				return TargetPlayer.PLAYER_1;
			case GameContext.PLAYER_2:
				return TargetPlayer.PLAYER_2;
			default:
				throw new UnsupportedOperationException("invalid owner");
		}
	}
}
