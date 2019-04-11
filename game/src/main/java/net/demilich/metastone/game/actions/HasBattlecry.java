package net.demilich.metastone.game.actions;

/**
 * Indicates this action could later create prompt the user for a battlecry targeting option.
 * <p>
 * Choose-one effects on actors are implemented as choosing their battlecry.
 */
public interface HasBattlecry {
	BattlecryAction getBattlecry();

	void setBattlecry(BattlecryAction action);
}
