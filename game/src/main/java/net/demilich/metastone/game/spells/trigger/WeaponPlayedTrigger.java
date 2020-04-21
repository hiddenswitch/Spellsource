package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.WeaponEquippedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class WeaponPlayedTrigger extends WeaponEquippedTrigger {
	public WeaponPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		WeaponEquippedEvent weaponEvent = (WeaponEquippedEvent) event;

		if (weaponEvent.getSource() == null) {
			return false;
		}

		return super.innerQueues(event, enchantment, host);
	}
}
