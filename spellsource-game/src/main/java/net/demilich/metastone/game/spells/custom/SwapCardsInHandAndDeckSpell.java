package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SwapCardsSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Swaps a card from the hand filtered using the first item in the {@link SpellArg#CARD_FILTERS}
 * with a card in the deck filtered with the second filter in the card filters array.
 * <p>
 * Does up to {@link SpellArg#VALUE} swaps.
 * <p>
 * If a card in the hand or deck is not found matching the respective filter, no swap occurs.
 */
public final class SwapCardsInHandAndDeckSpell extends SwapCardsSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter[] filters = (EntityFilter[]) desc.get(SpellArg.CARD_FILTERS);
		if (filters == null || filters.length != 2) {
			throw new UnsupportedOperationException("SwapCardsInHandAndDeckSpell requires filters");
		}

		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < count; i++) {
			CardList filteredHand = player.getHand().filtered(filters[0].matcher(context, player, source));
			CardList filteredDeck = player.getDeck().filtered(filters[1].matcher(context, player, source));
			if (filteredDeck.isEmpty() || filteredHand.isEmpty()) {
				break;
			}
			Card handCard = context.getLogic().getRandom(filteredHand);
			Card deckCard = context.getLogic().getRandom(filteredDeck);

			SpellDesc swapSpell = new SpellDesc(SwapCardsSpell.class);
			swapSpell.put(SpellArg.SECONDARY_TARGET, deckCard.getReference());
			super.onCast(context, player, swapSpell, source, handCard);
		}
	}
}
