package com.hiddenswitch.cluster.functions;

import com.hiddenswitch.cluster.models.TestConfig;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.MetaHero;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static java.util.stream.Collectors.toList;

public class DecksToGameConfigs implements PairFlatMapFunction<String[], TestConfig, GameConfig> {
	private int batches;
	private int gamesPerBatch;

	public DecksToGameConfigs() throws CardParseException, IOException, URISyntaxException {
	}

	public DecksToGameConfigs(int batches, int gamesPerBatch) throws IOException, URISyntaxException, CardParseException {
		this();
		this.batches = batches;
		this.gamesPerBatch = gamesPerBatch;
	}

	@Override
	public Iterator<Tuple2<TestConfig, GameConfig>> call(String[] decklists) throws Exception {
		CardCatalogue.loadCardsFromPackage();

		Deck testDeck = DeckCreateRequest.fromDeckList(decklists[0]).toGameDeck();
		Deck opponentDeck = DeckCreateRequest.fromDeckList(decklists[1]).toGameDeck();

		if (testDeck == null
				|| opponentDeck == null) {
			throw new NullPointerException();
		}

		GameConfig gameConfig = GameConfig.fromDecks(Arrays.stream(decklists)
				.map(DeckCreateRequest::fromDeckList)
				.map(DeckCreateRequest::toGameDeck)
				.collect(toList()));

		gameConfig.setNumberOfGames(gamesPerBatch);

		TestConfig testConfig = new TestConfig();
		testConfig.setDeckId1(testDeck.getName());
		testConfig.setDeckId2(opponentDeck.getName());

		// Create gameCount games to test
		ArrayList<Tuple2<TestConfig, GameConfig>> testConfigs = new ArrayList<>();
		for (int i = 0; i < batches; i++) {
			testConfigs.add(i, new Tuple2<>(testConfig, gameConfig.clone()));
		}

		return testConfigs.iterator();
	}
}
