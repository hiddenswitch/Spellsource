package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardReceivedTrigger extends EventTrigger {

	public CardReceivedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTrigger create() {
		return new EventTriggerDesc(CardReceivedTrigger.class).create();
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DRAW_CARD;
	}

}
