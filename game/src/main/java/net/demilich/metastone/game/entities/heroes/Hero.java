package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;

import java.util.EnumMap;
import java.util.Map;

public final class Hero extends Actor {
	private String heroClass;
	private EntityZone<Card> heroPowerZone = new EntityZone<>(getOwner(), Zones.HERO_POWER);
	private EntityZone<Weapon> weaponZone = new EntityZone<>(getOwner(), Zones.WEAPON);

	public Hero(Card heroCard, Card heroPower) {
		super(heroCard);
		setName(heroCard.getName());
		this.setHeroClass(heroCard.getHeroClass());
		this.setHeroPower(heroPower);
	}

	public void activateWeapon(boolean active) {
		if (getWeapon() != null) {
			getWeapon().setActive(active);
		}
	}

	@Override
	public Hero clone() {
		Hero clone = (Hero) super.clone();
		clone.heroPowerZone = heroPowerZone.clone();
		clone.weaponZone = weaponZone.clone();
		return clone;
	}

	@Override
	public int getAttack() {
		int attack = super.getAttack();
		if (getWeapon() != null && getWeapon().isActive()) {
			attack += getWeapon().getWeaponDamage();
		}
		return attack;
	}

	public Map<Attribute, Object> getAttributesCopy() {
		Map<Attribute, Object> copy = new EnumMap<>(Attribute.class);
		for (Attribute attribute : getAttributes().keySet()) {
			copy.put(attribute, getAttributes().get(attribute));
		}
		return copy;
	}

	public int getEffectiveHp() {
		return getHp() + getArmor();
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.HERO;
	}

	@Override
	public String getHeroClass() {
		return heroClass;
	}

	public Card getHeroPower() {
		if (heroPowerZone.size() > 0) {
			return heroPowerZone.get(0);
		} else {
			return null;
		}
	}

	public Weapon getWeapon() {
		if (weaponZone.size() > 0) {
			return weaponZone.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Changes the amount of armor the hero has.
	 *
	 * @param armor The requested change in armor.
	 * @return The amount the armor changed. If damage is being dealt, then the armor will change {@code -Infinity < armor
	 * 		<= 0} if it is possible.
	 */
	public int modifyArmor(final int armor) {
		// armor cannot fall below zero
		final int originalArmor = getArmor();
		int newArmor = Math.max(originalArmor + armor, 0);
		setAttribute(Attribute.ARMOR, newArmor);
		return newArmor - originalArmor;
	}

	/**
	 * Changes the amount of decaying armor the hero has.
	 *
	 * @param armor The requested change in decaying armor.
	 * @return The amount the armor changed. If damage is being dealt, then the armor will change {@code -Infinity < armor
	 * 		<= 0} if it is possible.
	 */
	public int modifyDecayingArmor(final int armor) {
		// decaying armor cannot fall below zero
		final int originalArmor = getDecayingArmor();
		int newArmor = Math.max(originalArmor + armor, 0);
		setAttribute(Attribute.DECAYING_ARMOR, newArmor);
		return newArmor - originalArmor;
	}

	public void setHeroClass(String heroClass) {
		this.heroClass = heroClass;
	}

	public void setHeroPower(Card heroPower) {
		if (heroPowerZone.size() > 0) {
			heroPowerZone.remove(0);
		}
		heroPower.setOwner(getOwner());
		this.heroPowerZone.add(heroPower);

	}

	@Override
	public void setOwner(int ownerIndex) {
		super.setOwner(ownerIndex);
		if (heroPowerZone.getPlayer() == -1) {
			heroPowerZone.setPlayer(ownerIndex);
		}
		if (weaponZone.getPlayer() == -1) {
			weaponZone.setPlayer(ownerIndex);
		}
		getHeroPower().setOwner(ownerIndex);
		if (getWeapon() != null) {
			getWeapon().setOwner(ownerIndex);
		}
	}

	public void setWeapon(Weapon weapon) {
		if (weaponZone.size() > 0) {
			weaponZone.remove(0);
		}
		if (weapon != null) {
			weapon.setOwner(getOwner());
			this.weaponZone.add(weapon);
		}
	}

	public EntityZone<Card> getHeroPowerZone() {
		return heroPowerZone;
	}

	public EntityZone<Weapon> getWeaponZone() {
		return weaponZone;
	}
}
