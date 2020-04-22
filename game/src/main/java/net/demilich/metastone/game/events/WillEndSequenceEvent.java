package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * The sequence is about to end.
 *
 * @see GameLogic#endOfSequence() for more about ending sequences.
 */
public class WillEndSequenceEvent extends BasicGameEvent {

	public WillEndSequenceEvent(GameContext context) {
		super(EventTypeEnum.WILL_END_SEQUENCE, context, context.getActivePlayerId(), context.getActivePlayerId());
	}
}
