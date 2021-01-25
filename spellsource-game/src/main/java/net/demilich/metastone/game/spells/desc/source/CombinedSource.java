package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Combines multiple card sources together and includes cards from all of them.
 */
public class CombinedSource extends CardSource {
	public CombinedSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		CardSource[] cardSources = (CardSource[]) getDesc().get(CardSourceArg.CARD_SOURCES);
		CardList totalCards = new CardArrayList();
		for (CardSource cardSource : cardSources) {
			totalCards.addAll(cardSource.getCards(context, source, player));
		}
		return totalCards;
	}
}
