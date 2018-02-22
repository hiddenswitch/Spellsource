package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.stream.Collectors;

public class UngoroPackRarityWeightedCardSource extends CatalogueSource {

	public UngoroPackRarityWeightedCardSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return CardCatalogue
				.query(context.getDeckFormat()).stream()
				.filter(c -> c.getCardSet() == CardSet.JOURNEY_TO_UNGORO)
				.collect(Collectors.toCollection(CardArrayList::new));
	}

	/**
	 * Gets the weight for the Un'Goro pack that Elise the Trailblazer's token gives you.
	 * <p>
	 * The values here are very eyeballed.
	 *
	 * @param targetPlayer The player to calculate this with respect to.
	 * @param card         The card.
	 * @return The weight.
	 */
	@Override
	public int getWeight(Player targetPlayer, Card card) {
		boolean isClassCard = card.hasHeroClass(targetPlayer.getHero().getHeroClass());
		int isSameClassMultiplier = isClassCard ? 4 : 1;
		boolean isNeutral = card.hasHeroClass(HeroClass.ANY);

		if (!isClassCard && !isNeutral) {
			return 0;
		}

		switch (card.getRarity()) {
			case LEGENDARY:
			case EPIC:
				return isSameClassMultiplier;
			default:
			case RARE:
			case FREE:
			case COMMON:
				return 4 * isSameClassMultiplier;
		}
	}
}
