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
	List<DecisionType> decisionTypeList = new ArrayList<>();
	List<DecisionType> otherDecisionTypeList;

	public DeckAndDecisionGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks, List<DecisionType> decisionTypeList) {
		super(indexInBitmap, basicTournamentDecks);
		this.decisionTypeList = decisionTypeList;
	}

	public DeckAndDecisionGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks, List<DecisionType> decisionTypeList, List<DecisionType> otherDecisionTypeList) {
		this(indexInBitmap, basicTournamentDecks, decisionTypeList);
		this.otherDecisionTypeList = otherDecisionTypeList;
	}

	@Override
	public double fitness(Genotype<BitGene> individual, HeroClass heroClass) {
		GameDeck gameDeck = deckFromBitGenotype(individual, heroClass);

		if (individual.getChromosome().
				stream().map(gene -> (gene.getBit() ? 1 : 0)).mapToInt(Integer::intValue).sum() != maxCardsPerDeck) {
			return Double.MIN_VALUE;
		}

		List<List<String>> cardListForEachDecision = new ArrayList<>();
		for (int i = 0; i < decisionTypeList.size(); i++) {
			List<String> cardListForDecision = new ArrayList<>();
			for (int j = 0; j < individual.getChromosome(1 + i).length(); j++) {
				if (individual.getChromosome(i).getGene(j).booleanValue()) {
					cardListForDecision.add(indexInBitmap.get(j).getCardId());
				}
			}
			cardListForEachDecision.add(cardListForDecision);
		}

		List<DecisionType> otherDecisionsList = new ArrayList<>();
		for (int i = 0; i < otherDecisionTypeList.size(); i++) {
			if (individual.getChromosome(i + decisionTypeList.size() + 1).getGene(0).booleanValue()) {
				otherDecisionsList.add(otherDecisionTypeList.get(i));
			}
		}

		PlayRandomWithoutSelfDamageWithDefinedDecisions playerBehaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypeList, cardListForEachDecision, otherDecisionsList);

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
