package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BeforeMinionSummonedTrigger extends AbstractSummonTrigger {

	public BeforeMinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create() {
		return new EventTriggerDesc(BeforeMinionSummonedTrigger.class);
	}

	@Override
	public GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.BEFORE_SUMMON;
	}

}
