package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.events.TargetAcquisitionEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class TargetAcquisitionTrigger extends EventTrigger {

	public TargetAcquisitionTrigger() {
		this(new EventTriggerDesc(TargetAcquisitionTrigger.class));
	}

	public TargetAcquisitionTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		TargetAcquisitionEvent targetAcquisitionEvent = (TargetAcquisitionEvent) event;

		ActionType actionType = (ActionType) getDesc().get(EventTriggerArg.ACTION_TYPE);
		if (actionType != null && targetAcquisitionEvent.getActionType() != actionType) {
			return false;
		}

		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TARGET_ACQUISITION;
	}
}

