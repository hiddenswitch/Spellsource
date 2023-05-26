package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Prompts the player to guess which card started in the opponent's deck in order to receive it.
 * <p>
 * Implements Curious Glimmerroot.
 */
public final class GuessCardSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(GuessCardSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);

		// Find all the cards which started in the opponent's deck.
		Map<String, List<Entity>> deckCards = context.getEntities()
				.filter(e -> e.getOwner() == opponent.getId())
				.filter(e -> e.getEntityType() == EntityType.CARD)
				.filter(e -> e.hasAttribute(Attribute.STARTED_IN_DECK))
				.collect(groupingBy(e -> e.getSourceCard().getHeroClass()));

		Set<String> startingDeck = deckCards.values().stream().flatMap(Collection::stream).map(Entity::getSourceCard).map(Card::getCardId).collect(toSet());

		String opponentClass = opponent.getHero().getHeroClass();
		String correctClass;
		final Card correctCard;

		if (deckCards.containsKey(opponentClass)
				&& deckCards.get(opponentClass).size() > 0) {
			correctCard = (Card) context.getLogic().getRandom(deckCards.get(opponentClass));
			correctClass = opponentClass;
		} else {
			logger.debug("onCast {} {}: The opponent's deck does not use any class cards, only choosing neutrals for wrong cards now.", context.getGameId(), source);
			correctCard = (Card) context.getLogic().getRandom(deckCards.get(HeroClass.ANY));
			correctClass = HeroClass.ANY;
		}

		List<Card> others = context.getCardCatalogue().query(new DeckFormat().withCardSets(CardCatalogue.latestImplementedHearthstoneExpansion(), "BASIC", "CLASSIC")/*prefer the latest expansion*/)
				.shuffle(context.getLogic().getRandom())
				.stream()
				.filter(c -> c.getHeroClass().equals(correctClass))
				.filter(c -> !c.getCardId().equals(correctCard.getCardId()))
				.filter(c -> !startingDeck.contains(c.getCardId()))
				.limit(2)
				.collect(Collectors.toList());

		CardList cards = new CardArrayList();
		cards.addCard(correctCard);
		others.forEach(cards::addCard);
		logger.debug("onCast {} {}: {} is correct, {} is wrong", context.getGameId(), source, correctCard, others);

		cards.shuffle(context.getLogic().getRandom());
		DiscoverAction result = SpellUtils.discoverCard(context, player, source, desc, cards);
		String cardId = result.getCard().getCardId();
		if (cardId.equals(correctCard.getCardId())) {
			logger.debug("onCast {} {}: Player {} chose correct card {}", context.getGameId(), source, player, correctCard);
			context.getLogic().receiveCard(player.getId(), context.getCardById(cardId));
		}
	}
}
