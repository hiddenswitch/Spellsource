package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.HasCard;

import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

public class GraveyardActorsSource extends CardSource implements HasCardCreationSideEffects {

	public GraveyardActorsSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		return player
				.getGraveyard()
				.stream()
				.filter(e -> e instanceof Actor)
				.map(HasCard::getSourceCard)
				.filter(Objects::nonNull)
				.collect(toCollection(CardArrayList::new))
				.getCopy();
	}
}
