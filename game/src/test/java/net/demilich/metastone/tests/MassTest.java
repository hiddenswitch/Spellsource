package net.demilich.metastone.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.tests.util.TestBase;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.stream.IntStream;

public class MassTest extends TestBase {

	@BeforeTest
	private void loggerSetup() {
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
		GameContext context = GameContext.fromTwoRandomDecks();
		try {
			context.play();
		} catch (RuntimeException any) {
			try {
				Files.writeString(Path.of("masstest-trace-" + Instant.now().toString() + ".json"), context.getTrace().dump());
			} catch (IOException e) {
				return;
			}
			throw any;
		}

	}
}