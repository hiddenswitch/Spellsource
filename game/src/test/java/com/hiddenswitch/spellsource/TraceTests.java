package com.hiddenswitch.spellsource;

import com.google.common.io.Resources;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.RandomDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.Trace;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TraceTests {
	private static Map<String, Trace> traces;

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
		return traces.get(traceName + ".txt");
	}

	@Test
	@Ignore
	public void testTraceValid() {
		Player player1 = new Player(new RandomDeck(HeroClass.BLACK, DeckFormat.STANDARD));
		player1.setBehaviour(new PlayRandomBehaviour());
		Player player2 = new Player(new RandomDeck(HeroClass.BLACK, DeckFormat.STANDARD));
		player2.setBehaviour(new PlayRandomBehaviour());
		GameContext context1 = new GameContext(player1, player2, new GameLogic(), DeckFormat.STANDARD);
		context1.play();
		Trace trace = context1.getTrace();
		GameContext context2 = trace.replayContext(false);
		Assert.assertEquals(context1.getTurn(), context2.getTurn());
	}
}
