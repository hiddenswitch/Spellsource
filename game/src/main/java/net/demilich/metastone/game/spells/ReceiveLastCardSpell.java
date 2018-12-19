package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.desc.SpellArg;

import java.util.List;

/**
 * Retrieves the last card from the filtered list of cards specified by {@link SpellArg#CARD_SOURCE} and {@link
 * SpellArg#CARD_FILTER}.
 *
 * @see ReceiveCardSpell for a more general spell.
 */
public final class ReceiveLastCardSpell extends ReceiveCardSpell {

	@Override
	protected Card getAndRemoveCard(GameContext context, List<Card> cards) {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(cards.size() - 1);
	}
}
