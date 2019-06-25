package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetType;

public class BeforeMinionSummonedTrigger extends AbstractSummonTrigger {

	public BeforeMinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create() {
		return new EventTriggerDesc(BeforeMinionSummonedTrigger.class);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.BEFORE_SUMMON;
	}

}
