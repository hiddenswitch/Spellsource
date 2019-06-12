package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class TestAttackingDecisionOptimization {
	@Test
	public void testWillAlwaysHitFace() {
		int GAMES_PER_MATCH = 100;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 3;
		int CARDS_IN_DECK = 30;
		int STABLE_GENERATIONS = 5;

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC).limit(CARDS_IN_DECK - 1)
				.collect(toList());

		indexInBitmap.add(CardCatalogue.getCardById("minion_charge_30"));

		GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE, indexInBitmap.stream().map(card -> card.getCardId()).collect(toList()));
		basicTournamentDecks.add(tournamentDeck);

		DeckAndDecisionGeneratorContext deckAndDecisionGeneratorContext = new DeckAndDecisionGeneratorContext(indexInBitmap, basicTournamentDecks, new ArrayList<>(), Collections.singletonList(DecisionType.ALWAYS_ATTACK_ENEMY_HERO));
		PlayRandomWithoutSelfDamageBehaviour enemyBehvaiour = new PlayRandomWithoutSelfDamageBehaviour();
		deckAndDecisionGeneratorContext.setEnemyBehaviour(enemyBehvaiour);
		deckAndDecisionGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);
		deckAndDecisionGeneratorContext.setStartingHp(STARTING_HP);
		deckAndDecisionGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckAndDecisionGeneFactory(CARDS_IN_DECK, CARDS_IN_DECK, new ArrayList<>(), Collections.singletonList(DecisionType.ALWAYS_ATTACK_ENEMY_HERO));
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckAndDecisionGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(new ActsOnSpecificChromosomesBasicMutator<>(1, chromosomesToActOn))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestGenotype());


		assertTrue(result.getChromosome(1).getGene().booleanValue());
	}

	@Test
	public void testWillAlwaysHitFaceWithCharge30() {
		int GAMES_PER_MATCH = 50;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int CARDS_IN_DECK = 30;
		int STABLE_GENERATIONS = 10;

		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> basicTournamentDecks = new ArrayList<>();
		List<Card> indexInBitmap = CardCatalogue.getAll()
				.stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
						&& card.getCardSet() == CardSet.BASIC).limit(CARDS_IN_DECK - 1)
				.collect(toList());

		indexInBitmap.add(CardCatalogue.getCardById("minion_charge_30"));

		GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE, indexInBitmap.stream().map(card -> card.getCardId()).collect(toList()));
		basicTournamentDecks.add(tournamentDeck);

		DeckAndDecisionGeneratorContext deckAndDecisionGeneratorContext = new DeckAndDecisionGeneratorContext(indexInBitmap, basicTournamentDecks,  Collections.singletonList(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION), new ArrayList<>());
		PlayRandomWithoutSelfDamageBehaviour enemyBehvaiour = new PlayRandomWithoutSelfDamageBehaviour();
		deckAndDecisionGeneratorContext.setEnemyBehaviour(enemyBehvaiour);
		deckAndDecisionGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);
		deckAndDecisionGeneratorContext.setStartingHp(STARTING_HP);
		deckAndDecisionGeneratorContext.setMaxCardsPerDeck(CARDS_IN_DECK);

		List<Integer> chromosomesToActOn = new ArrayList<>();
		chromosomesToActOn.add(1);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckAndDecisionGeneFactory(CARDS_IN_DECK, CARDS_IN_DECK, Collections.singletonList(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION), new ArrayList<>());
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckAndDecisionGeneratorContext.fitness(individual, HeroClass.BLUE), bitGeneFactory)
				.populationSize(POPULATION_SIZE)
				.alterers(new ActsOnSpecificChromosomesBasicMutator<>(0.2, chromosomesToActOn), new MultiPointCrossoverOnSpecificChromosomes<>(1, 2, chromosomesToActOn))
				.build();

		Genotype<BitGene> result = engine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestGenotype());

		int chargeIndex = indexInBitmap.size() - 1;

		assertTrue(result.getChromosome(1).getGene(chargeIndex).booleanValue());
	}
}
