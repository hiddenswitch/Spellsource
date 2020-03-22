package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;

public final class NonDrainDamageReceivedTrigger extends DamageReceivedTrigger {

	public NonDrainDamageReceivedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		DamageEvent damageEvent=(DamageEvent)event;
		if (damageEvent.getDamageType()== DamageTypeEnum.DRAIN) {
			return false;
		}
		return super.innerQueues(event, host);
	}
}
