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

	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MassTest.class);

	@BeforeAll
	public static void loggerSetup() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);
	}

	/**
	 * Fuzzes the game by randomly playing random decks 1,000-10000}
	 */
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
				var path = FileSystems.getDefault().getPath("src", "test", "resources", "traces");
				if (!Files.exists(path)) {
					path = FileSystems.getDefault().getPath("cards", "src", "test", "resources", "traces");
				}
				if (!Files.exists(path)) {
					LOGGER.error("oneGame: Could not find directory: {}", path);
				}
				Files.write(path.resolve("masstest-trace-" + Instant.now().toString().replaceAll("[/\\\\?%*:|\".<>\\s]", "_") + ".json"), context.getTrace().dump().getBytes());
			} catch (IOException e) {
				LOGGER.error("oneGame:", e);
			}
			throw any;
		}
	}
}