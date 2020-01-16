package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class TestMutators {
	/**
	 * Run the testDeckGeneratorForWinTheGameCardWithOnlyBitSwapMutator, which we know succeeds
	 * but use selectOneOfMutators with the BitSwapMutator embedded in them
	 */
	@Test
	public void umbrellaMutatorContainsOneMutatorTest() {
		int MAX_CARDS_PER_DECK = 4;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 10;
		int POPULATION_SIZE = 20;
		int NUMBER_OF_GENERATIONS = 10;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass().equals(HeroClass.BLUE) || card.getHeroClass().equals(HeroClass.ANY))
						&& card.getCardSet().equals(CardSet.BASIC))
				.collect(toList());

		// Create random decks for the tournament
		for (int i = 0; i < 10; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < MAX_CARDS_PER_DECK; j++) {
				tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
			}
			basicTournamentDecks.add(tournamentDeck);
		}

		// Ensure that we do not begin with "win the game"
		// in any of our original population decks
		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));

		// Set up our tournament playing environment
		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, basicTournamentDecks);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(MAX_CARDS_PER_DECK);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		int winTheGameIndex = indexInBitmap.size() - 1;
		List<Integer> invalidCards = new ArrayList<>(0);
		invalidCards.add(indexInBitmap.size() - 1);

		UmbrellaMutator mutators = new UmbrellaMutator(Collections.singletonList(new BitSwapMutator(1)));

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(MAX_CARDS_PER_DECK, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(mutators, mutators)
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(result.getChromosome().getGene(winTheGameIndex).booleanValue());
	}

	public double matchesDesiredGenotype(Genotype<BitGene> individual) {
		double toReturn = 0.0;
		Chromosome<BitGene> c1 = individual.getChromosome(0);
		Chromosome<BitGene> c2 = individual.getChromosome(1);
		if (c1.getGene(1).booleanValue()) {
			toReturn += 1.0;
		}
		if (!c1.getGene(0).booleanValue()) {
			toReturn += 1.0;
		}
		if (c2.getGene(1).booleanValue()) {
			toReturn += 1.0;
		}
		if (!c2.getGene(0).booleanValue()) {
			toReturn += 1.0;
		}
		return toReturn;
	}

	@Test
	public void umbrellaMutatorContainsMultipleMutatorsTest() {
		int POPULATION_SIZE = 4;
		int NUMBER_OF_GENERATIONS = 10;

		int BIT_LENGTH = 2;

		BitSet bits1 = new BitSet(BIT_LENGTH);
		BitSet bits2 = new BitSet(BIT_LENGTH);

		bits1.flip(0);
		bits2.flip(0);

		Chromosome<BitGene> chromosome1 = BitChromosome.of(bits1, BIT_LENGTH);
		Chromosome<BitGene> chromosome2 = BitChromosome.of(bits2, BIT_LENGTH);

		List<Integer> firstChromosome = Collections.singletonList(0);
		List<Integer> secondChromosome = Collections.singletonList(1);
		List<Mutator> mutatorList = new ArrayList<>();
		mutatorList.add(new BitSwapOnSpecificChromosomesMutator(1, firstChromosome));
		mutatorList.add(new BitSwapOnSpecificChromosomesMutator(1, secondChromosome));

		UmbrellaMutator umbrellaMutator = new UmbrellaMutator(mutatorList);

		Factory<Genotype<BitGene>> factory = new SpecificGenotypesFactory(Collections.singletonList(Genotype.of(chromosome1, chromosome2)));
		Engine<BitGene, Double> engine = Engine.builder((individual) -> matchesDesiredGenotype(individual), factory)
				.alterers(umbrellaMutator)
				.populationSize(POPULATION_SIZE)
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(matchesDesiredGenotype(result) == 4.0);
	}
}
