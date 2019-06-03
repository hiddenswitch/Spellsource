package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
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
	@Test(invocationCount = 14)
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

		List<Integer> invalidCards = new ArrayList<>();
		invalidCards.add(indexInBitmap.size() - 1);
		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1))
				.build();

		EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();

		Phenotype<BitGene, Double> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.peek(statistics)
				.collect(EvolutionResult.toBestPhenotype());

		int bestCardIndex = 0;
		assertTrue(result.getGenotype().getChromosome().getGene(bestCardIndex).booleanValue());
	}

	/**
	 * Tests if the genetic algorithm can find cards that are objectively better
	 * than other similar cards (in this case, a 4 cost fireball spell with variable damage cost)
	 * We expect the finished deck to be the maximal damage version of the spell.
	 */
	@Test
	public void differentDamageBurnSpellsTest() {
		int maxCardsPerDeck = 1;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		long NUMBER_OF_GENERATIONS = 10;

		CardCatalogue.loadCardsFromPackage();

		List<Card> indexInBitmap = new ArrayList<>();
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_1"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_3"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_6"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_10"));

		List<GameDeck> basicTournamentDecks = new ArrayList<>();

		// Create unique decks for each of the different damage versions of the card
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

		int bestCardIndex = indexInBitmap.size() - 1;
		assertTrue(result.getGenotype().getChromosome().getGene(bestCardIndex).booleanValue());
	}

	// With completely random play, we expect to have not nearly as good results
	@Test
	public void differentDamageBurnSpellsTestWithRandomPlay() {
		int maxCardsPerDeck = 1;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int TRIALS = 10;
		long NUMBER_OF_GENERATIONS = 5;

		CardCatalogue.loadCardsFromPackage();

		List<Card> indexInBitmap = new ArrayList<>();
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_1"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_3"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_6"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_fireball_10"));

		List<GameDeck> basicTournamentDecks = new ArrayList<>();

		for (int i = 0; i < indexInBitmap.size(); i++) {
			basicTournamentDecks.add(new GameDeck(HeroClass.BLUE, Collections.singletonList(indexInBitmap.get(i).getCardId())));
		}

		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, basicTournamentDecks);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(maxCardsPerDeck);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);
		deckGeneratorContext.setEnemyBehaviour(new PlayRandomBehaviour());
		deckGeneratorContext.setPlayerBehaviour(new PlayRandomBehaviour());

		// Keep track of how many times we succeed (presumed to be by pure chance)
		int succeeds = 0;
		for (int i = 0; i < 10; i++) {
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

			int bestCardIndex = indexInBitmap.size() - 1;
			if (result.getGenotype().getChromosome().getGene(bestCardIndex).booleanValue()) {
				succeeds++;
			}
		}

		// If
		assertTrue(succeeds <= 2 * TRIALS / indexInBitmap.size());
	}
}
