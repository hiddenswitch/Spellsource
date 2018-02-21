package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;

import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

public class GraveyardCardsSource extends CardSource {

	public GraveyardCardsSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		return player
				.getGraveyard()
				.stream()
				.filter(e -> e instanceof Card)
				.map(e -> (Card)e)
				.filter(Objects::nonNull)
				.collect(toCollection(CardArrayList::new));
	}
}

