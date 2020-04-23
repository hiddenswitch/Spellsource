package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.events.HealEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever a {@code target} is healed.
 * <p>
 * Obeys the {@link EventTriggerArg#TARGET_ENTITY_TYPE} constraint.
 */
public class HealingTrigger extends EventTrigger {

	public HealingTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		HealEvent healEvent = (HealEvent) event;

		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null) {
			if (healEvent.getTarget().getEntityType() != targetEntityType) {
				return false;
			}
		}

		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.HEAL;
	}

}