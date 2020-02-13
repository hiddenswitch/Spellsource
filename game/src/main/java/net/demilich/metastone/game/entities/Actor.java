package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HasDeathrattleEnchantments;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.IdFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An actor hosts common functionality between minions, weapons and heroes. Actors have hitpoints; they can be
 * destroyed; they have deathrattles; they have other enchantments.
 * <p>
 * When actors enter or exit their respective zones ({@link net.demilich.metastone.game.targeting.Zones#HERO}, {@link
 * net.demilich.metastone.game.targeting.Zones#BATTLEFIELD}, {@link net.demilich.metastone.game.targeting.Zones#WEAPON}),
 * {@link net.demilich.metastone.game.events.BoardChangedEvent} will be raised.
 */
public abstract class Actor extends Entity implements HasEnchantments, HasDeathrattleEnchantments {

	private Card sourceCard;
	private List<Enchantment> enchantments = new ArrayList<>();
	private CardCostModifier cardCostModifier;
	private int frozenDeathrattlesSize;

	public Actor(Card sourceCard) {
		this.setName(sourceCard != null ? sourceCard.getName() : null);
		this.sourceCard = sourceCard;
	}

	@Override
	public void addDeathrattle(SpellDesc deathrattleSpell) {
		if (!hasAttribute(Attribute.DEATHRATTLES)) {
			setAttribute(Attribute.DEATHRATTLES, new ArrayList<SpellDesc>());
		}
		if (getDeathrattles().size() < GameLogic.MAX_DEATHRATTLES) {
			getDeathrattles().add(deathrattleSpell);
		}
	}

	@Override
	public void addEnchantment(Enchantment enchantment) {
		enchantments.add(enchantment);
		enchantment.setHost(this);
	}

	public boolean canAttackThisTurn() {
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
		return getAttack() > 0 && ((getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + getAttributeValue(Attribute.EXTRA_ATTACKS)) > 0 || hasAttribute(Attribute.UNLIMITED_ATTACKS));
	}

	@Override
	public void clearEnchantments() {
		this.enchantments = new ArrayList<>();
	}

	@Override
	public Actor clone() {
		Actor clone = (Actor) super.clone();
		clone.attributes = this.attributes.clone();
		clone.clearEnchantments();
		for (Enchantment trigger : getEnchantments()) {
			clone.enchantments.add(trigger.clone());
		}
		if (hasAttribute(Attribute.DEATHRATTLES)
				|| (getDeathrattles().size() > 0)) {
			clone.getAttributes().remove(Attribute.DEATHRATTLES);
			for (SpellDesc deathrattleSpell : getDeathrattles()) {
				SpellDesc deathrattleClone = deathrattleSpell.clone();
				clone.addDeathrattle(deathrattleClone);
			}
		}
		if (hasAttribute(Attribute.BATTLECRY)
				|| (getDeathrattles().size() > 0)) {
			clone.getAttributes().remove(Attribute.BATTLECRY);
			for (BattlecryDesc battlecry : getBattlecries()) {
				BattlecryDesc battlecryClone = battlecry.clone();
				clone.addBattlecry(battlecryClone);
			}
		}

		if (cardCostModifier != null) {
			clone.cardCostModifier = cardCostModifier.clone();
		}

		updateTriggers();
		return clone;
	}

	public void addBattlecry(BattlecryDesc battlecry) {
		if (!hasAttribute(Attribute.BATTLECRY)) {
			setAttribute(Attribute.BATTLECRY, new ArrayList<BattlecryDesc>());
		}
		getBattlecries().add(battlecry);
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

	@SuppressWarnings("unchecked")
	@NotNull
	public List<BattlecryDesc> getBattlecries() {
		Object attribute = getAttribute(Attribute.BATTLECRY);
		if (attribute == null) {
			return new ArrayList<>();
		} else {
			return (List<BattlecryDesc>) attribute;
		}
	}

	public CardCostModifier getCardCostModifier() {
		return cardCostModifier;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public List<SpellDesc> getDeathrattles() {
		Object attribute = getAttribute(Attribute.DEATHRATTLES);
		if (attribute == null) {
			return new ArrayList<>();
		} else {
			return (List<SpellDesc>) attribute;
		}
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

	@Override
	public Card getSourceCard() {
		return sourceCard;
	}

	@Override
	public List<Enchantment> getEnchantments() {
		return new ArrayList<>(enchantments);
	}

	@Override
	public boolean hasEnchantment() {
		return enchantments.size() != 0;
	}


	/**
	 * Indicates whether or not the actor is mortally wounded.
	 * <p>
	 * A mortally wounded actor hasn't necessarily been taken off the board and put into the {@link
	 * net.demilich.metastone.game.targeting.Zones#GRAVEYARD} yet. This is useful for preventing effects from impacting
	 * already dead minions before a {@link GameLogic#endOfSequence()} has been called.
	 *
	 * @return {@code true} if the minion's health is less than 1 or if the minion has the {@link Attribute#DESTROYED}
	 * 		attribute.
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

	public void setBattlecry(BattlecryDesc battlecry) {
		if (battlecry != null) {
			if (!hasAttribute(Attribute.BATTLECRY)) {
				setAttribute(Attribute.BATTLECRY, new ArrayList<BattlecryDesc>());
			}
			if (getBattlecries().size() == 0) {
				getBattlecries().add(battlecry);
			} else {
				getBattlecries().set(0, battlecry);
			}
		}
	}

	public void setCardCostModifier(CardCostModifier cardCostModifier) {
		this.cardCostModifier = cardCostModifier;
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
		updateTriggers();
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		updateTriggers();
	}

	private void updateTriggers() {
		for (Enchantment trigger : enchantments) {
			trigger.setHost(this);
		}
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
	public List<SpellDesc> getDeathrattleEnchantments() {
		return getDeathrattles();
	}

	@Override
	public Actor getCopy() {
		Actor clone = this.clone();
		clone.setEntityLocation(EntityLocation.UNASSIGNED);
		clone.setId(IdFactory.UNASSIGNED);
		clone.setOwner(IdFactory.UNASSIGNED);
		clone.getAttributes().put(Attribute.COPIED_FROM, this.getReference());
		clone.getAttributes().remove(Attribute.TRANSFORM_REFERENCE);
		// Clear aura buffs when copying an actor
		clone.getAttributes().remove(Attribute.AURA_ATTACK_BONUS);
		clone.getAttributes().remove(Attribute.AURA_HP_BONUS);
		clone.getAttributes().remove(Attribute.AURA_UNTARGETABLE_BY_SPELLS);
		clone.getAttributes().remove(Attribute.AURA_TAUNT);
		clone.getAttributes().remove(Attribute.AURA_STEALTH);
		clone.getAttributes().remove(Attribute.AURA_CANNOT_ATTACK);
		clone.getAttributes().remove(Attribute.AURA_CANNOT_ATTACK_HEROES);
		clone.getAttributes().remove(Attribute.AURA_CARD_ID);
		clone.getAttributes().remove(Attribute.AURA_CHARGE);
		clone.getAttributes().remove(Attribute.AURA_ECHO);
		clone.getAttributes().remove(Attribute.AURA_IMMUNE);
		clone.getAttributes().remove(Attribute.AURA_INVOKE);
		clone.getAttributes().remove(Attribute.AURA_LIFESTEAL);
		clone.getAttributes().remove(Attribute.AURA_POISONOUS);
		clone.getAttributes().remove(Attribute.AURA_RUSH);
		clone.getAttributes().remove(Attribute.AURA_WINDFURY);
		clone.getAttributes().remove(Attribute.AURA_IMMUNE_WHILE_ATTACKING);
		clone.getAttributes().remove(Attribute.AURA_TAKE_DOUBLE_DAMAGE);
		clone.getAttributes().remove(Attribute.AURA_SPELL_DAMAGE);
		clone.getAttributes().remove(Attribute.AURA_COSTS_HEALTH_INSTEAD_OF_MANA);
		// TODO: When auras put attributes on minions that aren't attack or hp bonuses, they must be removed here
		return clone;
	}

	/**
	 * Indicates that all the deathrattles currently on this actor should be frozen, i.e., they are intrinsic to this
	 * actor's text.
	 */
	public void freezeDeathrattles() {
		if (getAttributes().get(Attribute.DEATHRATTLES) instanceof Boolean) {
			frozenDeathrattlesSize = 0;
			return;
		}
		frozenDeathrattlesSize = getDeathrattles().size();
	}

	/**
	 * Removes the deathrattles that were not frozen, i.e., added as part of other effects.
	 */
	@Override
	public void clearAddedDeathrattles() {
		for (int i = getDeathrattles().size() - 1; i >= frozenDeathrattlesSize; i--) {
			getDeathrattles().remove(i);
		}
	}
}
