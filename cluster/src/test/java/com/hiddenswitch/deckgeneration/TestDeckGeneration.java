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
}

