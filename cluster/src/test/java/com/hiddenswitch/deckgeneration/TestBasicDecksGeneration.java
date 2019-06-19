package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TestBasicDecksGeneration {
	BasicTournamentDecks basicTournamentDecks = new BasicTournamentDecks();

	public void deckGenerationForClass(HeroClass heroClass) {
		int MAX_CARDS_PER_DECK = 30;
		int GAMES_PER_MATCH = 25;
		int STARTING_HP = 30;
		int POPULATION_SIZE = 10;
		int STABLE_GENERATIONS = 20;

		CardCatalogue.loadCardsFromPackage();

		List<Card> indexInBitmap = CardCatalogue.getAll().stream()
				.filter(card -> card.isCollectible()
						&& (card.getHeroClass() == heroClass || card.getHeroClass() == HeroClass.ANY)
						&& (card.getCardSet() == CardSet.BASIC))
				.collect(toList());

		indexInBitmap.addAll(CardCatalogue.getAll().stream()
				.filter(card -> card.isCollectible()
						&& (!card.getRarity().equals(Rarity.LEGENDARY))
						&& (card.getHeroClass() == heroClass || card.getHeroClass() == HeroClass.ANY)
						&& (card.getCardSet() == CardSet.BASIC))
				.collect(toList()));

		BasicTournamentDecks basicTournamentDecks = new BasicTournamentDecks();
		List<GameDeck> tournamentDecksList = basicTournamentDecks.getAllTrumpBasicDecks();

		DeckAndDecisionGeneratorContext deckGeneratorContext = new DeckAndDecisionGeneratorContext(indexInBitmap, tournamentDecksList);
		deckGeneratorContext.setStartingHp(STARTING_HP);
		deckGeneratorContext.setMaxCardsPerDeck(MAX_CARDS_PER_DECK);
		deckGeneratorContext.setGamesPerMatch(GAMES_PER_MATCH);

		List<DecisionType> alwaysActiveCardListDecisionTypes = new ArrayList<>();
		alwaysActiveCardListDecisionTypes.add(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES);
		alwaysActiveCardListDecisionTypes.add(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES);

		List<HashSet<String>> cardListsForAlwaysActiveCardListDecisionTypes = new ArrayList<>();
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		NeverUseOnOwnMinion neverUseOnOwnMinion = new NeverUseOnOwnMinion();
		cardListsForAlwaysActiveCardListDecisionTypes.add(neverUseOnEnemyMinions.classicAndBasicSets);
		cardListsForAlwaysActiveCardListDecisionTypes.add(neverUseOnOwnMinion.classicAndBasicSets);

		PlayRandomWithoutSelfDamageWithDefinedDecisions enemyBehaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(alwaysActiveCardListDecisionTypes, cardListsForAlwaysActiveCardListDecisionTypes);

		deckGeneratorContext.setEnemyBehaviour(enemyBehaviour);
		deckGeneratorContext.setAlwaysActiveCardListDecisionTypes(alwaysActiveCardListDecisionTypes, cardListsForAlwaysActiveCardListDecisionTypes);

		Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(MAX_CARDS_PER_DECK, indexInBitmap.size());
		Engine<BitGene, Double> engine = Engine.builder((individual) -> deckGeneratorContext.fitness(individual, heroClass), bitGeneFactory)
				.mapping(pop -> pop)
				.populationSize(POPULATION_SIZE)
				.alterers(new BitSwapMutator<>(.8),
						new BitSwapToChangeCardCountMutator<>(.8, indexInBitmap),
						new BitSwapByManaCostMutator<>(.8, indexInBitmap),
						new BitSwapBetweenTwoSequencesMutator<>(.8))
				.build();

		Phenotype<BitGene, Double> deckResultPhenotype = engine.stream()
				.limit(Limits.bySteadyFitness(STABLE_GENERATIONS))
				.collect(EvolutionResult.toBestPhenotype());

		for (int i = 0; i < indexInBitmap.size(); i++) {
			if (deckResultPhenotype.getGenotype().getChromosome().getGene(i).booleanValue()) {
				System.out.println(indexInBitmap.get(i));
			}
		}
		System.out.println(deckResultPhenotype.getFitness());
	}

	@Test
	public void druidDeckGeneration() {
		deckGenerationForClass(HeroClass.BROWN);
	}

	@Test
	public void hunterDeckGeneration() {
		deckGenerationForClass(HeroClass.GREEN);
	}

	@Test
	public void mageDeckGeneration() {
		deckGenerationForClass(HeroClass.BLUE);
	}

	@Test
	public void paladinDeckGeneration() {
		deckGenerationForClass(HeroClass.GOLD);
	}
	@Test

	public void priestDeckGeneration() {
		deckGenerationForClass(HeroClass.WHITE);
	}

	@Test
	public void rogueDeckGeneration() {
		deckGenerationForClass(HeroClass.BLACK);
	}

	@Test
	public void shamanDeckGeneration() {
		deckGenerationForClass(HeroClass.SILVER);
	}
	@Test
	public void warlockDeckGeneration() {
		deckGenerationForClass(HeroClass.VIOLET);
	}
	@Test
	public void warriorDeckGeneration() {
		deckGenerationForClass(HeroClass.RED);
	}
}

