package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;

public class ReceiveCardSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		CardList cards = CardCatalogue.query(context.getDeckFormat());
		if (cardSource != null) {
			cards = cardSource.getCards(context, player).getCopy();
		}
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		// If a card is being received from a filter, we're creating new cards
		if (cardFilter != null
				|| cardSource != null) {
			CardList result = new CardArrayList();

			if (cardFilter != null) {
				result = cards.filtered(c -> cardFilter.matches(context, player, c, source));
			}

			String replacementCard = (String) desc.get(SpellArg.CARD);
			for (int i = 0; i < count; i++) {
				Card card = null;
				if (!result.isEmpty()) {
					card = result.getRandom();
					result.remove(card);
				} else if (replacementCard != null) {
					card = context.getCardById(replacementCard);
				}
				if (card != null) {
					context.getLogic().receiveCard(player.getId(), card);
				}
			}
		} else {
			// If a card isn't received from a filter, it's coming from a description
			for (Card card : SpellUtils.getCards(context, desc)) {
				// Move at most one card from discover or create a card. Handled by get cards.
				context.getLogic().receiveCard(player.getId(), card, count);
			}
		}
	}

}
