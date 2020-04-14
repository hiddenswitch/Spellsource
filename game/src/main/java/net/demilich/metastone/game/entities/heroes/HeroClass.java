package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
	public static final String CORAL = "CORAL";
	public static final String COPPER = "COPPER";
	public static final String DARKBLUE = "DARKBLUE";
	public static final String BLOOD = "BLOOD";
	public static final String NAVY = "NAVY";
	public static final String CAMO = "CAMO";
	public static final String BLUEGREY = "BLUEGREY";
	public static final String CRIMSON = "CRIMSON";
	public static final String MAGENTA = "MAGENTA";
	public static final String TWILIGHT = "TWILIGHT";
	public static final String CANDY = "CANDY";


	/**
	 * Retrieves the hero card for a specified hero class.
	 *
	 * @param heroClass The hero class
	 * @return A hero card
	 */
	@NotNull
	public static Card getHeroCard(String heroClass) {
		return CardCatalogue.getHeroCard(heroClass);
	}


	/**
	 * Gets all the classes (a list of strings) in the card catalogue.
	 *
	 * @param deckFormat
	 * @return
	 */
	@NotNull
	public static List<String> getBaseClasses(DeckFormat deckFormat) {
		return CardCatalogue.getBaseClasses(deckFormat);
	}

	/**
	 * Gets a list of cards that define a class (a champion, a color and additional description or key information).
	 *
	 * @param deckFormat
	 * @return
	 */
	@NotNull
	public static CardList getClassCards(DeckFormat deckFormat) {
		return CardCatalogue.getClassCards(deckFormat);
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

	/**
	 * Checks if the specified card has the specified hero class, respecting a {@link HeroClass#SELF} and a {@link
	 * HeroClass#OPPONENT} specs.
	 *
	 * @param context
	 * @param player
	 * @param card
	 * @param heroClass
	 * @return
	 */
	public static boolean hasHeroClass(GameContext context, Player player, Card card, String heroClass) {
		if (heroClass.equals(OPPONENT)) {
			heroClass = context.getOpponent(player).getHero().getHeroClass();
		} else if (heroClass.equals(SELF)) {
			heroClass = player.getHero().getHeroClass();
		}

		if (heroClass != null && card.hasHeroClass(heroClass)) {
			return false;
		}

		return true;
	}
}
