package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class TestDeckGenerationOptimization {

	/**
	 * Tests if the genetic algorithm can find cards that are objectively better
	 * than other similar cards (in this case, a 30 damage fireball spell with variable mana cost)
	 * We expect the finished deck to be the one-cost (minimal cost) version of the spell.
	 */
	@Test
	public void differentCostBurnSpellsTest() {
		int maxCardsPerDeck = 1;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		long NUMBER_OF_GENERATIONS = 10;

		CardCatalogue.loadCardsFromPackage();

		List<Card> indexInBitmap = new ArrayList<>();
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_1"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_2"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_3"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_4"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_5"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_6"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_7"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_8"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_9"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_shock_10"));

		List<GameDeck> basicTournamentDecks = new ArrayList<>();

		// Create unique decks for each of the mana cost versions of the same card
		for (int i = 0; i < indexInBitmap.size(); i++) {
			basicTournamentDecks.add(new GameDeck(HeroClass.BLUE, Collections.singletonList(indexInBitmap.get(i).getCardId())));
		}

		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, basicTournamentDecks);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(maxCardsPerDeck);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);
		deckGeneratorContext.setEnemyBehaviour(new PlayRandomWithoutSelfDamageBehaviour());
		deckGeneratorContext.setPlayerBehaviour(new PlayRandomWithoutSelfDamageBehaviour());

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size());
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1), new BitSwapBetweenTwoSequencesMutator<>(1))
				.build();

		EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();

		Phenotype<BitGene, Double> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.peek(statistics)
				.collect(EvolutionResult.toBestPhenotype());

		int bestCardIndex = 0;
		assertTrue(result.getGenotype().getChromosome().getGene(bestCardIndex).booleanValue());
	}
}
