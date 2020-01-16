package com.hiddenswitch.deckgeneration;

import io.jenetics.BitGene;
import io.jenetics.Genotype;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toList;

public class DeckGeneratorRoundRobinContext extends DeckGeneratorContext {
	HashMap<Genotype<BitGene>, Double> winRatesForEachGenotype = new HashMap<>();

	public DeckGeneratorRoundRobinContext(List<Card> indexInBitmap) {
		super(indexInBitmap, new ArrayList<>());
	}

	public void runTournament(List<Genotype<BitGene>> genotypes, String heroClass) {
		HashMap<GameDeck, Genotype<BitGene>> deckToGenotypeMap = new HashMap<>();
		List<GameDeck> decks = new ArrayList<>();
		for (Genotype<BitGene> genotype : genotypes) {
			GameDeck deck = deckFromBitGenotype(genotype, heroClass);
			decks.add(deck);
			deckToGenotypeMap.put(deck, genotype);
		}

		Map<GameDeck[], SimulationResult> resultList = new HashMap<>();
		// Make two bots that make random decisions and play multiple games (GAMES_PER_MATCH)
		for (GameDeck[] deckPairArray : GameContext.getDeckCombinations(decks, false)) {
			AtomicReference<SimulationResult> resultRef = new AtomicReference<>();
			resultRef.set(GameContext.simulate(Arrays.asList(deckPairArray),
					() -> this.playerBehaviour,
					() -> this.enemyBehaviour,
					gamesPerMatch,
					true,
					false,
					null,
					this::handleContext));

			SimulationResult result = resultRef.get();
			resultList.put(deckPairArray, result);
		}

		// Order the decks by average win rate
		List<Map.Entry<GameDeck, Double>> results = resultList.entrySet().stream()
				.flatMap(kv -> Stream.of(
						new DeckWinrateTuple(kv.getKey()[0], kv.getValue().getPlayer1Stats().getDouble(Statistic.WIN_RATE)),
						new DeckWinrateTuple(kv.getKey()[1], kv.getValue().getPlayer2Stats().getDouble(Statistic.WIN_RATE))
				))
				.collect(Collectors.groupingBy(DeckWinrateTuple::getGameDeck, averagingDouble(DeckWinrateTuple::getWinRate)))
				.entrySet()
				.stream()
				.collect(toList());

		for (Map.Entry<GameDeck, Double> deckResult : results) {
			winRatesForEachGenotype.put(deckToGenotypeMap.get(deckResult.getKey()), deckResult.getValue());
		}
	}

	public void clearWinRates() {
		winRatesForEachGenotype.clear();
	}

	public double fitness(Genotype<BitGene> individual) {
		return winRatesForEachGenotype.get(individual);
	}

	public static class DeckWinrateTuple {
		private GameDeck gameDeck;
		private double winRate;

		public DeckWinrateTuple(GameDeck gameDeck, double winRate) {
			this.gameDeck = gameDeck;
			this.winRate = winRate;
		}

		public GameDeck getGameDeck() {
			return gameDeck;
		}

		public DeckWinrateTuple setGameDeck(GameDeck gameDeck) {
			this.gameDeck = gameDeck;
			return this;
		}

		public double getWinRate() {
			return winRate;
		}

		public DeckWinrateTuple setWinRate(double winRate) {
			this.winRate = winRate;
			return this;
		}
	}
}
