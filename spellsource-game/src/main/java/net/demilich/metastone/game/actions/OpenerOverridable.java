package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.spells.desc.OpenerDesc;

/**
 * Indicates this action could later create prompt the user for a battlecry targeting option.
 * <p>
 * Choose-one effects on actors are implemented as choosing their battlecry.
 */
public interface OpenerOverridable {
	/**
	 * Gets a battlecry description for this object.
	 *
	 * @return
	 */
	OpenerDesc getOpener();

	/**
	 * Sets the battlecry. May not be supported.
	 *
	 * @param action
	 */
	void setOpener(OpenerDesc action);

	void setResolveOpener(boolean resolveOpener);

	boolean getResolveOpener();
}
