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
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

// Our goal: Try to make an optimal deck under some very simple constraints where the winrates in a tournament
// of these decks by random play is maximized.

public class TestDeckGeneration {
	int maxCardsPerDeck = 4;

	// Tests that the deckFromBitGenotype function correctly translates
	// a Genotype<BitGene> into the corresponding deck
	@Test
	public void testDeckFromBitGenotype() {
		XORShiftRandom random = new XORShiftRandom(101010L);
		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());
		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, new ArrayList<>(0));
		BitSet bits = new BitSet(CardCatalogue.getAll().size());
		GameDeck testDeck = new GameDeck(HeroClass.BLUE);
		for (int j = 0; j < maxCardsPerDeck; j++) {
			int toAdd = random.nextInt(indexInBitmap.size());
			while (bits.get(toAdd)) {
				toAdd = random.nextInt(indexInBitmap.size());
			}
			bits.flip(toAdd);
			testDeck.getCards().add(indexInBitmap.get(toAdd));
		}
		Genotype<BitGene> testGenotype = Genotype.of(BitChromosome.of(bits));
		GameDeck comparisonDeck = deckGeneratorContext.deckFromBitGenotype(testGenotype, HeroClass.BLUE);
		List<String> testDeckIds = testDeck.getCards().stream().map(card -> card.getCardId()).collect(toList());
		List<String> comparisonDeckIds = comparisonDeck.getCards().stream().map(card -> card.getCardId()).collect(toList());
		assertTrue(testDeckIds.containsAll(comparisonDeckIds) && testDeckIds.size() == comparisonDeckIds.size());
	}

	/**
	 * Tests the "invalid" card aspect of DeckGeneFactory, which asserts that
	 * the card represented by any integer passed in cannot be used in the initial population
	 * of decks
	 */
	@Test
	public void invalidCardTest() {
		int bitLength = 2;

		List<Integer> invalidCards = new ArrayList<>();
		invalidCards.add(0);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(1, bitLength, invalidCards);
		Engine<BitGene, Integer> engine = Engine.builder((individual) -> 0, bitGeneFactory)
				.mapping(r -> {
					List<Genotype<BitGene>> genotypes = r.getGenotypes().stream().collect(toList());
					for (int i = 0; i < genotypes.size(); i++) {
						assertTrue(!genotypes.get(i).getChromosome().getGene(0).booleanValue());
					}
					return r;
				})
				.populationSize(1000)
				.alterers(new SwapMutator<>(0))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(2)
				.collect(EvolutionResult.toBestGenotype());
	}

	/**
	 * Generate decks under a simple encoding. A bitmap that corresponds to whether or not a card is in a deck.
	 * In our simple scheme, we are only going to deal with mage cards. We're going to use only basic cards from
	 * mage and basic neutrals. We will only have at most one card. Decks of size 30. Any decks that are invalid
	 * will always be ranked at the bottom when sorted by whatever evaluated Jenetics actually uses.
	 */
	@Test
	public void testDeckGeneratorForWinTheGameCardWithOnlyBitSwapMutator() {

		// General outline:
		// 1. Generate N decks
		// 2. Each deck is played against a fixed pool of other test decks (in the case of the paper, the hearthstone meta game). The winrate is its fitness.
		// 3. Mix aspects of the best decks together.
		// 4. Remove the worst performing decks and replace them with new random decks.

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
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());

		// Create random decks for the tournament
		for (int i = 0; i < 10; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < maxCardsPerDeck; j++) {
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
		deckGeneratorContext.setMaxCardsPerDeck(maxCardsPerDeck);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		int winTheGameIndex = indexInBitmap.size() - 1;
		List<Integer> invalidCards = new ArrayList<>(0);
		invalidCards.add(indexInBitmap.size() - 1);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1), new BitSwapMutator<>(1))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(result.getChromosome().getGene(winTheGameIndex).booleanValue());
	}

	@Test
	public void testDeckGeneratorRoundRobinTournament() {
		int MAX_CARDS_IN_DECK = 4;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 10;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<Genotype<BitGene>> basicTournamentDeckGenotypes = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());

		for (int i = 0; i < 9; i++) {
			BitSet bits = new BitSet(indexInBitmap.size() + 1);
			for (int j = 0; j < MAX_CARDS_IN_DECK; j++) {
				int bitToFlip = random.nextInt(indexInBitmap.size());
				while (bits.get(bitToFlip)) {
					bitToFlip = random.nextInt(indexInBitmap.size());
				}
				bits.flip(bitToFlip);
			}
			basicTournamentDeckGenotypes.add(Genotype.of(BitChromosome.of(bits)));
		}

		// Ensure that we do not begin with "win the game"
		// in any of our original population decks
		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));

		int winTheGameIndex = indexInBitmap.size() - 1;

		BitSet winningBits = new BitSet(indexInBitmap.size());
		winningBits.flip(winTheGameIndex);
		for (int i = 0; i < MAX_CARDS_IN_DECK - 1; i++) {
			int bitToFlip = random.nextInt(indexInBitmap.size());
			while (winningBits.get(bitToFlip)) {
				bitToFlip = random.nextInt(indexInBitmap.size());
			}
			winningBits.flip(bitToFlip);
		}
		basicTournamentDeckGenotypes.add(Genotype.of(BitChromosome.of(winningBits)));

		// Set up our tournament playing environment
		DeckGeneratorRoundRobinContext deckGeneratorContext = new DeckGeneratorRoundRobinContext(indexInBitmap);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(MAX_CARDS_IN_DECK);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		deckGeneratorContext.runTournament(basicTournamentDeckGenotypes, HeroClass.BLUE);
		Double bestWinRate = deckGeneratorContext.winRatesForEachGenotype.get(basicTournamentDeckGenotypes.get(basicTournamentDeckGenotypes.size() - 1));
		for (int i = 0; i < basicTournamentDeckGenotypes.size() - 2; i++) {
			assertTrue(bestWinRate > deckGeneratorContext.winRatesForEachGenotype.get(basicTournamentDeckGenotypes.get(i)));
		}
		Double allWinRatesAverage = deckGeneratorContext.winRatesForEachGenotype.values().stream().mapToDouble(val -> val.doubleValue()).average().getAsDouble();
		assertTrue(allWinRatesAverage == 0.5);
	}

	/**
	 * Here, instead of having a constant set of tournament decks and evolving our decks based on the
	 * win rate against those decks, we instead let the fitnesses be the win rates of the decks
	 * against the other decks in the pool (survival of the fittest essentially). Then, we take the best deck
	 * from the last population.
	 */
	@Test
	public void testDeckGeneratorForContinuallyUpdatingTournamentDecks() {
		int MAX_CARDS_PER_DECK = 4;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 10;
		int POPULATION_SIZE = 10;
		int NUMBER_OF_GENERATIONS = 30;

		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());

		// Ensure that we do not begin with "win the game"
		// in any of our original population decks
		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));

		// Set up our tournament playing environment
		DeckGeneratorRoundRobinContext deckGeneratorContext = new DeckGeneratorRoundRobinContext(indexInBitmap);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(MAX_CARDS_PER_DECK);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		int winTheGameIndex = indexInBitmap.size() - 1;
		List<Integer> invalidCards = new ArrayList<>(0);
		invalidCards.add(indexInBitmap.size() - 1);

		Engine.Evaluator<BitGene, Double> evaluator = population -> {
			deckGeneratorContext.runTournament(population.stream().map(p -> p.getGenotype()).collect(toList()), HeroClass.BLUE);
			population.forEach(Phenotype::evaluate);
			deckGeneratorContext.clearWinRates();
			return population.asISeq();
		};

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(MAX_CARDS_PER_DECK, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual), bitGeneFactory)
				.mapping(pop -> pop)
				.evaluator(evaluator)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1), new BitSwapMutator<>(1))
				.build();

		EvolutionResult<BitGene, Double> result = engine
				.stream()
				.skip(NUMBER_OF_GENERATIONS)
				.findFirst().get();
		assertTrue(result.getBestPhenotype().getGenotype().getChromosome().getGene(winTheGameIndex).booleanValue());
	}

	// Tests the use of the stable fitness terminator,
	// which terminates the genetic algorithm when the fitness
	// has been stable for a certain number of generations
	@Test
	public void testDeckGeneratorUsingStableFitnessTermination() {
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 10;
		int POPULATION_SIZE = 20;
		int STABLE_GENERATIONS_COUNT = 5;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());

		// Create random decks for the tournament
		for (int i = 0; i < 10; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < maxCardsPerDeck; j++) {
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
		deckGeneratorContext.setMaxCardsPerDeck(maxCardsPerDeck);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		int winTheGameIndex = indexInBitmap.size() - 1;
		List<Integer> invalidCards = new ArrayList<>(0);
		invalidCards.add(winTheGameIndex);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1), new BitSwapMutator<>(1))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS_COUNT))
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(result.getChromosome().getGene(winTheGameIndex).booleanValue());
	}

	/**
	 * Now we will test to see if our algorithm finds "combos"
	 */
	@Test(invocationCount = 14)
	public void testDeckGeneratorForComboWithOnlyOneCostCards() {
		maxCardsPerDeck = 2;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 10;
		int POPULATION_SIZE = 20;
		int NUMBER_OF_GENERATIONS = 20;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC
						&& card.getBaseManaCost() == 1
				)
				.collect(toList());

		// Create random decks for the tournament
		for (int i = 0; i < 10; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < maxCardsPerDeck; j++) {
				tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
			}
			basicTournamentDecks.add(tournamentDeck);
		}

		// Ensure that we do not begin with the win combo
		// in any of our original population decks
		indexInBitmap.add(CardCatalogue.getCardById("minion_combo_win_1"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_combo_win_2"));

		// Set up our tournament playing environment
		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, basicTournamentDecks);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(maxCardsPerDeck);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		int winComboIndex1 = indexInBitmap.size() - 2;
		int winComboIndex2 = indexInBitmap.size() - 1;
		List<Integer> invalidCards = new ArrayList<>(0);
		invalidCards.add(winComboIndex1);
		invalidCards.add(winComboIndex2);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.mapping(EvolutionResult.toUniquePopulation())
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1), new BitSwapMutator<>(1))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(result.getChromosome().getGene(winComboIndex1).booleanValue());
		assertTrue(result.getChromosome().getGene(winComboIndex2).booleanValue());
	}

	/**
	 * Our previous test uses a longer exhaustive approach to find combos
	 * Let's see if utilizing swapping between decks helps at all
	 */
	@Test(invocationCount = 14)
	public void testDeckGeneratorForComboWithOnlyOneCostCardsUsingBitSwapBetweenTwoSequences() {
		maxCardsPerDeck = 2;
		int GAMES_PER_MATCH = 18;
		int STARTING_HP = 10;
		int POPULATION_SIZE = 20;
		int NUMBER_OF_GENERATIONS = 15;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC
						&& card.getBaseManaCost() == 1
				)
				.collect(toList());

		// Create random decks for the tournament
		for (int i = 0; i < 10; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < maxCardsPerDeck; j++) {
				tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
			}
			basicTournamentDecks.add(tournamentDeck);
		}

		// Ensure that we do not begin with the win combo
		// in any of our original population decks
		indexInBitmap.add(CardCatalogue.getCardById("minion_combo_win_1"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_combo_win_2"));

		// Set up our tournament playing environment
		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, basicTournamentDecks);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(maxCardsPerDeck);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		int winComboIndex1 = indexInBitmap.size() - 2;
		int winComboIndex2 = indexInBitmap.size() - 1;
		List<Integer> invalidCards = new ArrayList<>(0);
		invalidCards.add(winComboIndex1);
		invalidCards.add(winComboIndex2);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.mapping(EvolutionResult.toUniquePopulation())
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(1), new BitSwapMutator<>(1), new BitSwapBetweenTwoSequencesMutator<>(1))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(result.getChromosome().getGene(winComboIndex1).booleanValue());
		assertTrue(result.getChromosome().getGene(winComboIndex2).booleanValue());
	}

	/**
	 * A test primarily for reference
	 * With a player behaviour that can cast buff cards on enemy minions,
	 * a gentic algorithm may not necessarily add a +10/+10 buff card
	 * to the deck
	 */
	@Ignore
	@Test(invocationCount = 10)
	public void testWillNotNecessarilyFindBuffCardIfBuffingEnemyMinionsIsAllowed() {
		int GAMES_PER_MATCH_FOR_DECK_GENERATION = 10;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int CARDS_IN_DECK = 20;
		int STABLE_GENERATIONS = 10;
		int NUMBER_OF_DECKS = 10;

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());

		indexInBitmap.add(CardCatalogue.getCardById("spell_buff_10"));
		int buffIndex = indexInBitmap.size() - 1;

		Random random = new XORShiftRandom(101010L);

		// Create random decks for the tournament
		for (int i = 0; i < NUMBER_OF_DECKS; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < CARDS_IN_DECK; j++) {
				tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
			}
			basicTournamentDecks.add(tournamentDeck);
		}

		DeckAndDecisionGeneratorContext deckAndDecisionGeneratorContext = new DeckAndDecisionGeneratorContext(indexInBitmap, basicTournamentDecks, new ArrayList<>());
		PlayRandomWithoutSelfDamageWithDefinedDecisions enemyBehvaiour = new PlayRandomWithoutSelfDamageWithDefinedDecisions();
		enemyBehvaiour.setCanEndTurnIfAttackingEnemyHeroIsValid(true);
		enemyBehvaiour.setCanBuffEnemyMinions(true);
		deckAndDecisionGeneratorContext.setEnemyBehaviour(enemyBehvaiour);
		deckAndDecisionGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH_FOR_DECK_GENERATION);
		deckAndDecisionGeneratorContext.setStartingHp(STARTING_HP);
		deckAndDecisionGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		List<Integer> deckListChromosomes = Collections.singletonList(0);

		Factory<Genotype<BitGene>> deckFactory = new DeckAndDecisionGeneFactory(CARDS_IN_DECK, indexInBitmap.size(), Collections.singletonList(buffIndex), 0, 0, 1.0);

		Engine<BitGene, Double> deckEngine = Engine.builder((individual) -> deckAndDecisionGeneratorContext.fitness(individual, HeroClass.BLUE), deckFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(
						new BitSwapOnSpecificChromosomesMutator<>(1, deckListChromosomes),
						new BitSwapOnSpecificChromosomesMutator<>(1, deckListChromosomes),
						new BitSwapBetweenTwoSequencesOnSpecificChromosomesMutator<>(1, deckListChromosomes))
				.build();

		Phenotype<BitGene, Double> deckResultPhenotype = deckEngine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestPhenotype());

		Genotype<BitGene> deckResult = deckResultPhenotype.getGenotype();
		assertTrue(deckResult.getChromosome(0).getGene(buffIndex).booleanValue());
	}

	/**
	 * With a player behaviour that never casts buff cards on enemy minions,
	 * tests that the genetic algorithm adds a +10/+10 buff card to the deck
	 * if avaialable
	 */
	@Test(invocationCount = 10)
	public void testWillFindBuffCardIfBuffingEnemyMinionsIsForbidden() {
		int GAMES_PER_MATCH_FOR_DECK_GENERATION = 10;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int CARDS_IN_DECK = 20;
		int STABLE_GENERATIONS = 10;
		int NUMBER_OF_DECKS = 10;

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC)
				.collect(toList());

		indexInBitmap.add(CardCatalogue.getCardById("spell_buff_10"));
		int buffIndex = indexInBitmap.size() - 1;

		Random random = new XORShiftRandom(101010L);

		// Create random decks for the tournament
		for (int i = 0; i < NUMBER_OF_DECKS; i++) {
			GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
			for (int j = 0; j < CARDS_IN_DECK; j++) {
				tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
			}
			basicTournamentDecks.add(tournamentDeck);
		}

		PlayRandomWithoutSelfDamageWithDefinedDecisions enemyBehvaiour = new PlayRandomWithoutSelfDamageWithDefinedDecisions();
		DeckAndDecisionGeneratorContext deckAndDecisionGeneratorContext = new DeckAndDecisionGeneratorContext(indexInBitmap, basicTournamentDecks, new ArrayList<>(), Collections.singletonList(DecisionType.CANNOT_BUFF_ENEMY_MINIONS));
		enemyBehvaiour.setCanEndTurnIfAttackingEnemyHeroIsValid(true);
		enemyBehvaiour.setCanBuffEnemyMinions(false);
		deckAndDecisionGeneratorContext.setEnemyBehaviour(enemyBehvaiour);
		deckAndDecisionGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH_FOR_DECK_GENERATION);
		deckAndDecisionGeneratorContext.setStartingHp(STARTING_HP);
		deckAndDecisionGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		List<Integer> deckListChromosomes = Collections.singletonList(0);

		Factory<Genotype<BitGene>> deckFactory = new DeckAndDecisionGeneFactory(CARDS_IN_DECK, indexInBitmap.size(), Collections.singletonList(buffIndex), 0, 1, 1.0);

		Engine<BitGene, Double> deckEngine = Engine.builder((individual) -> deckAndDecisionGeneratorContext.fitness(individual, HeroClass.BLUE), deckFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(
						new BitSwapOnSpecificChromosomesMutator<>(1, deckListChromosomes),
						new BitSwapOnSpecificChromosomesMutator<>(1, deckListChromosomes),
						new BitSwapBetweenTwoSequencesOnSpecificChromosomesMutator<>(1, deckListChromosomes))
				.build();

		Phenotype<BitGene, Double> deckResultPhenotype = deckEngine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestPhenotype());

		Genotype<BitGene> deckResult = deckResultPhenotype.getGenotype();
		assertTrue(deckResult.getChromosome(0).getGene(buffIndex).booleanValue());
	}


	@Test
	@Ignore
	public void testObjectiveFunction() {
		int STARTING_HP = 30;
		int MAX_CARDS_PER_DECK = 10;
		int POPULATION_SIZE = 10;
		int STABLE_GENERATIONS = 10;
		int GAMES_PER_MATCH = 200;

		CardCatalogue.loadCardsFromPackage();
		List<Card> indexInBitmap = new ArrayList<>();
		indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_novice_engineer"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_gnomish_inventor"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_loot_hoarder"));
		indexInBitmap.add(CardCatalogue.getCardById("spell_power_word_shield"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_polluted_hoarder"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_coldlight_oracle"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_bloodmage_thalnos"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_acolyte_of_pain"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_novice_engineer"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_ogre_magi"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_chillwind_yeti"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_magma_rager"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_booty_bay_bodyguard"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_goldshire_footman"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_razorfen_hunter"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_lord_of_the_arena"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_stormwind_champion"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_core_hound"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_murloc_raider"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_wisp"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_razorfen_hunter"));
		indexInBitmap.add(CardCatalogue.getCardById("minion_win_the_game_alt"));

		int badWinIndex = indexInBitmap.size() - 1;
		int goodWinIndex = 0;

		GameDeck generatedDeck = new GameDeck(HeroClass.BLUE);

		generatedDeck.getCards().addCard("spell_win_the_game");
		generatedDeck.getCards().addCard("minion_chillwind_yeti");
		generatedDeck.getCards().addCard("minion_bloodfen_raptor");
		generatedDeck.getCards().addCard("minion_novice_engineer");
		generatedDeck.getCards().addCard("minion_gnomish_inventor");
		generatedDeck.getCards().addCard("minion_loot_hoarder");
		generatedDeck.getCards().addCard("spell_power_word_shield");
		generatedDeck.getCards().addCard("minion_polluted_hoarder");
		generatedDeck.getCards().addCard("minion_coldlight_oracle");
		generatedDeck.getCards().addCard("minion_bloodmage_thalnos");

		List<GameDeck> tournamentDecksList = Collections.singletonList(generatedDeck);

		DeckGeneratorContext deckGeneratorContext = new DeckGeneratorContext(indexInBitmap, tournamentDecksList);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(MAX_CARDS_PER_DECK);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		List<DecisionType> cardListDecisionTypes = new ArrayList<>();
		cardListDecisionTypes.add(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES);
		cardListDecisionTypes.add(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES);

		List<HashSet<String>> cardListsForDecisionTypes = new ArrayList<>();
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		NeverUseOnOwnMinion neverUseOnOwnMinion = new NeverUseOnOwnMinion();
		cardListsForDecisionTypes.add(neverUseOnEnemyMinions.classicAndBasicSets);
		cardListsForDecisionTypes.add(neverUseOnOwnMinion.classicAndBasicSets);

		HashSet<DecisionType> booleanDecisionTypes = new HashSet<>();
		booleanDecisionTypes.add(DecisionType.CANNOT_ATTACK_WITH_A_MINION_THAT_WILL_DIE_AND_NOT_KILL_OTHER_MINION);

		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(cardListDecisionTypes, cardListsForDecisionTypes, booleanDecisionTypes);

		deckGeneratorContext.setEnemyBehaviour(behaviour);
		deckGeneratorContext.setPlayerBehaviour(behaviour);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(MAX_CARDS_PER_DECK, indexInBitmap.size());
		List<Mutator> mutators = new ArrayList<>();
		mutators.add(new BitSwapMutator<>(.7));
		mutators.add(new BitSwapToChangeCardCountMutator<>(.7, indexInBitmap));
		mutators.add(new BitSwapByManaCostMutator<>(.7, indexInBitmap));
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(
						new UmbrellaMutator<>(mutators),
						new UmbrellaMutator<>(mutators),
						new BitSwapBetweenTwoSequencesMutator<>(.7),
						new BitSwapBetweenTwoSequencesMutator<>(.7))
				.build();

		Phenotype<BitGene, Double> deckResultPhenotype = engine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestPhenotype());

		assertTrue(deckResultPhenotype.getGenotype().getChromosome().getGene(goodWinIndex).booleanValue());
		assertTrue(!deckResultPhenotype.getGenotype().getChromosome().getGene(badWinIndex).booleanValue());
		assertTrue(deckResultPhenotype.getFitness() < .51);
	}
}

