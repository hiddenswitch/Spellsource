package net.demilich.metastone.game.actions;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;

/**
 * Indicates this action could later create prompt the user for a battlecry targeting option.
 *
 * Choose-one effects on actors are implemented as choosing their battlecry.
 */
public interface HasBattlecry {
	BattlecryAction getBattlecryAction();

	void setBattlecryAction(BattlecryAction action);
}
