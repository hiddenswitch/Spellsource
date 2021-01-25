package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * The {@link #getTargetPlayerId()} ended their sequence.
 *
 * @see GameLogic#endOfSequence() for when sequences end
 */
public class DidEndSequenceEvent extends BasicGameEvent {

	public DidEndSequenceEvent(GameContext context) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.DID_END_SEQUENCE, context, context.getActivePlayerId(), -1);
	}
}
