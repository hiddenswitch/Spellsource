package net.demilich.metastone.game.entities.weapons;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.targeting.TargetSelection;

public final class Weapon extends Actor {

	private boolean active;
	private SpellDesc onEquip;
	private SpellDesc onUnequip;

	public Weapon(Card sourceCard) {
		super(sourceCard);
	}

	@Override
	public Weapon clone() {
		return (Weapon) super.clone();
	}

	@Override
	public int getHp() {
		return getDurability();
	}

	@Override
	public int getAttack() {
		return getWeaponDamage();
	}

	public int getBaseDurability() {
		return getAttributeValue(Attribute.BASE_HP);
	}

	public int getDurability() {
		return getAttributeValue(Attribute.HP);
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.WEAPON;
	}

	public int getMaxDurability() {
		return getMaxHp();
	}

	public int getWeaponDamage() {
		return Math.max(0, getAttributeValue(Attribute.ATTACK) +
				getAttributeValue(Attribute.CONDITIONAL_ATTACK_BONUS) +
				getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS) +
				getAttributeValue(Attribute.ATTACK_BONUS) +
				getAttributeValue(Attribute.AURA_ATTACK_BONUS));
	}

	public boolean isActive() {
		return active;
	}

	public boolean isBroken() {
		return !hasAttribute(Attribute.HP) || getHp() <= 0;
	}

	@Override
	public boolean isDestroyed() {
		return hasAttribute(Attribute.DESTROYED) || isBroken();
	}

	@Suspendable
	public void onEquip(GameContext context, Player player) {
		if (onEquip != null) {
			context.getLogic().castSpell(player.getId(), onEquip, getReference(), EntityReference.NONE, TargetSelection.NONE, false, null);
		}
	}

	@Suspendable
	public void onUnequip(GameContext context, Player player) {
		if (onUnequip != null) {
			context.getLogic().castSpell(player.getId(), onUnequip, getReference(), EntityReference.NONE, TargetSelection.NONE, false, null);
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setOnEquip(SpellDesc onEquip) {
		this.onEquip = onEquip;
	}

	public void setOnUnequip(SpellDesc onUnequip) {
		this.onUnequip = onUnequip;
	}
}
