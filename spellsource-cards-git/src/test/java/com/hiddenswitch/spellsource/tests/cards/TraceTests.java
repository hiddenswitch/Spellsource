package com.hiddenswitch.spellsource.tests.cards;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.Trace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Execution(ExecutionMode.CONCURRENT)
public class TraceTests extends TestBase {
	private static Logger LOGGER = LoggerFactory.getLogger(TraceTests.class);
	private static ClasspathCardCatalogue cardCatalogue;

	public static Object[][] getTraces() {
		try (ScanResult scanResult = new ClassGraph()
				.disableRuntimeInvisibleAnnotations()
				.acceptPaths(getDirectoryPrefix()).scan()) {
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
		cardCatalogue = ClasspathCardCatalogue.INSTANCE;
		cardCatalogue.loadCardsFromPackage();
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
	 *   <li><b>Mutate {@link net.demilich.metastone.game.cards.desc.Desc} objects.</b> For example, you retrieve
	 *   an array of {@code SpellDesc} using {@code (SpellDesc[])desc.get(SpellArg.SPELLS)} and mutate the array,
	 *   replacing elements inside of it thereby changing the card for all subsequent games.</li>
	 * </ul>
	 */
	@RepeatedTest(10000)
	public void testTraceRecordedCorrectlyAndGameIsDeterministic() {
		assertTimeoutPreemptively(Duration.ofMillis(10000), () -> {
			GameContext context1 = fromTwoRandomDecks(cardCatalogue.spellsource(), cardCatalogue);
			context1.play();
			Trace trace = context1.getTrace().clone();
			GameContext context2 = trace.replayContext();
			assertEquals(context1.getTurn(), context2.getTurn());
			return null;
		}, "timeout");
	}

	@ParameterizedTest
	@MethodSource("getTraces")
	@Execution(ExecutionMode.SAME_THREAD)
	public void testTraces(Trace trace) {
		if ("ignore".equals(trace.getId())) {
			return;
		}

		TestBase.assertTimeoutPreemptively(Duration.ofMillis(20000), () -> {
			var traceClone = trace.clone();
			var traceCardCatalogue = ClasspathCardCatalogue.INSTANCE;
			if (trace.getVersion() >= 5) {
				traceCardCatalogue = cardCatalogue;
			}
			var context1 = trace.replayContext(false, null, traceCardCatalogue);
			var context2 = traceClone.replayContext(false, null, traceCardCatalogue);
			assertEquals(context1.getTurn(), context2.getTurn(), diagnose(context1, context2));
			return null;
		}, "timeout");
	}

	private String diagnose(GameContext context1, GameContext context2) {
		for (var j = 1; j < Math.min(context1.getTrace().getActions().size(), context2.getTrace().getActions().size()); j++) {
			if (!context1.getTrace().getRawActions().get(j).equals(context2.getTrace().getRawActions().get(j))) {
				var gameAction1 = context1.getTrace().getRawActions().get(j - 1);
				var gameAction2 = context1.getTrace().getRawActions().get(j);
				var gameAction3 = context2.getTrace().getRawActions().get(j - 1);
				var gameAction4 = context2.getTrace().getRawActions().get(j);
				return String.format("A diverging outcome was observed between a game context and its trace between actions %d and %d:" +
								"\nContext: \n'%s' to '%s'\nTrace:\n'%s' to '%s'",
						j - 1, j,
						gameAction1.getDescription(context1, 0),
						gameAction2.getDescription(context1, 0),
						gameAction3.getDescription(context2, 0),
						gameAction4.getDescription(context2, 0));
			}
		}
		return "No issues detected";
	}

	/**
	 * This test can be used to find source cards that cause issues in game reproducibility.
	 */
	@Test
	@Disabled
	public void testDiagnoseTraces() {
		Multiset<String> cards = ConcurrentHashMultiset.create();
		IntStream.range(0, 10000).parallel().forEach(i -> {
			GameContext context1 = fromTwoRandomDecks(cardCatalogue.spellsource(), cardCatalogue);
			context1.play();
			Trace trace = context1.getTrace().clone();
			GameContext context2 = trace.replayContext();
			if (context1.getTurn() != context2.getTurn()) {
				LOGGER.info("A failed trace was observed, its cards will be added to a list of possibility problematic cards.");
				cards.addAll(context1.getEntities().filter(e -> e.getEntityType().equals(EntityType.CARD) && e.hasAttribute(Attribute.STARTED_IN_DECK)
						|| e.hasAttribute(Attribute.STARTED_IN_HAND)).map(Entity::getSourceCard).map(Card::getCardId).toList());
				LOGGER.info(diagnose(context1, context2));
			}

		});
		// Find the card which most frequently appear in the bad set
		var entries = new ArrayList<>(cards.entrySet());
		entries.sort(Comparator.comparing((Multiset.Entry e) -> e.getCount()).reversed());
		if (!entries.isEmpty()) {
			LOGGER.error("Cards which appeared in decks with reproducibility issues:");
			for (var entry : entries) {
				try {
					LOGGER.error("{}x: {} ({})", entry.getCount(), entry.getElement(), cardCatalogue.getCardById(entry.getElement()).getDescription());
				} catch (Throwable ignored) {
				}
			}
		}
		if (!cards.isEmpty()) {
			fail("Invalid trace");
		}
	}

}
