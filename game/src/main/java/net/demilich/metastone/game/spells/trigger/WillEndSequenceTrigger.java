package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class WillEndSequenceTrigger extends EventTrigger {
	public WillEndSequenceTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public WillEndSequenceTrigger() {
		super(new EventTriggerDesc(WillEndSequenceTrigger.class));
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.WILL_END_SEQUENCE;
	}
}
