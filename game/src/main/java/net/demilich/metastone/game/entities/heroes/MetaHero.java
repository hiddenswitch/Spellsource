package net.demilich.metastone.game.entities.heroes;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.HeroCardDesc;

public class MetaHero extends HeroCard {

	private static HeroCardDesc createDesc() {
		HeroCardDesc desc = new HeroCardDesc();
		desc.collectible = false;
		desc.heroClass = HeroClass.DECK_COLLECTION;
		desc.name = "Depending on deck";
		desc.id = "hero_meta";
		desc.rarity = Rarity.FREE;
		return desc;
	}

	public static HeroCard getHeroCard(HeroClass heroClass) {
		switch (heroClass) {
		case BROWN:
			return (HeroCard) CardCatalogue.getCardById("hero_malfurion");
		case GREEN:
			return (HeroCard) CardCatalogue.getCardById("hero_rexxar");
		case BLUE:
			return (HeroCard) CardCatalogue.getCardById("hero_jaina");
		case GOLD:
			return (HeroCard) CardCatalogue.getCardById("hero_uther");
		case WHITE:
			return (HeroCard) CardCatalogue.getCardById("hero_anduin");
		case BLACK:
			return (HeroCard) CardCatalogue.getCardById("hero_valeera");
		case SILVER:
			return (HeroCard) CardCatalogue.getCardById("hero_thrall");
		case VIOLET:
			return (HeroCard) CardCatalogue.getCardById("hero_guldan");
		case RED:
			return (HeroCard) CardCatalogue.getCardById("hero_garrosh");
		default:
			break;
		}
		return null;
	}
	
	public MetaHero() {
		super(createDesc());
	}

}
