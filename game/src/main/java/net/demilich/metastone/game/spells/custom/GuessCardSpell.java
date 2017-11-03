package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class GuessCardSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);

		// Find all the cards which started in the opponent's deck.
		Map<HeroClass, List<Entity>> deckCards = context.getEntities()
				.filter(e -> e.getOwner() == opponent.getId())
				.filter(e -> e.getEntityType() == EntityType.CARD)
				.filter(e -> e.hasAttribute(Attribute.STARTED_IN_DECK))
				.collect(groupingBy(e -> e.getSourceCard().getHeroClass()));

		Set<String> startingDeck = deckCards.values().stream().flatMap(Collection::stream).map(Entity::getSourceCard).map(Card::getCardId).collect(toSet());

		HeroClass opponentClass = opponent.getHero().getHeroClass();
		HeroClass correctClass;
		final Card correctCard;

		if (deckCards.containsKey(opponentClass)
				&& deckCards.get(opponentClass).size() > 0) {
			correctCard = (Card) deckCards.get(opponentClass).get(context.getLogic().random(deckCards.get(opponentClass).size()));
			correctClass = opponentClass;
		} else {
			correctCard = (Card) deckCards.get(HeroClass.ANY).get(context.getLogic().random(deckCards.get(HeroClass.ANY).size()));
			correctClass = HeroClass.ANY;
		}

		List<Card> others = CardCatalogue.query(new DeckFormat().withCardSets(CardSet.latest(), CardSet.BASIC, CardSet.CLASSIC)/*prefer the latest expansion*/)
				.shuffle(context.getLogic().getRandom())
				.stream()
				.filter(c -> c.getHeroClass() == correctClass)
				.filter(c -> !c.getCardId().equals(correctCard.getCardId()))
				.filter(c -> !startingDeck.contains(c.getCardId()))
				.limit(2)
				.collect(Collectors.toList());

		CardList cards = new CardArrayList();
		cards.addCard(correctCard);
		others.forEach(cards::addCard);

		cards.shuffle(context.getLogic().getRandom());
		DiscoverAction result = SpellUtils.discoverCard(context, player, desc, cards);
		String cardId = result.getCard().getCardId();
		if (cardId.equals(correctCard.getCardId())) {
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById(cardId));
		}
	}
}
