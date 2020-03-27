package com.hiddenswitch.spellsource.tests.cards;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.Instant;
import java.util.stream.IntStream;

public class MassTest extends TestBase {

	@BeforeAll
	public static void loggerSetup() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);
	}

	@Test
	public void testRandomMassPlay() {
		loggerSetup();
		int tests = Boolean.parseBoolean(System.getenv("CI")) ? 1000 : 10000;
		IntStream.range(0, tests).parallel().forEach(i -> oneGame());
	}

	private void oneGame() {
		GameContext context = GameContext.fromTwoRandomDecks(DeckFormat.spellsource());
		try {
			context.play();
		} catch (RuntimeException any) {
			try {
				Files.write(FileSystems.getDefault().getPath("src", "test", "resources", "traces", "masstest-trace-" + Instant.now().toString().replaceAll("[/\\\\?%*:|\".<>\\s]", "_") + ".json"), context.getTrace().dump().getBytes());
			} catch (IOException e) {
				return;
			}
			throw any;
		}
	}
}