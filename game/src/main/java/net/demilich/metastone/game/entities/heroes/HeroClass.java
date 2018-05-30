package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;

public enum HeroClass {
	ANY,
	DECK_COLLECTION,
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
	LEATHER,
	SPIRIT,
	SELF,
	OPPONENT,
	INHERIT;

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
			case NAVY:
				return CardCatalogue.getCardById("hero_baron_aldus");
			case LEATHER:
				return CardCatalogue.getCardById("hero_quarnassio");
			default:
				return CardCatalogue.getCardById("hero_neutral");
		}
	}

	public boolean isBaseClass() {
		HeroClass[] nonBaseClasses = {ANY, SELF, DECK_COLLECTION, OPPONENT, INHERIT, SPIRIT};
		for (int i = 0; i < nonBaseClasses.length; i++) {
			if (nonBaseClasses[i] == this) {
				return false;
			}
		}
		return true;
	}
}
