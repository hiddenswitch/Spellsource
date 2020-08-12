package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * The player ended their turn.
 */
public class TurnEndEvent extends BasicGameEvent {

	public TurnEndEvent(GameContext context, int playerId) {
		super(GameEvent.EventTypeEnum.TURN_END, true, context, context.getPlayer(playerId), playerId, playerId);
	}
}
