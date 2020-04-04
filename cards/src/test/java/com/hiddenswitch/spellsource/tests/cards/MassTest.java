package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.Trace;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@Execution(ExecutionMode.CONCURRENT)
public class MassTest extends TestBase {

	private static final Path path1 = FileSystems.getDefault().getPath("src", "test", "resources", "traces");
	private static final Path path2 = FileSystems.getDefault().getPath("cards", "src", "test", "resources", "traces");

	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MassTest.class);

	/**
	 * Fuzzes the game by randomly playing random decks 1,000-10000}
	 */
	@RepeatedTest(value = 3000)
	public void testRandomMassPlay() {
		GameContext context = GameContext.fromTwoRandomDecks(DeckFormat.spellsource());
		try {
			assertTimeoutPreemptively(Duration.ofMillis(4200), (Executable) context::play);
		} catch (Throwable any) {
			var trace = context.getTrace();
			saveTraceWithException(trace, path1, path2);
			throw any;
		}
	}

	public static void saveTraceWithException(Trace trace, Path... paths) {
		try {
			if (paths.length == 0) {
				return;
			}
			var i = 0;
			var path = paths[0];
			while (!Files.exists(path) && i + 1 < paths.length) {
				i++;
				path = paths[i];
			}

			if (!Files.exists(path)) {
				LOGGER.error("saveTrace: Could not find directory: {}", path);
			}

			var filename = "masstest-trace-" + Instant.now().toString().replaceAll("[/\\\\?%*:|\".<>\\s]", "_") + ".json";
			Files.write(path.resolve(filename), trace.dump().getBytes());
		} catch (IOException e) {
			LOGGER.error("saveTrace:", e);
		}
	}
}