package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * The sequence is about to end.
 *
 * @see GameLogic#endOfSequence() for more about ending sequences.
 */
public class WillEndSequenceEvent extends BasicGameEvent {

	public WillEndSequenceEvent(GameContext context) {
		super(GameEventType.WILL_END_SEQUENCE, context, context.getActivePlayerId(), context.getActivePlayerId());
	}
}
