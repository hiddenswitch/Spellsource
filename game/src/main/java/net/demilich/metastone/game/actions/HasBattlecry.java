package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;

public interface HasBattlecry {
	BattlecryAction getBattlecryAction();

	void setBattlecryAction(BattlecryAction action);
}
