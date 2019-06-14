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

	/**
	 * @param indexInBitmap         The list of cards such that the i'th card is represented by the
	 *                              i'th digit in the deck chromosome
	 * @param basicTournamentDecks  The list of tournament decks that will be the opponents in
	 *                              the fitness tournament
	 * @param cardListDecisionTypes The list of decision types that will also evolve alongside the deck
	 */
	public DeckAndDecisionGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks, List<DecisionType> cardListDecisionTypes) {
		super(indexInBitmap, basicTournamentDecks);
		this.cardListDecisionTypes = cardListDecisionTypes;
	}

	/**
	 * @param indexInBitmap         The list of cards such that the i'th card is represented by the
	 *                              i'th digit in the deck chromosome
	 * @param basicTournamentDecks  The list of tournament decks that will be the opponents in
	 *                              the fitness tournament
	 * @param cardListDecisionTypes The list of decision types that will also evolve alongside the deck
	 * @param booleanDecisionTypes  The list of decision types that also evolve but are booleans and
	 *                              do not directly correspond to indexInBitmap
	 */
	public DeckAndDecisionGeneratorContext(List<Card> indexInBitmap, List<GameDeck> basicTournamentDecks, List<DecisionType> cardListDecisionTypes, List<DecisionType> booleanDecisionTypes) {
		this(indexInBitmap, basicTournamentDecks, cardListDecisionTypes);
		this.booleanDecisionTypes = booleanDecisionTypes;
	}

	/**
	 * @param individual The Genotype of a BitChromosome containing the deck information
	 * @param heroClass  The {@link HeroClass} of the deck we are testing
	 * @return the win percentage of the genotype, ranging from 0.0 to 1.0
	 */
	@Override
	public double fitness(Genotype<BitGene> individual, HeroClass heroClass) {
		GameDeck gameDeck = deckFromBitGenotype(individual, heroClass);

		if (individual.getChromosome().
				stream().map(gene -> (gene.getBit() ? 1 : 0)).mapToInt(Integer::intValue).sum() != maxCardsPerDeck) {
			return Double.MIN_VALUE;
		}

		// Create the list of cards associated with each decisionType in order
		// to create the behaviour to be utilized
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

		// Determine which boolean decisionTypes should be utilized by the
		// player behaviour
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
