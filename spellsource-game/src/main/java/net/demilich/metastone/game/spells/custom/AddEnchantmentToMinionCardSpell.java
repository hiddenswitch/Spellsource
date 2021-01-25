package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddEnchantmentSpell;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts the enchantment written in this spell into play as soon as the minion is summoned from the {@code target} minion
 * card.
 * <p>
 * This is a way to have minion cards carry enchantments into the battlefield when they are summoned.
 * <p>
 * Enchantments written this way <b>do</b> get copied with the card.
 *
 * @see net.demilich.metastone.game.spells.AddBattlecrySpell for putting spell-like text onto cards.
 */
public final class AddEnchantmentToMinionCardSpell extends AddEnchantmentSpell {
	private static Logger LOGGER = LoggerFactory.getLogger(AddEnchantmentToMinionCardSpell.class);

	public static SpellDesc create(Card card, EnchantmentDesc enchantmentDesc) {
		SpellDesc desc = new SpellDesc(AddEnchantmentToMinionCardSpell.class);
		desc.put(SpellArg.TRIGGER, enchantmentDesc);
		desc.put(SpellArg.TARGET, card.getReference());
		return desc;
	}

	public static SpellDesc create(Card card, Aura aura) {
		SpellDesc desc = new SpellDesc(AddEnchantmentToMinionCardSpell.class);
		desc.put(SpellArg.AURA, aura.clone());
		desc.put(SpellArg.TARGET, card.getReference());
		return desc;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc oldDesc, Entity source, Entity target) {
		throw new UnsupportedOperationException();
		/*
		SpellDesc desc = new SpellDesc(AddEnchantmentSpell.class);
		EnchantmentDesc summonedEnchantment = new EnchantmentDesc();
		summonedEnchantment.setEventTrigger(BeforeMinionSummonedTrigger.create());
		summonedEnchantment.getEventTrigger().put(EventTriggerArg.HOST_TARGET_TYPE, TargetType.IGNORE_OTHER_SOURCES);
		if (oldDesc.containsKey(SpellArg.TRIGGER)) {
			summonedEnchantment.setSpell(AddEnchantmentSpell.create(EntityReference.EVENT_TARGET, (EnchantmentDesc) oldDesc.get(SpellArg.TRIGGER)));
		} else if (oldDesc.containsKey(SpellArg.AURA)) {
			summonedEnchantment.setSpell(AddEnchantmentSpell.create(EntityReference.EVENT_TARGET, (Aura) oldDesc.get(SpellArg.AURA)));
		} else {
			LOGGER.error("onCast {} {}: Missing a trigger or an aura!", context.getGameId(), source);
		}
		desc.put(SpellArg.TRIGGER, summonedEnchantment);
		super.onCast(context, player, desc, source, target);*/
	}
}
