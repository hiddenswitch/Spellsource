package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.utils.Attribute;

import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

public class GraveyardActorsSource extends CardSource implements HasCardCreationSideEffects {

	public GraveyardActorsSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player
				.getGraveyard()
				.stream()
				.filter(e -> e instanceof Actor)
				.map(actor -> {
					Card sourceCard = actor.getSourceCard();
					if (sourceCard == null) {
						return null;
					}
					// Include the turn this card died
					sourceCard.getAttributes().put(Attribute.DIED_ON_TURN, source.getAttributeValue(Attribute.DIED_ON_TURN));
					return sourceCard;
				})
				.filter(Objects::nonNull)
				.collect(toCollection(CardArrayList::new))
				.getCopy();
	}
}
