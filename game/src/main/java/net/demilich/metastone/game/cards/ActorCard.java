package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.desc.ActorCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.utils.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class ActorCard extends Card {
	List<SpellDesc> deathrattleEnchantments = new ArrayList<>();

	ActorCard(ActorCardDesc desc) {
		super(desc);
	}

	public boolean hasDeathrattle() {
		return ((ActorCardDesc) desc).deathrattle != null;
	}

	public void addDeathrattle(SpellDesc deathrattle) {
		// TODO: Should Forlorn Stalker affect cards with deathrattle added this way?
		deathrattleEnchantments.add(deathrattle);
	}

	@Override
	public ActorCard clone() {
		ActorCard clone = (ActorCard) super.clone();
		clone.deathrattleEnchantments = new ArrayList<>();
		deathrattleEnchantments.forEach(de -> clone.deathrattleEnchantments.add(de.clone()));
		return clone;
	}

	protected Actor populate(Actor instance) {
		ActorCardDesc desc = (ActorCardDesc) this.desc;
		instance.setBattlecry(desc.getBattlecryAction());
		instance.setRace(desc.race);

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
			final Aura enchantment = desc.aura.create();
			instance.addEnchantment(enchantment);
		}

		if (desc.cardCostModifier != null) {
			instance.setCardCostModifier(desc.cardCostModifier.create());
		}

		return instance;
	}
}
