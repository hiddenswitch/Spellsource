package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * The player started their turn.
 */
public class TurnStartEvent extends BasicGameEvent {

	public TurnStartEvent(GameContext context, int playerId) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_START, true, context, null, null , playerId, -1);
	}
}
