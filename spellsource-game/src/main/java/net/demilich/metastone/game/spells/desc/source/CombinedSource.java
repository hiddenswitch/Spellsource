package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Combines the results of multiple {@link CardSource}s specified in {@link CardSourceArg#CARD_SOURCES}.
 */
public class CombinedSource extends CardSource {
	public CombinedSource(CardSourceDesc desc) {
		super(desc);
	}

	protected CardList match(GameContext context, Entity source, Player player, CardSource[] cardSources) {
		CardList cards = new CardArrayList();
		if (cardSources != null) {
			for (CardSource cardSource : cardSources) {
				cards.addAll(cardSource.getCards(context, source, player));
			}
		}
		return cards;
	}
}
