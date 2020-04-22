package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

/**
 * Fires whenever the board may have changed.
 */
public class BoardChangedEvent extends BasicGameEvent {

	public BoardChangedEvent(GameContext context) {
		super(GameEvent.EventTypeEnum.BOARD_CHANGED, context, -1, -1);
	}
}
