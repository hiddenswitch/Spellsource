package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Roasting a card removes the card from the top of the deck and adds the {@link Attribute#ROASTED} to it. Always
 * reveals the card. Roasts {@link SpellArg#VALUE} cards.
 */
public final class RoastSpell extends DiscardSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < value; i++) {
			// Use the TARGET_PLAYER to indicate whose card should be roasted.
			if (desc.containsKey(SpellArg.CARD_FILTER) && !player.getDeck().isEmpty()) {
				EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
				target = context.getLogic().getRandom(player.getDeck().filtered(cardFilter.matcher(context, player, source)));
			} else if (!desc.containsKey(SpellArg.TARGET)) {
				target = player.getDeck().peek();
			}

			if (target == null) {
				return;
			}

			context.getLogic().discardCard(player, (Card) target);
		}
	}
}
