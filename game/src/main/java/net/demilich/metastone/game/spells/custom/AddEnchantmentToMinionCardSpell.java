package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.trigger.BeforeMinionSummonedTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetType;

/**
 * Puts the enchantment written in this spell into play as soon as the minion is summoned from the {@code target} minion
 * card.
 */
public final class AddEnchantmentToMinionCardSpell extends AddEnchantmentSpell {

	public static SpellDesc create(Card card, EnchantmentDesc enchantmentDesc) {
		SpellDesc desc = new SpellDesc(AddEnchantmentToMinionCardSpell.class);
		desc.put(SpellArg.TRIGGER,enchantmentDesc);
		desc.put(SpellArg.TARGET, card.getReference());
		return desc;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EnchantmentDesc enchantment = (EnchantmentDesc) desc.get(SpellArg.TRIGGER);

		desc = new SpellDesc(AddEnchantmentSpell.class);
		EnchantmentDesc summonedEnchantment = new EnchantmentDesc();
		summonedEnchantment.eventTrigger = BeforeMinionSummonedTrigger.create();
		summonedEnchantment.eventTrigger.put(EventTriggerArg.HOST_TARGET_TYPE, TargetType.IGNORE_OTHER_SOURCES);
		summonedEnchantment.spell = AddEnchantmentSpell.create(EntityReference.EVENT_TARGET, enchantment);
		desc.put(SpellArg.TRIGGER, summonedEnchantment);
		super.onCast(context, player, desc, source, target);
	}
}
