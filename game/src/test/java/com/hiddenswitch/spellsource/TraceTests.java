package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.io.Resources;
import com.hiddenswitch.spellsource.util.Logging;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.RandomDeck;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.Trace;
import org.apache.commons.lang3.RandomUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class TraceTests {
	private static Map<String, Trace> traces;
	private static Logger LOGGER = LoggerFactory.getLogger(TraceTests.class);

	@BeforeClass
	public static void before() {
		CardCatalogue.loadCardsFromPackage();
	}

	private static Trace getTrace(String traceName) {
		if (traces == null) {
			traces = new Reflections("traces", new ResourcesScanner())
					.getResources(x -> true)
					.stream()
					.filter(Objects::nonNull)
					.map(Resources::getResource)
					.filter(Objects::nonNull)
					.collect(
							Collectors.toMap(
									c -> Paths.get(c.getPath()).getFileName().toString(),
									c -> {
										try {
											return Trace.load(Resources.toString(c, Charset.defaultCharset()));
										} catch (IOException e) {
											return new Trace();
										}
									})
					);
		}
		return traces.get(traceName + ".json");
	}

	@Test
	public void testTraceValid() {
		IntStream.range(0, 100).parallel().unordered().forEach(ignored -> {
			Player player1 = new Player(new RandomDeck(), "Player 1");
			Player player2 = new Player(new RandomDeck(), "Player 2");
			GameContext context1 = new GameContext(player1, player2, new GameLogic(), DeckFormat.CUSTOM);
			context1.play();
			Trace trace = context1.getTrace();
			GameContext context2 = trace.replayContext(false, null);
			Assert.assertEquals(context1.getTurn(), context2.getTurn());
		});
	}

	@Test
	@Ignore("out of date")
	public void testFinleyShouldNotChangeHeroPowerToMinion() {
		Trace noHeroPower = getTrace("noheropower");
		GameContext context = noHeroPower.replayContext(false, null);
	}


	@Test
	@Ignore("out of date")
	public void testSuccessfulMagnetize() {
		Trace trace = getTrace("magnetize1");
		GameContext context = trace.replayContext(false, null);
	}

	@Test
	public void testTrace1() {
		Trace trace = getTrace("trace1");
		GameContext context = trace.replayContext(false, null);
	}

	@Test
	public void testTrace2() {
		Trace trace = getTrace("trace2");
		GameContext context = trace.replayContext(false, null);
	}

	@Test
	public void testTrace3() {
		Trace trace = getTrace("trace3");
		GameContext context = trace.replayContext(false, null);
	}

	@Test
	@Ignore("out of date")
	public void testShouldNotSummonEvilLaughter() {
		Trace summoningEvilLaughter = getTrace("summoningevillaughter");
		GameContext context = summoningEvilLaughter.replayContext(false, null);
	}

	@Test
	@Ignore("out of date")
	public void testDiagnoseTraces() {
		Multiset<String> cards = ConcurrentHashMultiset.create();
		IntStream.range(0, 10000).parallel().forEach(i -> {
			List<HeroClass> classes = Arrays.stream(HeroClass.values()).filter(HeroClass::isBaseClass).collect(toList());
			HeroClass heroClass1 = classes.get(RandomUtils.nextInt(0, classes.size()));
			HeroClass heroClass2 = classes.get(RandomUtils.nextInt(0, classes.size()));
			Player player1 = new Player(new RandomDeck(heroClass1, DeckFormat.STANDARD));
			Player player2 = new Player(new RandomDeck(heroClass2, DeckFormat.STANDARD));
			GameContext context1 = new GameContext(player1, player2, new GameLogic(), DeckFormat.STANDARD);
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
