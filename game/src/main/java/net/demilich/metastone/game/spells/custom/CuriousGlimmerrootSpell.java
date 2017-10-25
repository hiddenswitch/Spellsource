package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class CuriousGlimmerrootSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		SpellDesc rightChoice = (SpellDesc) desc.get(SpellArg.SPELL_1);
		SpellDesc wrongChoice = (SpellDesc) desc.get(SpellArg.SPELL_2);

		// Find all the cards which started in the opponent's deck.
		Map<HeroClass, List<Entity>> deckCards = context.getEntities()
				.filter(e -> e.getOwner() == opponent.getId())
				.filter(e -> e.getEntityType() == EntityType.CARD)
				.filter(e -> e.hasAttribute(Attribute.STARTED_IN_DECK))
				.collect(groupingBy(e -> e.getSourceCard().getHeroClass()));

		HeroClass opponentClass = opponent.getHero().getHeroClass();
		HeroClass correctClass;
		Card correctCard;

		if (deckCards.containsKey(opponentClass)
				&& deckCards.get(opponentClass).size() > 0) {
			correctCard = (Card) deckCards.get(opponentClass).get(context.getLogic().random(deckCards.get(opponentClass).size()));
			correctClass = opponentClass;
		} else {
			correctCard = (Card) deckCards.get(HeroClass.ANY).get(context.getLogic().random(deckCards.get(HeroClass.ANY).size()));
			correctClass = HeroClass.ANY;
		}

		List<Card> others = CardCatalogue.query(context.getDeckFormat())
				.shuffle(context.getLogic().getRandom())
				.stream()
				.filter(c -> c.getHeroClass() == correctClass)
				.filter(c -> !c.getCardId().equals(correctCard.getCardId()))
				.limit(2)
				.collect(Collectors.toList());

		CardList cards = new CardArrayList();
		cards.addCard(correctCard);
		others.forEach(cards::addCard);

		cards.shuffle(context.getLogic().getRandom());
		DiscoverAction result = SpellUtils.discoverCard(context, player, desc, cards);
		String cardId = result.getCard().getCardId();
		if (cardId.equals(correctCard.getCardId())) {
			SpellUtils.castChildSpell(context, player, rightChoice.addArg(SpellArg.CARD, cardId), source, null);
		} else {
			SpellUtils.castChildSpell(context, player, wrongChoice.addArg(SpellArg.CARD, cardId), source, null);
		}
	}
}
