package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * An actor hosts common functionality between minions, weapons and heroes. Actors have hitpoints; they can be
 * destroyed; they have deathrattles; they have other enchantments.
 * <p>
 * When actors enter or exit their respective zones ({@link net.demilich.metastone.game.targeting.Zones#HERO}, {@link
 * net.demilich.metastone.game.targeting.Zones#BATTLEFIELD}, {@link net.demilich.metastone.game.targeting.Zones#WEAPON}),
 * {@link net.demilich.metastone.game.events.BoardChangedEvent} will be raised.
 */
public abstract class Actor extends Entity {

	public Actor() {
	}

	/**
	 * Refreshes the number of attacks an {@link Actor} has, typically to 1 or the number of {@link Attribute#WINDFURY}
	 * attacks if the actor has Windfury.
	 *
	 */
	public void refreshAttacksPerRound() {
		int attacks = 1;
		if (hasAttribute(Attribute.MEGA_WINDFURY)) {
			attacks = GameLogic.MEGA_WINDFURY_ATTACKS;
		} else if (hasAttribute(Attribute.WINDFURY) || hasAttribute(Attribute.AURA_WINDFURY)) {
			attacks = GameLogic.WINDFURY_ATTACKS;
		}
		setAttribute(Attribute.NUMBER_OF_ATTACKS, attacks);
	}

	public boolean canAttackThisTurn(GameContext context) {
		if (hasAttribute(Attribute.CANNOT_ATTACK)
				|| hasAttribute(Attribute.AURA_CANNOT_ATTACK)) {
			return false;
		}
		if (hasAttribute(Attribute.FROZEN)) {
			return false;
		}
		if (hasAttribute(Attribute.SUMMONING_SICKNESS) && !hasAttribute(Attribute.CHARGE) && !hasAttribute(Attribute.AURA_CHARGE)
				&& !hasAttribute(Attribute.RUSH) && !hasAttribute(Attribute.AURA_RUSH)) {
			return false;
		}
		if (hasAttribute(Attribute.PERMANENT)) {
			return false;
		}
		return hasNonZeroAttack(context) && ((getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + getAttributeValue(Attribute.EXTRA_ATTACKS)) > 0 || hasAttribute(Attribute.UNLIMITED_ATTACKS));
	}

	protected boolean hasNonZeroAttack(GameContext context) {
		return getAttack() > 0;
	}

	@Override
	public Actor clone() {
		return (Actor) super.clone();
	}

	public int getArmor() {
		return getAttributeValue(Attribute.ARMOR);
	}

	public int getAttack() {
		int bonuses = getAttributeValue(Attribute.ATTACK_BONUS)
				+ getAttributeValue(Attribute.AURA_ATTACK_BONUS) + getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS)
				+ getAttributeValue(Attribute.CONDITIONAL_ATTACK_BONUS);
		if (hasAttribute(Attribute.ATTACK_BONUS_MULTIPLIER) && getAttributeValue(Attribute.ATTACK_BONUS_MULTIPLIER) != 0) {
			bonuses *= getAttributeValue(Attribute.ATTACK_BONUS_MULTIPLIER);
		}

		if (hasAttribute(Attribute.AURA_ATTACK_BONUS_MULTIPLIER) && getAttributeValue(Attribute.AURA_ATTACK_BONUS_MULTIPLIER) != 0) {
			bonuses *= getAttributeValue(Attribute.AURA_ATTACK_BONUS_MULTIPLIER);
		}

		int attack = getAttributeValue(Attribute.ATTACK) + bonuses - getAttributeValue(Attribute.WITHERED);

		if (hasAttribute(Attribute.ATTACK_MULTIPLIER) && getAttributeValue(Attribute.ATTACK_MULTIPLIER) != 0) {
			attack *= getAttributeValue(Attribute.ATTACK_MULTIPLIER);
		}

		if (hasAttribute(Attribute.AURA_ATTACK_MULTIPLIER) && getAttributeValue(Attribute.AURA_ATTACK_MULTIPLIER) != 0) {
			attack *= getAttributeValue(Attribute.AURA_ATTACK_MULTIPLIER);
		}

		return Math.max(0, attack);
	}

	public int getBaseAttack() {
		return getAttributeValue(Attribute.BASE_ATTACK);
	}

	public int getBaseHp() {
		return getAttributeValue(Attribute.BASE_HP);
	}

	/**
	 * The current number of hitpoints this actor has.
	 *
	 * @return The hitpoints.
	 */
	public int getHp() {
		return getAttributeValue(Attribute.HP);
	}

	/**
	 * Returns the maximum amount of hitpoints this actor can have, considering all of its bonuses from effects and {@link
	 * net.demilich.metastone.game.spells.aura.Aura}s.
	 *
	 * @return The maximum hitpoints.
	 */
	public int getMaxHp() {
		return getAttributeValue(Attribute.MAX_HP) + getAttributeValue(Attribute.HP_BONUS)
				+ getAttributeValue(Attribute.AURA_HP_BONUS);
	}

	/**
	 * Indicates whether or not the actor is mortally wounded.
	 * <p>
	 * A mortally wounded actor hasn't necessarily been taken off the board and put into the {@link
	 * net.demilich.metastone.game.targeting.Zones#GRAVEYARD} yet. This is useful for preventing effects from impacting
	 * already dead minions before a {@link GameLogic#endOfSequence()} has been called.
	 *
	 * @return {@code true} if the minion's health is less than 1 or if the minion has the {@link Attribute#DESTROYED}
	 * attribute.
	 */
	@Override
	public boolean isDestroyed() {
		if (hasAttribute(Attribute.PERMANENT)) {
			return hasAttribute(Attribute.DESTROYED);
		}
		return (getHp() < 1 || super.isDestroyed());
	}

	public boolean isWounded() {
		return getHp() != getMaxHp();
	}

	public void modifyAuraHpBonus(int value) {
		modifyAttribute(Attribute.AURA_HP_BONUS, value);
		if (value > 0) {
			modifyAttribute(Attribute.HP, value);
		}
		if (getHp() > getMaxHp()) {
			setHp(getMaxHp());
		}
	}

	@Override
	public void modifyHpBonus(int value) {
		modifyAttribute(Attribute.HP_BONUS, value);
		modifyAttribute(Attribute.HP, value);

		if (getHp() > getMaxHp()) {
			setHp(getMaxHp());
		}
	}

	public void setAttack(int value) {
		setAttribute(Attribute.ATTACK, value);
	}

	public void setBaseAttack(int value) {
		setAttribute(Attribute.BASE_ATTACK, value);
	}

	public void setBaseHp(int value) {
		setAttribute(Attribute.BASE_HP, value);
	}

	public void setHp(int value) {
		setAttribute(Attribute.HP, value);
	}

	public void setMaxHp(int value) {
		setAttribute(Attribute.MAX_HP, value);
	}

	public String getHeroClass() {
		return getSourceCard().getHeroClass();
	}

	@Override
	public void setOwner(int ownerIndex) {
		super.setOwner(ownerIndex);
	}

	@Override
	public void setId(int id) {
		super.setId(id);
	}

	public void setRace(String race) {
		if (race != null) {
			setAttribute(Attribute.RACE, race);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
				.append("name", getName())
				.append("id", getId())
				.append("zone", getZone())
				.append(getAttack() + "/" + getHp())
				.append("description", getDescription())
				.toString();
	}

	@Override
	public String getCardInventoryId() {
		if (super.getCardInventoryId() != null) {
			return super.getCardInventoryId();
		}

		if (getSourceCard() != null) {
			return getSourceCard().getCardInventoryId();
		}

		return null;
	}

	@Override
	public Actor getCopy() {
		Actor clone = this.clone();
		clone.setEntityLocation(EntityLocation.UNASSIGNED);
		clone.setId(IdFactory.UNASSIGNED);
		clone.setOwner(IdFactory.UNASSIGNED);
		clone.getAttributes().put(Attribute.COPIED_FROM, this.getReference());
		clone.getAttributes().remove(Attribute.TRANSFORM_REFERENCE);
		// Clear aura attributes when copying an actor
		for (var auraAttribute : Attribute.getAuraAttributes()) {
			clone.getAttributes().remove(auraAttribute);
		}
		return clone;
	}

	/**
	 * Gets the zones where the {@link CardDesc#getTrigger()} and {@link CardDesc#getTriggers()} are active by default.
	 *
	 * @return
	 */
	public abstract Zones[] getDefaultActiveTriggerZones();
}
