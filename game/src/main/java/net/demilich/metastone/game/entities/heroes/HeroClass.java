package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * All the hero classes, including special hero class specifiers, in Spellsource.
 * <p>
 * In ord
 */
public class HeroClass {

	public static final String INHERIT = "INHERIT";
	public static final String SELF = "SELF";
	public static final String OPPONENT = "OPPONENT";
	public static final String ANY = "ANY";

	/*
	 case "BROWN":
	 return CardCatalogue.getCardById("hero_malfurion");
	 case "GREEN":
	 return CardCatalogue.getCardById("hero_rexxar");
	 case "BLUE":
	 return CardCatalogue.getCardById("hero_jaina");
	 case "GOLD":
	 return CardCatalogue.getCardById("hero_uther");
	 case "WHITE":
	 return CardCatalogue.getCardById("hero_anduin");
	 case "BLACK":
	 return CardCatalogue.getCardById("hero_valeera");
	 case "SILVER":
	 return CardCatalogue.getCardById("hero_thrall");
	 case "VIOLET":
	 return CardCatalogue.getCardById("hero_guldan");
	 case "RED":
	 return CardCatalogue.getCardById("hero_garrosh");
	 case "JADE":
	 return CardCatalogue.getCardById("hero_mienzhou");
	 case "ROSE":
	 return CardCatalogue.getCardById("hero_senzaku");
	 case "NAVY":
	 return CardCatalogue.getCardById("hero_baron_aldus");
	 case "LEATHER":
	 return CardCatalogue.getCardById("hero_quarnassio");
	 case "EGGPLANT":
	 return CardCatalogue.getCardById("hero_mephilia");
	 case "ICE":
	 return CardCatalogue.getCardById("hero_darion");
	 case "RUST":
	 return CardCatalogue.getCardById("hero_alder_ravenwald");
	 case "OBSIDIAN":
	 return CardCatalogue.getCardById("hero_nerzhul");
	 case "AMBER":
	 return CardCatalogue.getCardById("hero_zara");
	 case "TOAST":
	 return CardCatalogue.getCardById("hero_warchef_gordo");
	 case "BRASS":
	 return CardCatalogue.getCardById("hero_brass");
	 case "ICECREAM":
	 return CardCatalogue.getCardById("hero_kel_thuzad");
	 case "BLOOD":
	 return CardCatalogue.getCardById("hero_koltira");
	 case "NEONGREEN":
	 return CardCatalogue.getCardById("hero_oth");
	 case "DARKGREEN":
	 return CardCatalogue.getCardById("hero_jikr");
	 case "TEAL":
	 return CardCatalogue.getCardById("hero_lady_vashj_sea_witch");
	 case "PURPLE":
	 return CardCatalogue.getCardById("hero_illidan");
	 case "TIME":
	 return CardCatalogue.getCardById("hero_atropos");
	 */


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


	@NotNull
	public static List<String> getBaseClasses(DeckFormat deckFormat) {
		return getClassCards(deckFormat).filtered(Card::isCollectible).stream().map(Card::getHeroClass).collect(Collectors.toList());
	}

	public static CardList getClassCards(DeckFormat deckFormat) {
		return CardCatalogue.query(deckFormat, CardType.CLASS);
	}


	public static String random(DeckFormat deckFormat) {
		List<String> baseHeroes = getBaseClasses(deckFormat);
		return baseHeroes.get(RandomUtils.nextInt(0, baseHeroes.size()));
	}


}
