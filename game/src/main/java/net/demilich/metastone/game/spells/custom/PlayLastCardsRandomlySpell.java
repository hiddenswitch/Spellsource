package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Replays randomly the last {@link SpellArg#VALUE} cards the caster played.
 * <p>
 * Implements Aspect Champion Gar'Hok.
 */
public final class PlayLastCardsRandomlySpell extends PlayCardsRandomlySpell {

	@Override
	protected CardList getCards(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 5);
		desc = desc.clone();
		// Retrieve the last 5 cards played by this player
		List<Card> cardsPlayed = player.getGraveyard().stream()
				.filter(e -> e.getEntityType() == EntityType.CARD)
				.filter(e -> e.hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK))
				.filter(e -> !e.equals(source))
				.map(Card.class::cast)
				.collect(Collectors.toList());
		List<Card> lastCountCardsPlayed = cardsPlayed.subList(Math.max(0, cardsPlayed.size() - count), cardsPlayed.size());
		return new CardArrayList(lastCountCardsPlayed);
	}
}
