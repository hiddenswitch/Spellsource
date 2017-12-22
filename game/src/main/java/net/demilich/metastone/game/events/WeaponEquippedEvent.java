package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;

public final class WeaponEquippedEvent extends GameEvent implements HasCard {
	private final Weapon weapon;
	private final Card source;

	public WeaponEquippedEvent(GameContext context, Weapon weapon, Card source) {
		super(context, weapon.getOwner(), -1);
		this.weapon = weapon;
		this.source = source;
	}

	@Override
	public Entity getEventTarget() {
		return getWeapon();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.WEAPON_EQUIPPED;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	@Override
	public Entity getSource(GameContext context) {
		return source;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public Card getCard() {
		return source;
	}
}
