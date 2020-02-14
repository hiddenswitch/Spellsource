package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;

public class WeaponDestroyedEvent extends GameEvent {

	private final Weapon weapon;

	public WeaponDestroyedEvent(GameContext context, Weapon weapon) {
		super(context, weapon.getOwner(), -1);
		this.weapon = weapon;
	}

	@Override
	public Entity getEventTarget() {
		return getWeapon();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.WEAPON_DESTROYED;
	}

	public Weapon getWeapon() {
		return weapon;
	}

}
