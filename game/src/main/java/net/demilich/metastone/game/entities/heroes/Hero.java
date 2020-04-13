package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.cards.Attribute;

import java.lang.ref.WeakReference;

public final class Hero extends Actor {
	private String heroClass;
	private WeakReference<Player> playerRef = new WeakReference<>(null);

	public Hero(Card heroCard, Player player) {
		super(heroCard);
		setPlayer(player);
		setName(heroCard.getName());
		setHeroClass(heroCard.getHeroClass());
	}

	public void activateWeapon(boolean active) {
		if (getWeapon() != null) {
			getWeapon().setActive(active);
		}
	}

	@Override
	public Hero clone() {
		Hero clone = (Hero) super.clone();
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
		if (getHeroPowerZone().size() > 0) {
			return getHeroPowerZone().get(0);
		} else {
			return null;
		}
	}

	public Weapon getWeapon() {
		if (getWeaponZone().size() > 0) {
			return getWeaponZone().get(0);
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
		int armorChange = newArmor - originalArmor;
		return armorChange;
	}

	public void setHeroClass(String heroClass) {
		this.heroClass = heroClass;
	}

	public EntityZone<Card> getHeroPowerZone() {
		return playerRef.get().getHeroPowerZone();
	}

	public EntityZone<Weapon> getWeaponZone() {
		return playerRef.get().getWeaponZone();
	}

	public Hero setPlayer(Player player) {
		playerRef = new WeakReference<>(player);
		return this;
	}
}
