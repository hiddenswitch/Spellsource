package net.demilich.metastone.game.cards;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.desc.ActorCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.utils.Attribute;

import java.util.ArrayList;
import java.util.List;

public abstract class ActorCard extends Card {
	List<SpellDesc> deathrattleEnchantments = new ArrayList<>();

	ActorCard(ActorCardDesc desc) {
		super(desc);
	}

	public boolean hasDeathrattle() {
		return ((ActorCardDesc) desc).deathrattle != null;
	}

	public ActorCardDesc getDesc() {
		return (ActorCardDesc) desc;
	}

	public void addDeathrattle(SpellDesc deathrattle) {
		// TODO: Should Forlorn Stalker affect cards with deathrattle added this way?
		deathrattleEnchantments.add(deathrattle);
	}

	public boolean hasTrigger() {
		return getDesc().trigger != null || (getDesc().triggers != null && getDesc().triggers.length > 0);
	}

	public boolean hasAura() {
		return getDesc().aura != null
				|| getDesc().auras != null && getDesc().auras.length > 0;
	}

	public boolean hasCardCostModifier() {
		return getDesc().cardCostModifier != null;
	}

	public boolean hasBattlecry() {
		return getDesc().battlecry != null;
	}

	@Override
	public ActorCard clone() {
		ActorCard clone = (ActorCard) super.clone();
		clone.deathrattleEnchantments = new ArrayList<>();
		deathrattleEnchantments.forEach(de -> clone.deathrattleEnchantments.add(de.clone()));
		return clone;
	}

	/**
	 * Applies this card's effects (everything except mana cost, attack and HP) to the specified actor. Mutates the
	 * provided instance.
	 *
	 * @param instance An actor to apply effects to
	 * @return The provided actor.
	 */
	@Suspendable
	public Actor applyText(Actor instance) {
		ActorCardDesc desc = (ActorCardDesc) this.desc;
		instance.setBattlecry(desc.getBattlecryAction());
		instance.setRace((getAttributes() != null && getAttributes().containsKey(Attribute.RACE)) ?
				(Race) getAttribute(Attribute.RACE) :
				desc.race);

		if (desc.deathrattle != null) {
			instance.getAttributes().remove(Attribute.DEATHRATTLES);
			instance.addDeathrattle(desc.deathrattle);
		}

		if (deathrattleEnchantments.size() > 0) {
			deathrattleEnchantments.forEach(instance::addDeathrattle);
		}

		if (desc.trigger != null) {
			instance.addEnchantment(desc.trigger.create());
		}

		if (desc.triggers != null) {
			for (TriggerDesc trigger : desc.triggers) {
				instance.addEnchantment(trigger.create());
			}
		}

		if (desc.aura != null) {
			final Aura enchantment = desc.aura.createInstance();
			instance.addEnchantment(enchantment);
		}

		if (desc.auras != null) {
			for (AuraDesc auraDesc : desc.auras) {
				instance.addEnchantment(auraDesc.createInstance());
			}
		}

		if (desc.cardCostModifier != null) {
			instance.setCardCostModifier(desc.cardCostModifier.createInstance());
		}

		return instance;
	}

	public int getAttack() {
		return getAttributeValue(Attribute.ATTACK);
	}

	public int getBonusAttack() {
		return getAttributeValue(Attribute.ATTACK_BONUS);
	}

	public int getHp() {
		return getAttributeValue(Attribute.HP);
	}

	public int getBonusHp() {
		return getAttributeValue(Attribute.HP_BONUS);
	}

	public int getBaseAttack() {
		return getAttributeValue(Attribute.BASE_ATTACK);
	}

	public int getBaseHp() {
		return getAttributeValue(Attribute.BASE_HP);
	}
}
