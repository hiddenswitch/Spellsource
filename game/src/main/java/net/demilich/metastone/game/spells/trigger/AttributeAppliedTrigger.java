package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.AttributeAppliedEvent;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.cards.Attribute;

public class AttributeAppliedTrigger extends EventTrigger {

	public AttributeAppliedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		AttributeAppliedEvent e = (AttributeAppliedEvent) event;
		Attribute attribute = (Attribute) getDesc().get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		return (targetEntityType == null || e.getTarget().getEntityType() == targetEntityType)
				&& e.getAttribute() == attribute;

	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ATTRIBUTE_APPLIED;
	}
}
