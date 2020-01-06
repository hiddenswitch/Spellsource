package com.hiddenswitch.spellsource.tests.cards;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.logic.Trace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TraceTests {
	private static Logger LOGGER = LoggerFactory.getLogger(TraceTests.class);

	public static Object[][] getTraces() {
		try (ScanResult scanResult = new ClassGraph()
				.disableRuntimeInvisibleAnnotations()
				.whitelistPaths(getDirectoryPrefix()).scan()) {
			List<Trace> traces = scanResult
					.getResourcesWithExtension(".json")
					.stream()
					.filter(Objects::nonNull)
					.map(c -> {
						try {
							Trace trace = Trace.load(c.getContentAsString());
							if (trace.getId() == null) {
								trace.setId(c.toString());
							}
							return trace;
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
	}

	private static String getDirectoryPrefix() {
		return "traces";
	}

	@BeforeAll
	public static void before() {
		CardCatalogue.loadCardsFromPackage();
	}

	@Test
	public void testTraceRecordedCorrectly() {
		IntStream.range(0, 100).parallel().unordered().forEach(ignored -> {
			Player player1 = new Player(Deck.randomDeck(), "Player 1");
			Player player2 = new Player(Deck.randomDeck(), "Player 2");
			GameContext context1 = new GameContext();
			context1.setDeckFormat(DeckFormat.getFormat("Custom"));
			context1.setPlayer(0, player1);
			context1.setPlayer(1, player2);
			context1.play();
			Trace trace = context1.getTrace();
			GameContext context2 = trace.replayContext(false, null);
			assertEquals(context1.getTurn(), context2.getTurn());
		});
	}

	@ParameterizedTest
	@MethodSource("getTraces")
	public void testTraces(Trace trace) {
		if ("ignore".equals(trace.getId())) {
			return;
		}
		GameContext context = trace.replayContext(false, null);
	}

	@Test
	@Disabled("diagnostic only")
	public void testDiagnoseTraces() {
		Multiset<String> cards = ConcurrentHashMultiset.create();
		IntStream.range(0, 10000).parallel().forEach(i -> {
			Player player1 = new Player(Deck.randomDeck());
			Player player2 = new Player(Deck.randomDeck());
			GameContext context1 = new GameContext();
			context1.setPlayer(0, player1);
			context1.setPlayer(1, player2);
			context1.setDeckFormat(DeckFormat.getFormat("Standard"));
			context1.play();
			Trace trace = context1.getTrace();
			try {
				GameContext context2 = trace.replayContext(false, null);
				assertEquals(context1.getTurn(), context2.getTurn());
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
