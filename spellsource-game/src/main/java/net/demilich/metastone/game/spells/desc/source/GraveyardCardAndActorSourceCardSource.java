package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.jetbrains.annotations.NotNull;

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
		return graveyardCards(context, player);
	}

	/**
	 * Gets the cards that properly belong in the player's {@link Zones#GRAVEYARD}, meaning they were played by the player
	 * or they are the card representing a token or minion that died (not peacefully) on the battlefield.
	 * <p>
	 * This means when a minion is played, this method will not return the card from which the minion was played until the
	 * minion is destroyed. However, there is still a card with the minion's card ID in the graveyard as soon as it is
	 * played. This method can be used to tell the difference between a card corresponding to a played minion (which is
	 * always in the graveyard) versus a card of a minion that has died.
	 *
	 * @param context
	 * @param player
	 * @return
	 */
	@NotNull
	public static CardList graveyardCards(GameContext context, Player player) {
		Set<Card> inPlay = context.getEntities()
				.filter(Entity::isInPlay)
				.map(Entity::getSourceCard)
				.collect(Collectors.toUnmodifiableSet());
		Set<Card> removedPeacefully = context.getEntities().filter(e -> GameLogic.isEntityType(e.getEntityType(), EntityType.ACTOR)
				&& e.isRemovedPeacefully()).map(Entity::getSourceCard).collect(Collectors.toUnmodifiableSet());
		return player.getGraveyard().stream()
				.map(entity -> {
					Card card;
					if (entity instanceof Card) {
						card = (Card) entity;
					} else if (entity.diedOnBattlefield()) {
						// Don't count entities that were removed peacefully
						if (entity.getSourceCard() == null || entity.isRemovedPeacefully()) {
							card = null;
						} else if (entity.getSourceCard().getZone() != Zones.GRAVEYARD) {
							// Token or played from card that is not in the graveyard
							card = entity.getSourceCard().getCopy();
						} else if (entity.getSourceCard().getZone() == Zones.GRAVEYARD) {
							card = entity.getSourceCard();
						} else {
							card = null;
						}
					} else {
						card = null;
					}
					return card;
				})
				// Remove duplicates by entityId / reference
				.filter(Objects::nonNull)
				.distinct()
				// Omit the backing cards of in-play entities and entities that were removed peacefully
				.filter(card -> !inPlay.contains(card) && !removedPeacefully.contains(card))
				.collect(Collectors.toCollection(CardArrayList::new));
	}
}
