package com.hiddenswitch.spellsource;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.io.Resources;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.logic.Trace;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class TraceTests {
	private static Logger LOGGER = LoggerFactory.getLogger(TraceTests.class);

	@DataProvider(name = "Traces")
	public static Object[][] getTraces() {
		List<Trace> traces = new Reflections("traces", new ResourcesScanner())
				.getResources(x -> true)
				.stream()
				.filter(Objects::nonNull)
				.map(Resources::getResource)
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(URL::getFile).reversed())
				.map(c -> {
					try {
						return Trace.load(Resources.toString(c, Charset.defaultCharset()));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).collect(toList());

		Object[][] data = new Object[traces.size()][1];
		for (int i = 0; i < traces.size(); i++) {
			data[i][0] = traces.get(i);
		}
		return data;
	}

	@BeforeClass
	public static void before() {
		CardCatalogue.loadCardsFromPackage();
	}

	@Test
	public void testTraceRecordedCorrectly() {
		IntStream.range(0, 100).parallel().unordered().forEach(ignored -> {
			Player player1 = new Player(Deck.randomDeck(), "Player 1");
			Player player2 = new Player(Deck.randomDeck(), "Player 2");
			GameContext context1 = new GameContext();
			context1.setDeckFormat(DeckFormat.CUSTOM);
			context1.setPlayer(0, player1);
			context1.setPlayer(1, player2);
			context1.play();
			Trace trace = context1.getTrace();
			GameContext context2 = trace.replayContext(false, null);
			Assert.assertEquals(context1.getTurn(), context2.getTurn());
		});
	}

	@Test(dataProvider = "Traces")
	public void testTraces(Trace trace) {
		GameContext context = trace.replayContext(false, null);
	}

	@Test
	@Ignore("diagnostic only")
	public void testDiagnoseTraces() {
		Multiset<String> cards = ConcurrentHashMultiset.create();
		IntStream.range(0, 10000).parallel().forEach(i -> {
			Player player1 = new Player(Deck.randomDeck());
			Player player2 = new Player(Deck.randomDeck());
			GameContext context1 = new GameContext();
			context1.setPlayer(0, player1);
			context1.setPlayer(1, player2);
			context1.setDeckFormat(DeckFormat.STANDARD);
			context1.play();
			Trace trace = context1.getTrace();
			try {
				GameContext context2 = trace.replayContext(false, null);
				Assert.assertEquals(context1.getTurn(), context2.getTurn());
			} catch (Throwable ex) {
				// Inspect the trace and observe which cards were played that could have caused the error
				cards.addAll(context1.getEntities().filter(e -> e.getEntityType().equals(EntityType.CARD)).map(Entity::getSourceCard).map(Card::getCardId).collect(toList()));
			}
		});
		// Find the card which most frequently appear in the bad set
		List<String> res = cards.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getCount(), e1.getCount())).map(Multiset.Entry::toString).collect(toList());
		LOGGER.info("{}", res);
	}

}
