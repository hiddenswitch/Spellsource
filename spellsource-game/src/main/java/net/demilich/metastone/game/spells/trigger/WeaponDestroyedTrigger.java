package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.events.WeaponDestroyedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class WeaponDestroyedTrigger extends EventTrigger {

	public WeaponDestroyedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		WeaponDestroyedEvent weaponDestroyedEvent = (WeaponDestroyedEvent) event;
		return weaponDestroyedEvent.getTarget().getOwner() == host.getOwner();
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.WEAPON_DESTROYED;
	}

}
