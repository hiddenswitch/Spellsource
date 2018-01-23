package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.GameContext;

public interface IdFactory {
	int UNASSIGNED = -1;
	int PLAYER_1 = GameContext.PLAYER_1;
	int PLAYER_2 = GameContext.PLAYER_2;

	int generateId();
}
