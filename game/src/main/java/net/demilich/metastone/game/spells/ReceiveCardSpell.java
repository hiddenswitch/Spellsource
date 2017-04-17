package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardCollectionImpl;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class ReceiveCardSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		if (cardFilter != null) {
			CardCollection cards = CardCatalogue.query(context.getDeckFormat());
			CardCollection result = new CardCollectionImpl();
			String replacementCard = (String) desc.get(SpellArg.CARD);
			for (Card card : cards) {
				if (cardFilter.matches(context, player, card)) {
					result.addCard(card);
				}
			}
			for (int i = 0; i < count; i++) {
				Card card = null;
				if (!result.isEmpty()) {
					card = result.getRandom();
				} else if (replacementCard != null) {
					card = context.getCardById(replacementCard);
				}
				if (card != null) {
					Card clone = card.getCopy();
					context.getLogic().receiveCard(player.getId(), clone);
				}
			}
		} else {
			for (Card card : SpellUtils.getCards(context, desc)) {
				for (int i = 0; i < count; i++) {
					context.getLogic().receiveCard(player.getId(), card.getCopy());
				}
			}
		}
		
	}

}
