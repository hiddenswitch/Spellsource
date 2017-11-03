package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.GameContext;

public interface Heuristic {

	double getScore(GameContext context, int playerId);

	void onActionSelected(GameContext context, int playerId);
}
