package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

/**
 * All the hero classes, including special hero class specifiers, in Spellsource.
 * <p>
 * In ord
 */
public enum HeroClass {
	/**
	 * A special value indicating neutral.
	 */
	ANY,
	BROWN,
	GREEN,
	BLUE,
	NAVY,
	GOLD,
	WHITE,
	BLACK,
	SILVER,
	VIOLET,
	RED,
	JADE,
	ROSE,
	LEATHER,
	EGGPLANT,
	RUST,
	ICE,
	OBSIDIAN,
	AMBER,
	TOAST,
	BRASS,
	ICECREAM,
	BLOOD,
	NEONGREEN,
	DARKGREEN,
	PURPLE,
	TIME,
	TEAL,
    /**
	 * The Hearthstone Death Knight card class.
	 */
	SPIRIT,
	/**
	 * A special value indicating the caster's hero class.
	 */
	SELF,
	/**
	 * A special value indicating the caster's opponent's hero class.
	 */
	OPPONENT,
	/**
	 * A special value indicating that when a hero is changed to this hero, this hero inherits the prior hero's hero
	 * class.
	 */
	INHERIT;

	/**
	 * Retrieves the hero card for a specified hero class.
	 *
	 * @param heroClass The hero class
	 * @return A hero card
	 */
	public static Card getHeroCard(HeroClass heroClass) {
		switch (heroClass) {
			case BROWN:
				return CardCatalogue.getCardById("hero_malfurion");
			case GREEN:
				return CardCatalogue.getCardById("hero_rexxar");
			case BLUE:
				return CardCatalogue.getCardById("hero_jaina");
			case GOLD:
				return CardCatalogue.getCardById("hero_uther");
			case WHITE:
				return CardCatalogue.getCardById("hero_anduin");
			case BLACK:
				return CardCatalogue.getCardById("hero_valeera");
			case SILVER:
				return CardCatalogue.getCardById("hero_thrall");
			case VIOLET:
				return CardCatalogue.getCardById("hero_guldan");
			case RED:
				return CardCatalogue.getCardById("hero_garrosh");
			case JADE:
				return CardCatalogue.getCardById("hero_chen_stormstout");
			case ROSE:
				return CardCatalogue.getCardById("hero_witch_doctor");
			case NAVY:
				return CardCatalogue.getCardById("hero_baron_aldus");
			case LEATHER:
				return CardCatalogue.getCardById("hero_quarnassio");
			case EGGPLANT:
				return CardCatalogue.getCardById("hero_mephilia");
			case ICE:
				return CardCatalogue.getCardById("hero_darion");
			case RUST:
				return CardCatalogue.getCardById("hero_alder_ravenwald");
			case OBSIDIAN:
				return CardCatalogue.getCardById("hero_nerzhul");
			case AMBER:
				return CardCatalogue.getCardById("hero_zara");
			case TOAST:
				return CardCatalogue.getCardById("hero_warchef_gordo");
			case BRASS:
				return CardCatalogue.getCardById("hero_brass");
			case ICECREAM:
				return CardCatalogue.getCardById("hero_kel_thuzad");
			case BLOOD:
				return CardCatalogue.getCardById("hero_koltira");
			case NEONGREEN:
				return CardCatalogue.getCardById("hero_oth");
			case DARKGREEN:
				return CardCatalogue.getCardById("hero_jikr");
			case TEAL:
				return CardCatalogue.getCardById("hero_lady_vashj_sea_witch");
			case PURPLE:
				return CardCatalogue.getCardById("hero_illidan");
			case TIME:
				return CardCatalogue.getCardById("hero_atropos");
			default:
				return CardCatalogue.getCardById("hero_neutral");
		}
	}

	@NotNull
	public static List<HeroClass> getBaseClasses() {
		return Arrays.stream(values()).filter(HeroClass::isBaseClass).collect(toList());
	}

	public static HeroClass random() {
		List<HeroClass> baseHeroes = getBaseClasses();
		return baseHeroes.get(RandomUtils.nextInt(0, baseHeroes.size()));
	}

	public boolean isBaseClass() {
		HeroClass[] nonBaseClasses = {ANY, BRASS, SELF, OPPONENT, INHERIT, SPIRIT};
		for (int i = 0; i < nonBaseClasses.length; i++) {
			if (nonBaseClasses[i] == this) {
				return false;
			}
		}
		return true;
	}
}
