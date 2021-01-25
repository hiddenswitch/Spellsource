package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;

/**
 * The player ended their turn.
 */
public class TurnEndEvent extends BasicGameEvent {

	public TurnEndEvent(GameContext context, int playerId) {
		super(GameEventType.TURN_END, true, context, context.getPlayer(playerId), playerId, playerId);
	}
}
