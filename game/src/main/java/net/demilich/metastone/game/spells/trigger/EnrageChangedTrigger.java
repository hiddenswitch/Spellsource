package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class EnrageChangedTrigger extends EventTrigger {

	public EnrageChangedTrigger() {
		this(new EventTriggerDesc(EnrageChangedTrigger.class));
	}

	public EnrageChangedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return event.getTarget() == host;
	}

	@Override
	public EventTypeEnum interestedIn() {
		return EventTypeEnum.ENRAGE_CHANGED;
	}

}
