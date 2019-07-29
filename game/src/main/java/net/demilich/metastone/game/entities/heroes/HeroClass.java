package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * All the hero classes, including special hero class specifiers, in Spellsource.
 */
public class HeroClass {

	public static final String INHERIT = "INHERIT";
	public static final String SELF = "SELF";
	public static final String OPPONENT = "OPPONENT";
	public static final String ANY = "ANY";
	public static final String BROWN = "BROWN";
	public static final String GREEN = "GREEN";
	public static final String BLUE = "BLUE";
	public static final String GOLD = "GOLD";
	public static final String WHITE = "WHITE";
	public static final String BLACK = "BLACK";
	public static final String SILVER = "SILVER";
	public static final String VIOLET = "VIOLET";
	public static final String RED = "RED";
	public static final String OLIVE = "OLIVE";
	public static final String TEST = "TEST";


	/**
	 * Retrieves the hero card for a specified hero class.
	 *
	 * @param heroClass The hero class
	 * @return A hero card
	 */
	@NotNull
	public static Card getHeroCard(String heroClass) {
		CardList classCards = getClassCards(DeckFormat.getFormat("All")).filtered(card -> card.getHeroClass().equals(heroClass));
		if (classCards.isEmpty()) {
			return CardCatalogue.getCardById("hero_neutral");
		}
		return CardCatalogue.getCardById(classCards.get(0).getHero());
	}


	/**
	 * Gets all the classes (a list of strings) in the card catalogue.
	 *
	 * @param deckFormat
	 * @return
	 */
	@NotNull
	public static List<String> getBaseClasses(DeckFormat deckFormat) {
		return getClassCards(deckFormat).filtered(Card::isCollectible).stream().map(Card::getHeroClass).collect(Collectors.toList());
	}

	/**
	 * Gets a list of cards that define a class (a champion, a color and additional description or key information).
	 *
	 * @param deckFormat
	 * @return
	 */
	@NotNull
	public static CardList getClassCards(DeckFormat deckFormat) {
		return CardCatalogue.query(deckFormat, CardType.CLASS);
	}

	/**
	 * Retrieves a random hero in the specified {@code deckFormat}
	 *
	 * @param deckFormat
	 * @return
	 */
	public static String random(DeckFormat deckFormat) {
		List<String> baseHeroes = getBaseClasses(deckFormat);
		return baseHeroes.get(RandomUtils.nextInt(0, baseHeroes.size()));
	}
}
