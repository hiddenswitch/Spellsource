package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.util.ArrayList;

/**
 * Resets the {@code target} card's cost to its original (base) mana cost by expiring all {@link CardCostModifier}
 * enchantments hosted by the target card.
 * <p>
 * When used with a multi-target like {@code BOTH_HANDS}, this removes per-card cost modifiers from every card in both
 * hands.
 * <p>
 * Implements the effect "Set the Cost of every card back to their original Costs."
 */
public final class ResetCardCostSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Remove card cost modifiers hosted by this specific card
		var triggers = new ArrayList<>(context.getLogic().getActiveTriggers(target.getReference()));
		for (var trigger : triggers) {
			if (trigger instanceof CardCostModifier) {
				((Enchantment) trigger).expire(context);
			}
		}
	}
}
