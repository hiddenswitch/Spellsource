package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.statistics.Statistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeckAndMulliganGeneratorContext extends DeckGeneratorContext {
	public DeckAndMulliganGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks) {
		super(indexInBitmap, basicTournamentDecks);
	}

	@Override
	public double fitness(Genotype<BitGene> individual, String heroClass) {
		GameDeck gameDeck = deckFromBitGenotype(individual, heroClass);

		if (individual.getChromosome().
				stream().map(gene -> (gene.getBit() ? 1 : 0)).mapToInt(Integer::intValue).sum() != maxCardsPerDeck) {
			return Double.MIN_VALUE;
		}

		List<String> cardIdsToKeepOnMulligan = new ArrayList<>();
		for (int i = 0; i < individual.getChromosome(1).length(); i++) {
			if (individual.getChromosome(1).getGene(i).getAllele()) {
				cardIdsToKeepOnMulligan.add(indexInBitmap.get(i).getCardId());
			}
		}
		PlayRandomWithDefinedMulligans playerBehaviourWithMulligan = new PlayRandomWithDefinedMulligans(cardIdsToKeepOnMulligan);
		playerBehaviourWithMulligan.ownMinionTargetingIsEnabled(false);

		return basicTournamentDecks.stream()
				.map(opposingDeck -> GameContext.simulate(
						Arrays.asList(gameDeck, opposingDeck),
						() -> playerBehaviourWithMulligan,
						() -> enemyBehaviour,
						gamesPerMatch,
						true,
						false,
						null,
						this::handleContext)
				)
				.mapToDouble(res -> res.getPlayer1Stats().getDouble(Statistic.WIN_RATE))
				.average().orElse(Double.MIN_VALUE);
	}
}
