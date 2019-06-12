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

public class DeckAndDecisionGeneratorContext extends DeckGeneratorContext {
	List<DecisionType> cardListDecisionTypes;
	List<DecisionType> booleanDecisionTypes;

	public DeckAndDecisionGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks, List<DecisionType> cardListDecisionTypes) {
		super(indexInBitmap, basicTournamentDecks);
		this.cardListDecisionTypes = cardListDecisionTypes;
	}

	public DeckAndDecisionGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks, List<DecisionType> cardListDecisionTypes, List<DecisionType> booleanDecisionTypes) {
		this(indexInBitmap, basicTournamentDecks, cardListDecisionTypes);
		this.booleanDecisionTypes = booleanDecisionTypes;
	}

	@Override
	public double fitness(Genotype<BitGene> individual, HeroClass heroClass) {
		GameDeck gameDeck = deckFromBitGenotype(individual, heroClass);

		if (individual.getChromosome().
				stream().map(gene -> (gene.getBit() ? 1 : 0)).mapToInt(Integer::intValue).sum() != maxCardsPerDeck) {
			return Double.MIN_VALUE;
		}

		List<List<String>> cardListForEachDecision = new ArrayList<>();
		for (int i = 0; i < cardListDecisionTypes.size(); i++) {
			List<String> cardListForDecision = new ArrayList<>();
			for (int j = 0; j < individual.getChromosome(1 + i).length(); j++) {
				if (individual.getChromosome(1 + i).getGene(j).booleanValue()) {
					cardListForDecision.add(indexInBitmap.get(j).getCardId());
				}
			}
			cardListForEachDecision.add(cardListForDecision);
		}

		List<DecisionType> otherDecisionsList = new ArrayList<>();
		for (int i = 0; i < booleanDecisionTypes.size(); i++) {
			if (individual.getChromosome(i + cardListDecisionTypes.size() + 1).getGene(0).booleanValue()) {
				otherDecisionsList.add(booleanDecisionTypes.get(i));
			}
		}

		PlayRandomWithoutSelfDamageWithDefinedDecisions playerBehaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(cardListDecisionTypes, cardListForEachDecision, otherDecisionsList);

		return basicTournamentDecks.stream()
				.map(opposingDeck -> GameContext.simulate(
						Arrays.asList(gameDeck, opposingDeck),
						() -> playerBehaviour,
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
