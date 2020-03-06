package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.utils.MathUtils;

/**
 * Returns the top N cards of the deck, where N is defined by the {@link CardSourceArg#VALUE} arg
 *
 */
public class TopCardsOfDeckSource extends DeckSource {

	public TopCardsOfDeckSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		CardList deck = super.match(context, source, player);
		int value = howMany(context, source, player);
		return new CardArrayList(deck.subList(MathUtils.clamp( deck.size() - value, 0, deck.size()), deck.size()));
	}

	protected int howMany(GameContext context, Entity source, Player player) {
		return getDesc().getValue(CardSourceArg.VALUE, context, player, player, source, 3);
	}
}
