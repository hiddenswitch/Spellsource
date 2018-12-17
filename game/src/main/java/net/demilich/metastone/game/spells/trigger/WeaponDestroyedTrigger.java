package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.WeaponDestroyedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class WeaponDestroyedTrigger extends EventTrigger {

	private static final long serialVersionUID = -8088379205417836241L;

	public WeaponDestroyedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		WeaponDestroyedEvent weaponDestroyedEvent = (WeaponDestroyedEvent) event;
		return weaponDestroyedEvent.getWeapon().getOwner() == host.getOwner();
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.WEAPON_DESTROYED;
	}

}
