package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * Just before the game starts. Mulligans have occurred.
 */
public class PreGameStartEvent extends BasicGameEvent {

	public PreGameStartEvent(GameContext context, int playerId) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.PRE_GAME_START, context, playerId, -1);
	}
}
