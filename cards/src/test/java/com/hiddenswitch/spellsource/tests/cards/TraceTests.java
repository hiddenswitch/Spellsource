package com.hiddenswitch.spellsource.tests.cards;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Attribute;
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
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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

			Collections.reverse(traces);

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

	/**
	 * Tests that traces are recorded correctly, i.e., that the game is executed deterministically.
	 * <p>
	 * Some common issues to diagnose new code that breaks on this test:
	 * <ul>
	 *   <li><b>Appropriate methods should be marked as {@code @Suspendable}</b>. Commonly this issue crops up in
	 *   authoring new {@link net.demilich.metastone.game.spells.Spell} classes, because those
	 *   {@code onCast(GameContext, Player, SpellDesc, Entity, Entity)} implementations must be suspendable.</li>
	 *   <li><b>Using {@link java.util.Random}</b>. You should only use the random methods from
	 *   {@link net.demilich.metastone.game.logic.GameLogic}, like {@link net.demilich.metastone.game.logic.GameLogic#getRandom(List)}</li>.
	 *   <li><b>Using state that is not deterministic.</b> For example, you should not use non-deterministic objects or
	 *   fields in {@link net.demilich.metastone.game.spells.Spell} classes.</li>
	 * </ul>
	 */
	@Test
	public void testTraceRecordedCorrectlyAndGameIsDeterministic() {
		IntStream.range(0, 1000).parallel().unordered().forEach(ignored -> {
			GameContext context1 = GameContext.fromTwoRandomDecks(DeckFormat.spellsource());
			context1.play();
			Trace trace = context1.getTrace().clone();
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
			GameContext context1 = GameContext.fromTwoRandomDecks(DeckFormat.spellsource());
			context1.play();
			Trace trace = context1.getTrace().clone();
			GameContext context2 = trace.replayContext(false, null);
			if (context1.getTurn() != context2.getTurn()) {
				LOGGER.info("trace failed");
				cards.addAll(context1.getEntities().filter(e -> e.getEntityType().equals(EntityType.CARD) && e.hasAttribute(Attribute.STARTED_IN_DECK)
						|| e.hasAttribute(Attribute.STARTED_IN_HAND)).map(Entity::getSourceCard).map(Card::getCardId).collect(toList()));

				for (var j = 1; j < Math.min(context1.getTrace().getActions().size(), context2.getTrace().getActions().size()); j++) {
					if (!context1.getTrace().getActions().get(j).equals(context2.getTrace().getActions().get(j))) {
						var gameAction1 = context1.getTrace().getRawActions().get(j - 1);
						var gameAction2 = context1.getTrace().getRawActions().get(j);
						var gameAction3 = context2.getTrace().getRawActions().get(j - 1);
						var gameAction4 = context2.getTrace().getRawActions().get(j);
						LOGGER.info("diverging action: \n'{}' to '{}'\n'{}' to '{}'",
								gameAction1.getDescription(context1, 0),
								gameAction2.getDescription(context1, 0),
								gameAction3.getDescription(context2, 0),
								gameAction4.getDescription(context2, 0));
						break;
					}
				}
			}

		});
		// Find the card which most frequently appear in the bad set
		List<String> res = cards.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getCount(), e1.getCount())).map(Multiset.Entry::toString).collect(toList());
		LOGGER.info("{}", res);
		if (!cards.isEmpty()) {
			fail("Invalid trace");
		}
	}

}
