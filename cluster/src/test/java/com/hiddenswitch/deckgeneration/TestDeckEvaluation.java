package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.IntelligentBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

public class TestDeckEvaluation {
	static {
		CardCatalogue.loadCardsFromPackage();
	}
	int GAMES_PER_MATCH = 15;
	private DeckFormat hearthstone = DeckFormat.getFormat("hearthstone");

	public List<Map.Entry<GameDeck, Double>> statsFromTournament(List<GameDeck> decks) throws InterruptedException {
		Map<GameDeck[], SimulationResult> resultList = new HashMap<>();
		// Make two bots that make random decisions and play multiple games (GAMES_PER_MATCH)
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
						new TestDeckEvaluation.DeckWinrateTuple(kv.getKey()[0], kv.getValue().getPlayer1Stats().getDouble(Statistic.WIN_RATE)),
						new TestDeckEvaluation.DeckWinrateTuple(kv.getKey()[1], kv.getValue().getPlayer2Stats().getDouble(Statistic.WIN_RATE))
				))
				.collect(Collectors.groupingBy(TestDeckEvaluation.DeckWinrateTuple::getGameDeck, averagingDouble(TestDeckEvaluation.DeckWinrateTuple::getWinRate)))
				.entrySet()
				.stream()
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
				.filter(card -> card.getHeroClass().equals(HeroClass.ANY))
				.collect(toList());

		GameDeck winningDeck = new GameDeck(HeroClass.ANY, Collections.singletonList("spell_win_the_game"));
		List<GameDeck> losingDecks = Stream.generate(() ->
				new GameDeck(HeroClass.ANY,
						Collections.singletonList(cardCatalogueRecords.get(random.nextInt(cardCatalogueRecords.size())).getCardId())
				)).limit(20)
				.collect(toList());

		List<GameDeck> allDecks = new ArrayList<>(losingDecks);
		allDecks.add(winningDeck);
		final List<Map.Entry<GameDeck, Double>> unsortedResults = statsFromTournament(allDecks);
		final List<Map.Entry<GameDeck, Double>> results = unsortedResults.stream()
				.sorted(Comparator.comparingDouble(kv -> -kv.getValue()))
				.collect(toList());
		assertEquals(results.get(0).getKey(), winningDeck);
	}

	@Test
	public void testComboWins() throws InterruptedException {
		// Create one-card decks that each have a random card, plus a two-card deck that contains the win combo.
		// We also add two one-card decks that contain the combo pieces individually
		// We expect the win combo to be rated highest and the pieces to be rated near the bottom
		Random random = new XORShiftRandom(101010L);
		CardCatalogue.loadCardsFromPackage();
		List<Card> cardCatalogueRecords = CardCatalogue.getAll()
				.stream()
				.filter(Card::isCollectible)
				.filter(card -> !card.getCardId().equals("spell_win_the_game"))
				.filter(card -> !card.getCardId().equals("minion_combo_win_1"))
				.filter(card -> !card.getCardId().equals("minion_combo_win_2"))
				.filter(card -> card.getBaseManaCost() == 1)
				.filter(card -> card.getHeroClass().equals(HeroClass.ANY))
				.collect(toList());

		List<String> winningDeckList = new ArrayList<>();
		winningDeckList.add("minion_combo_win_1");
		winningDeckList.add("minion_combo_win_2");
		GameDeck winningDeck = new GameDeck(HeroClass.ANY, winningDeckList);
		List<GameDeck> losingDecks = Stream.generate(() ->
				new GameDeck(HeroClass.ANY,
						Collections.singletonList(cardCatalogueRecords.get(random.nextInt(cardCatalogueRecords.size())).getCardId())
				)).limit(20)
				.collect(toList());
		losingDecks.add(new GameDeck(HeroClass.ANY, Collections.singletonList("minion_combo_win_1")));
		losingDecks.add(new GameDeck(HeroClass.ANY, Collections.singletonList("minion_combo_win_2")));
		List<GameDeck> allDecks = new ArrayList<>(losingDecks);
		allDecks.add(winningDeck);
		final List<Map.Entry<GameDeck, Double>> unsortedResults = statsFromTournament(allDecks);
		final List<Map.Entry<GameDeck, Double>> results = unsortedResults.stream()
				.sorted(Comparator.comparingDouble(kv -> -kv.getValue()))
				.collect(toList());
		assertEquals(results.get(0).getKey(), winningDeck);
	}


	@Test
	@Ignore
	public void testFullRandomDecks() throws InterruptedException {
		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> decks = Stream.generate(() -> Deck.randomDeck(HeroClass.ANY, hearthstone))
				.limit(20)
				.collect(toList());
		for (int i = 0; i < 20; i++) {
			decks.get(i).setDeckId(i + "");
		}
		final List<Map.Entry<GameDeck, Double>> results = statsFromTournament(decks);
		for (Map.Entry<GameDeck, Double> kv : results) {
			System.out.println(kv.getKey().getDeckId() + " " + kv.getValue());
		}
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


	public List<Map.Entry<GameDeck, Double>> statsFromTournamentWithSpecificBehaviours(List<GameDeck> decks, IntelligentBehaviour playerBehaviour) throws InterruptedException {
		Map<GameDeck[], SimulationResult> resultList = new HashMap<>();
		// Make two bots that make random decisions and play multiple games (GAMES_PER_MATCH)
		for (GameDeck[] deckPairArray : GameContext.getDeckCombinations(decks, false)) {
			AtomicReference<SimulationResult> resultRef = new AtomicReference<>();
			GameContext.simulate(Arrays.asList(deckPairArray), Arrays.asList(() -> playerBehaviour, () -> playerBehaviour), GAMES_PER_MATCH, true,
					resultRef::set);

			SimulationResult result = resultRef.get();
			resultList.put(deckPairArray, result);
		}

		// Order the decks by average win rate
		List<Map.Entry<GameDeck, Double>> results = resultList.entrySet().stream()
				.flatMap(kv -> Stream.of(
						new TestDeckEvaluation.DeckWinrateTuple(kv.getKey()[0], kv.getValue().getPlayer1Stats().getDouble(Statistic.WIN_RATE)),
						new TestDeckEvaluation.DeckWinrateTuple(kv.getKey()[1], kv.getValue().getPlayer2Stats().getDouble(Statistic.WIN_RATE))
				))
				.collect(Collectors.groupingBy(TestDeckEvaluation.DeckWinrateTuple::getGameDeck, averagingDouble(TestDeckEvaluation.DeckWinrateTuple::getWinRate)))
				.entrySet()
				.stream()
				.collect(toList());
		return results;
	}

	@Test
	@Ignore
	public void testGeneratedDeck() throws InterruptedException {
		CardCatalogue.loadCardsFromPackage();

		GameDeck generatedDeck = new GameDeck(HeroClass.BLUE);

		generatedDeck.getCards().addCard("minion_river_crocolisk");
		generatedDeck.getCards().addCard("minion_river_crocolisk");
		generatedDeck.getCards().addCard("minion_bloodfen_raptor");
		generatedDeck.getCards().addCard("minion_grimscale_oracle");
		generatedDeck.getCards().addCard("minion_voodoo_doctor");
		generatedDeck.getCards().addCard("minion_gnomish_inventor");
		generatedDeck.getCards().addCard("minion_raid_leader");
		generatedDeck.getCards().addCard("minion_raid_leader");
		generatedDeck.getCards().addCard("minion_darkscale_healer");
		generatedDeck.getCards().addCard("minion_booty_bay_bodyguard");
		generatedDeck.getCards().addCard("minion_ironfur_grizzly");
		generatedDeck.getCards().addCard("minion_dragonling_mechanic");
		generatedDeck.getCards().addCard("minion_dragonling_mechanic");
		generatedDeck.getCards().addCard("minion_stormwind_champion");
		generatedDeck.getCards().addCard("spell_flamestrike");
		generatedDeck.getCards().addCard("minion_shattered_sun_cleric");
		generatedDeck.getCards().addCard("minion_lord_of_the_arena");
		generatedDeck.getCards().addCard("minion_lord_of_the_arena");
		generatedDeck.getCards().addCard("minion_frostwolf_warlord");
		generatedDeck.getCards().addCard("minion_frostwolf_warlord");
		generatedDeck.getCards().addCard("minion_goldshire_footman");
		generatedDeck.getCards().addCard("minion_nightblade");
		generatedDeck.getCards().addCard("spell_mirror_image");
		generatedDeck.getCards().addCard("minion_acidic_swamp_ooze");
		generatedDeck.getCards().addCard("minion_senjin_shieldmasta");
		generatedDeck.getCards().addCard("minion_water_elemental");
		generatedDeck.getCards().addCard("minion_gurubashi_berserker");
		generatedDeck.getCards().addCard("minion_ogre_magi");
		generatedDeck.getCards().addCard("minion_murloc_tidehunter");
		generatedDeck.getCards().addCard("minion_stormwind_champion");

		BasicTournamentDecks basicTournamentDecks = new BasicTournamentDecks();
		List<GameDeck> tournamentDecks = new ArrayList<>();
		tournamentDecks.add(basicTournamentDecks.getTrumpBasicPaladinDeck());
		tournamentDecks.add(basicTournamentDecks.getTrumpBasicPriestDeck());
		tournamentDecks.add(basicTournamentDecks.getTrumpBasicShamanDeck());
		tournamentDecks.add(generatedDeck);

		List<DecisionType> decisionTypes = new ArrayList<>();
		decisionTypes.add(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES);
		decisionTypes.add(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES);

		List<HashSet<String>> cardListForEachDecisionType = new ArrayList<>();
		NeverUseOnOwnMinion neverUseOnOwnMinion = new NeverUseOnOwnMinion();
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		cardListForEachDecisionType.add(neverUseOnEnemyMinions.classicAndBasicSets);
		cardListForEachDecisionType.add(neverUseOnOwnMinion.classicAndBasicSets);

		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes, cardListForEachDecisionType);

		final List<Map.Entry<GameDeck, Double>> unsortedResults = statsFromTournamentWithSpecificBehaviours(tournamentDecks, behaviour);
		final List<Map.Entry<GameDeck, Double>> results = unsortedResults.stream()
				.sorted(Comparator.comparingDouble(kv -> -kv.getValue()))
				.collect(toList());
		System.out.println(results);
	}
}
