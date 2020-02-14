package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.events.WeaponDestroyedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class WeaponDestroyedTrigger extends EventTrigger {

	public WeaponDestroyedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		WeaponDestroyedEvent weaponDestroyedEvent = (WeaponDestroyedEvent) event;
		return weaponDestroyedEvent.getWeapon().getOwner() == host.getOwner();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.WEAPON_DESTROYED;
	}

}
