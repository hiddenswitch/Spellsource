package com.hiddenswitch.deckgeneration;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

public class DeckGenerationTests {
	private static int GAMES_PER_MATCH = 10;
	public class DeckWinrateTuple {
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

	public List<Map.Entry<GameDeck, Double>> winRatesFromTournamentWithDecks(List<GameDeck> decks) throws InterruptedException {
		Map<GameDeck[], SimulationResult> resultList = new HashMap<>();
		// Make two bots that make random decisions and play a match a few times
		for (GameDeck[] deckPairArray : GameContext.getDeckCombinations(decks, false)) {
			AtomicReference<SimulationResult> resultRef = new AtomicReference<>();
			GameContext.simulate(Arrays.asList(deckPairArray), Arrays.asList(PlayRandomBehaviour::new, PlayRandomBehaviour::new), GAMES_PER_MATCH, true,
					resultRef::set);

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
				.sorted(Comparator.comparingDouble(kv -> -kv.getValue()))
				.collect(toList());
		return results;
	}

	@Test
	public void testSingletonDecksWithWinCard() throws InterruptedException {
		// Create one card decks that each have a random card, plus a one-card deck that contains Win the Game. We expect
		// win the game to be rated the highest.
		Random random = new XORShiftRandom(101010L);
		CardCatalogue.loadCardsFromPackage();
		List<Card> cardCatalogueRecords = CardCatalogue.getAll()
				.stream()
				.filter(Card::isCollectible)
				.filter(card -> !card.getCardId().equals("spell_win_the_game"))
				.filter(card -> card.getBaseManaCost() == 1)
				.filter(card -> card.getHeroClass() == HeroClass.ANY)
				.collect(toList());

		GameDeck winningDeck = new GameDeck(HeroClass.ANY, Collections.singletonList("spell_win_the_game"));
		List<GameDeck> losingDecks = Stream.generate(() ->
				new GameDeck(HeroClass.ANY,
						Collections.singletonList(cardCatalogueRecords.get(random.nextInt(cardCatalogueRecords.size())).getCardId())
				)).limit(20)
				.collect(toList());

		List<GameDeck> allDecks = new ArrayList<>(losingDecks);
		allDecks.add(winningDeck);
		final List<Map.Entry<GameDeck, Double>> results = winRatesFromTournamentWithDecks(allDecks);
		assertEquals(results.get(0).getKey(), winningDeck);
	}

	@Test
	public void testFullRandomDecks() throws InterruptedException {
		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> decks = Stream.generate(() -> Deck.randomDeck(HeroClass.ANY, DeckFormat.STANDARD)).limit(20).collect(toList());
		for (int i = 0; i < 20; i++){
			decks.get(i).setDeckId(i + "");
		}
		final List<Map.Entry<GameDeck, Double>> results =  winRatesFromTournamentWithDecks(decks);
		for (Map.Entry<GameDeck, Double> kv : results) {
			System.out.println(kv.getKey().getDeckId() + " " + kv.getValue() + "\n");
		}
	}


	@Test
	public void testDeckComparisonRatesDecksThatWinTheGameHighest() throws InterruptedException {
		// Objective: To generate competitive decks

		// Steps to get there:
		//   1. Somehow assemble decks
		//   2. Compare them to one another and choose the best deck
		//   3. Validate that the best deck is actually pretty decent

		// What are some clever ways that we can test these pieces in isolation and make sure that
		// all of our assumptions actually work?

		// This is really an optimization problem where we're trying to find the best deck given an objective function which
		// we're going to validate here.
	}
}
