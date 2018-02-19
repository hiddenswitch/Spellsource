package net.demilich.metastone.game.cards;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayWeaponCardAction;
import net.demilich.metastone.game.cards.desc.WeaponCardDesc;
import net.demilich.metastone.game.entities.weapons.Weapon;

public class WeaponCard extends ActorCard {

	private static final Set<Attribute> ignoredAttributes = new HashSet<Attribute>(
			Arrays.asList(Attribute.PASSIVE_TRIGGERS, Attribute.DECK_TRIGGER, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO,
					Attribute.TRANSFORM_REFERENCE));

	public WeaponCard(WeaponCardDesc desc) {
		super(desc);
		setAttribute(Attribute.BASE_ATTACK, desc.damage);
		setAttribute(Attribute.ATTACK, desc.damage);
		setAttribute(Attribute.BASE_HP, desc.durability);
		setAttribute(Attribute.HP, desc.durability);
		setAttribute(Attribute.MAX_HP, desc.durability);
		this.desc = desc;
	}

	protected Weapon createWeapon(Attribute... tags) {
		Weapon weapon = new Weapon(this);
		WeaponCardDesc desc = (WeaponCardDesc) getDesc();
		// assign battlecry if there is one specified
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				weapon.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		weapon.setAttack(getDamage());
		weapon.setBaseAttack(getBaseDamage());
		weapon.setMaxHp(getDurability());
		weapon.setHp(weapon.getMaxDurability());
		weapon.setBaseHp(getBaseDurability());
		weapon.setBattlecry(desc.getBattlecryAction());

		populate(weapon);

		weapon.setOnEquip(desc.onEquip);
		weapon.setOnUnequip(desc.onUnequip);
		return weapon;
	}

	public Weapon getWeapon() {
		return createWeapon();
	}

	@Override
	public PlayCardAction play() {
		return new PlayWeaponCardAction(getReference());
	}

	public int getDamage() {
		return getAttack();
	}

	public int getBonusDamage() {
		return getBonusAttack();
	}

	public int getDurability() {
		return getHp();
	}

	public int getBonusDurability() {
		return getBonusHp();
	}

	public int getBaseDamage() {
		return getBaseAttack();
	}

	public int getBaseDurability() {
		return getBaseHp();
	}

}
