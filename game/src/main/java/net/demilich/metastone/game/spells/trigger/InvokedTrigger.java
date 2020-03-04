package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class InvokedTrigger extends AbstractCardTrigger {

	public InvokedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.INVOKED;
	}
}
