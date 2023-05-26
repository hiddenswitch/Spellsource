package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DrawCardEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardDrawnTrigger extends AbstractCardTrigger {

	public CardDrawnTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		DrawCardEvent drawEvent = (DrawCardEvent) event;

		if (!drawEvent.isDrawn()) {
			return false;
		}

		return super.innerQueues(event, enchantment, host);
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.DRAW_CARD;
	}

}
