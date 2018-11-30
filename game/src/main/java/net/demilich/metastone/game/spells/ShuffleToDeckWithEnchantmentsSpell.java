package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.util.List;

/**
 * A work in progress spell that implements Immortal Prelate.
 */
public class ShuffleToDeckWithEnchantmentsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target != null) {
			Card card = target.getSourceCard();
			SpellUtils.processKeptEnchantments(context, target, card);
			if (target instanceof Actor) {
				final Actor actor = (Actor) target;
				List<Enchantment> enchantments = actor.getEnchantmentsFromContext(context);
				for (Enchantment enchantment : enchantments) {
					EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
					enchantmentDesc.eventTrigger = enchantment.getTriggers().get(0).getDesc();
					enchantmentDesc.countByValue = enchantment.isCountByValue();
					enchantmentDesc.keepAfterTransform = enchantment.isKeptAfterTransform();
					enchantmentDesc.maxFires = enchantment.getMaxFires();
					enchantmentDesc.spell = enchantment.getSpell();
					enchantmentDesc.oneTurn = enchantment.oneTurnOnly();
					enchantmentDesc.persistentOwner = enchantment.hasPersistentOwner();
					card.addStoredEnchantment(enchantmentDesc);
				}
			}
			context.getLogic().shuffleToDeck(player, card, false, true);
		}


	}
}

