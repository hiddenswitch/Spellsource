package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;

/**
 * The player started their turn.
 */
public class TurnStartEvent extends BasicGameEvent {

	public TurnStartEvent(GameContext context, int playerId) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.TURN_START, true, context, null, null , playerId, playerId);
	}
}
