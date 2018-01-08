package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public interface HasWeights {
	default int getWeight(Player targetPlayer, Card card) {
		if (card.hasHeroClass(targetPlayer.getHero().getHeroClass())) {
			return 4;
		} else if (card.getHeroClass() == HeroClass.ANY) {
			return 1;
		} else {
			return 0;
		}
	}
}
