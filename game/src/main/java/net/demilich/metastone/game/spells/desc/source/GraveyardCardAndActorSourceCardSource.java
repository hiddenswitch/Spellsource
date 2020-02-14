package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.targeting.Zones;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Returns a list of cards in the player's graveyard, including minion's cards.
 */
public class GraveyardCardAndActorSourceCardSource extends CardSource {

	public GraveyardCardAndActorSourceCardSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		Set<Card> inPlay = context.getEntities().filter(Entity::isInPlay).map(Entity::getSourceCard).collect(Collectors.toUnmodifiableSet());
		return player.getGraveyard().stream()
				.map(entity -> {
					Card card;
					if (entity instanceof Card) {
						card = (Card) entity;
					} else {
						if (entity.getSourceCard() == null) {
							card = null;
						} else if (entity.getSourceCard().getZone() != Zones.GRAVEYARD) {
							// Token or played from card that is not in the graveyard
							card = entity.getSourceCard().getCopy();
						} else if (entity.getSourceCard().getZone() == Zones.GRAVEYARD) {
							card = entity.getSourceCard();
						} else {
							card = null;
						}
					}
					return card;
				})
				// Remove duplicates by entityId / reference
				.filter(Objects::nonNull)
				.distinct()
				// Omit the backing cards of in-play entities
				.filter(card -> !inPlay.contains(card))
				.collect(Collectors.toCollection(CardArrayList::new));
	}
}
