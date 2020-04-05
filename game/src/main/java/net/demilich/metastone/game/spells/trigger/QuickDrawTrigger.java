package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires when a card is drawn for any reason besides the turn start.
 */
public final class QuickDrawTrigger extends CardDrawnTrigger {

	public QuickDrawTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		return super.innerQueues(event, host) &&
				!event.getGameContext().getPlayer(event.getTargetPlayerId()).hasAttribute(Attribute.STARTING_TURN);
	}
}
