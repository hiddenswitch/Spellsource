package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;

public final class NonDrainDamageReceivedTrigger extends DamageReceivedTrigger {

	public NonDrainDamageReceivedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		DamageEvent damageEvent=(DamageEvent)event;
		if (damageEvent.getDamageType().contains(DamageType.DRAIN)) {
			return false;
		}
		return super.innerQueues(event, enchantment, host);
	}
}
