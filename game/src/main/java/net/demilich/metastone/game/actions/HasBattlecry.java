package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.spells.desc.BattlecryDesc;

/**
 * Indicates this action could later create prompt the user for a battlecry targeting option.
 * <p>
 * Choose-one effects on actors are implemented as choosing their battlecry.
 */
public interface HasBattlecry {
	/**
	 * Gets a battlecry description for this object.
	 *
	 * @return
	 */
	BattlecryDesc getBattlecry();

	/**
	 * Sets the battlecry. May not be supported.
	 *
	 * @param action
	 */
	void setBattlecry(BattlecryDesc action);
}
