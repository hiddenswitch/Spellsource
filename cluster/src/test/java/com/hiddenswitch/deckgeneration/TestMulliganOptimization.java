package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.SinglePointCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class TestMulliganOptimization {
	@Test
	public void willNotMulliganWinTheGameTest() {
		int GAMES_PER_MATCH = 50;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int NUMBER_OF_GENERATIONS = 50;
		int CARDS_IN_DECK = 30;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC).limit(CARDS_IN_DECK - 1)
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

	// Perhaps make the mulligan mutators more consistent to make this pass?
	@Ignore
	@Test(invocationCount = 10)
	public void willFindAndNotMulliganWinTheGameTest() {
		int GAMES_PER_MATCH = 10;
		int STARTING_HP = 20;
		int POPULATION_SIZE = 10;
		int NUMBER_OF_GENERATIONS = 40;
		int CARDS_IN_DECK = 10;

		XORShiftRandom random = new XORShiftRandom(101010L);

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC).limit(22)
				.collect(toList());

		// Create random decks for the tournament
		for (int i = 0; i < 5; i++) {
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
		deckAndMulliganGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);
		deckAndMulliganGeneratorContext.setStartingHp(STARTING_HP);
		deckAndMulliganGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		List<Integer> chromosomesToBitSwapActOn = new ArrayList<>();
		chromosomesToBitSwapActOn.add(0);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckAndMulliganGeneFactory(CARDS_IN_DECK, indexInBitmap.size(), invalidCards);
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckAndMulliganGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(new ActsOnSpecificChromosomesBasicMutator<>(1, chromosomesToActOn), new BitSwapOnSpecificChromosomesMutator<>(1, chromosomesToBitSwapActOn))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(NUMBER_OF_GENERATIONS)
				.collect(EvolutionResult.toBestGenotype());

		assertTrue(result.getChromosome(1).getGene(winTheGameIndex).booleanValue());
		assertTrue(result.getChromosome(0).getGene(winTheGameIndex).booleanValue());

	}
}
