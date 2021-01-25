package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;

/**
 * Fires whenever the board may have changed.
 */
public class BoardChangedEvent extends BasicGameEvent {

	public BoardChangedEvent(GameContext context) {
		super(GameEventType.BOARD_CHANGED, context, -1, -1);
	}
}
