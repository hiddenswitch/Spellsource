package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.logic.Trace;
import org.junit.jupiter.api.RepeatedTest;
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

/**
 * Performs fuzzing of game contexts for Spellsource.
 */
@Execution(ExecutionMode.CONCURRENT)
public class MassTest extends TestBase {

	private static final Path path1 = FileSystems.getDefault().getPath("src", "test", "resources", "traces");
	private static final Path path2 = FileSystems.getDefault().getPath("cards", "src", "test", "resources", "traces");

	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MassTest.class);

	/**
	 * Fuzzes the game by randomly playing decks 10,000 times.
	 * <p>
	 * The game is given a maximum of 10,000 milliseconds to execute.
	 */
	@RepeatedTest(value = 10000)
	public void testRandomMassPlay() {
        GameContext context = TestBase.fromTwoRandomDecks(ClasspathCardCatalogue.CLASSPATH.spellsource());
		try {
			assertTimeoutPreemptively(Duration.ofMillis(10000), () -> {
						context.play();
						return null;
					},
					"To diagnose this issue, the trace will be played back with a lower timeout and will throw again. In" +
							" a debugger, examine the future.task object in the call stack of the test executor, and get its stack" +
							" trace using getStackTrace(). This will show you where the game context is stuck.");
		} catch (Throwable any) {
			var trace = context.getTrace();
			var isCi = Boolean.parseBoolean(System.getenv().getOrDefault("CI", "false"));
			if (isCi) {
				LOGGER.error("To diagnose this issue, a trace (a completely reproducible replay of the game that fails) is printing now:\n{}", trace.dump());
			} else {
				LOGGER.error("To diagnose this issue, a trace (a completely reproducible replay of the game that fails) has been" +
						" saved into cards/src/test/resources/traces. You can replay this trace using TraceTests.");
				saveTraceWithException(trace, path1, path2);
			}
			throw any;
		}
	}

	/**
	 * Saves a trace to a path, trying to find a path to a directory that exists before attempting to save there.
	 *
	 * @param trace
	 * @param paths
	 */
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