package com.hiddenswitch.deckgeneration;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
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

public class TestMulliganOptimization {

	// Creates a test that shows that the genetic algorithm result for mulligans will
	// always keep the "win the game" card in a deck kept constant through generations
	@Test
	public void willNotMulliganWinTheGameTest() {
		int GAMES_PER_MATCH = 50;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int NUMBER_OF_GENERATIONS = 50;
		int CARDS_IN_DECK = 30;

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass().equals(HeroClass.BLUE) || card.getHeroClass().equals(HeroClass.ANY))
						&& card.getCardSet().equals(CardSet.BASIC)).limit(CARDS_IN_DECK - 1)
				.collect(toList());

		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));

		GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE, indexInBitmap.stream().map(card -> card.getCardId()).collect(toList()));
		basicTournamentDecks.add(tournamentDeck);

		DeckAndMulliganGeneratorContext deckAndMulliganGeneratorContext = new DeckAndMulliganGeneratorContext(indexInBitmap, basicTournamentDecks);
		PlayRandomWithoutSelfDamageBehaviour enemyBehvaiour = new PlayRandomWithoutSelfDamageBehaviour();
		enemyBehvaiour.ownMinionTargetingIsEnabled(false);
		deckAndMulliganGeneratorContext.setEnemyBehaviour(enemyBehvaiour);
		deckAndMulliganGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);
		deckAndMulliganGeneratorContext.setStartingHp(STARTING_HP);
		deckAndMulliganGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		int winTheGameIndex = indexInBitmap.size() - 1;

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckAndMulliganGeneFactory(CARDS_IN_DECK, CARDS_IN_DECK);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckAndMulliganGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(new ActsOnSpecificChromosomesBasicMutator<>(1, chromosomesToActOn), new SinglePointCrossover<>(1))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());


		assertTrue(result.getChromosome(1).getGene(winTheGameIndex).booleanValue());
	}

	// Tests that the specific genotype factory generates only the specified genotype
	@Test
	public void specificGenotypesFactoryTest() {
		int GAMES_PER_MATCH = 50;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int CARDS_IN_DECK = 30;
		int NUMBER_OF_GENERATIONS = 10;

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass().equals(HeroClass.BLUE) || card.getHeroClass().equals(HeroClass.ANY))
						&& card.getCardSet().equals(CardSet.BASIC)).limit(CARDS_IN_DECK - 1)
				.collect(toList());

		GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE, Collections.singletonList(indexInBitmap.get(0).getCardId()));
		basicTournamentDecks.add(tournamentDeck);

		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));
		int bestCardIndex = indexInBitmap.size() - 1;

		BitSet bits = new BitSet(indexInBitmap.size());
		bits.flip(bestCardIndex);
		Chromosome<BitGene> chromosome = BitChromosome.of(bits, indexInBitmap.size());
		Genotype<BitGene> genotype = Genotype.of(chromosome);

		// Set up our tournament playing environment
		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, basicTournamentDecks);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		Factory<Genotype<BitGene>> specificFactory = new SpecificGenotypesFactory(Collections.singletonList(genotype));
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), specificFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(new SwapMutator(0))
				.build();

		Genotype<BitGene> result = engine.stream().limit(NUMBER_OF_GENERATIONS).collect(EvolutionResult.toBestGenotype());
		assertTrue(result.getChromosome().getGene(bestCardIndex).booleanValue());
	}

	// Creates a test that show that a genetic algorithm can both find the
	// win the game card and decide not to mulligan it if drawn
	@Test(invocationCount = 10)
	public void willFindAndNotMulliganWinTheGameTest() {
		int GAMES_PER_MATCH_FOR_DECK_GENERATION = 10;
		int GAMES_PER_MATCH_FOR_MULLIGAN_GENERATION = 60;
		int NUMBER_OF_DECKS = 10;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int CARDS_IN_DECK = 20;
		int STABLE_GENERATIONS = 10;

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
		for (int i = 0; i < NUMBER_OF_DECKS; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < CARDS_IN_DECK; j++) {
				tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
			}
			basicTournamentDecks.add(tournamentDeck);
		}

		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));

		int winTheGameIndex = indexInBitmap.size() - 1;

		List<Integer> invalidCards = new ArrayList<>();
		invalidCards.add(winTheGameIndex);

		DeckAndMulliganGeneratorContext deckAndMulliganGeneratorContext = new DeckAndMulliganGeneratorContext(indexInBitmap, basicTournamentDecks);
		PlayRandomWithoutSelfDamageBehaviour enemyBehaviour = new PlayRandomWithoutSelfDamageBehaviour();
		enemyBehaviour.ownMinionTargetingIsEnabled(false);
		deckAndMulliganGeneratorContext.setEnemyBehaviour(enemyBehaviour);
		deckAndMulliganGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH_FOR_DECK_GENERATION);
		deckAndMulliganGeneratorContext.setStartingHp(STARTING_HP);
		deckAndMulliganGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		List<Integer> deckListChromosomes = Collections.singletonList(0);

		List<Integer> mulliganChromosomes = Collections.singletonList(1);

		Factory<Genotype<BitGene>> deckFactory = new DeckAndMulliganGeneFactory(CARDS_IN_DECK, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> deckEngine = Engine.builder((individual) -> deckAndMulliganGeneratorContext.fitness(individual, HeroClass.BLUE), deckFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapOnSpecificChromosomesMutator<>(1, deckListChromosomes), new BitSwapOnSpecificChromosomesMutator<>(1, deckListChromosomes))
				.build();

		Genotype<BitGene> deckResult = deckEngine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(deckResult.getChromosome(0).getGene(winTheGameIndex).booleanValue());

		GameDeck deckForMulliganOptimization = new GameDeck(HeroClass.BLUE);

		for (int i = 0; i < deckResult.getChromosome().length(); i++) {
			if (deckResult.getChromosome(0).getGene(i).booleanValue()) {
				deckForMulliganOptimization.getCards().add(indexInBitmap.get(i));
			}
		}

		deckAndMulliganGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH_FOR_MULLIGAN_GENERATION);
		deckAndMulliganGeneratorContext.setBasicTournamentDecks(Collections.singletonList(deckForMulliganOptimization));

		Factory<Genotype<BitGene>> mulliganFactory = new SpecificGenotypesFactory(Collections.singletonList(deckResult));
		Engine<BitGene, Double> mulliganEngine = Engine.builder((individual) -> deckAndMulliganGeneratorContext.fitness(individual, HeroClass.BLUE), mulliganFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(new ActsOnSpecificChromosomesBasicMutator<>(1, mulliganChromosomes), new MultiPointCrossoverOnSpecificChromosomes<>(1, mulliganChromosomes))
				.build();

		Genotype<BitGene> mulliganResult = mulliganEngine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(mulliganResult.getChromosome(1).getGene(winTheGameIndex).booleanValue());
	}
}
