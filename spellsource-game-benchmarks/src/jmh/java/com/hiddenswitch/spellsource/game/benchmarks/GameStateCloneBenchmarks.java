package com.hiddenswitch.spellsource.game.benchmarks;

import com.hiddenswitch.spellsource.testutils.RandomDeck;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class GameStateCloneBenchmarks {
	private static final long BENCHMARK_SEED = 0x0C10E5EEDL;
	private static final int TARGET_TURN = 6;
	private static final int MAX_SEED_ATTEMPTS = 256;

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		GameContext midgameContext;

		@Setup(Level.Trial)
		public void setup() {
			ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
			var cardCatalogue = ClasspathCardCatalogue.INSTANCE;
			var format = Objects.requireNonNull(cardCatalogue.getFormat("Standard"), "Standard format not found");
			midgameContext = createStableMidgameContext(cardCatalogue, format);
		}
	}

	@Benchmark
	public int cloneMidgameGameContext(BenchmarkState benchmarkState) {
		var clone = benchmarkState.midgameContext.clone();
		return clone.getTurn() + clone.getEntities().mapToInt(entity -> entity.getId() == -1 ? 0 : 1).sum();
	}

	private static GameContext createStableMidgameContext(CardCatalogue cardCatalogue, DeckFormat format) {
		var seedSource = new XORShiftRandom(BENCHMARK_SEED);
		for (int attempt = 0; attempt < MAX_SEED_ATTEMPTS; attempt++) {
			try {
				return createMidgameContext(cardCatalogue, format, seedSource.nextLong());
			} catch (IllegalStateException ignored) {
			}
		}
		throw new IllegalStateException("Could not find a stable midgame benchmark seed");
	}

	private static GameContext createMidgameContext(CardCatalogue cardCatalogue, DeckFormat format, long seed) {
		var random = new XORShiftRandom(seed);
		var heroClasses = new ArrayList<>(cardCatalogue.getBaseClasses(format));
		var deck1 = new RandomDeck(random.nextLong(), heroClasses.get(random.nextInt(heroClasses.size())), format, cardCatalogue);
		var deck2 = new RandomDeck(random.nextLong(), heroClasses.get(random.nextInt(heroClasses.size())), format, cardCatalogue);
		var context = GameContext.fromDecks(Arrays.asList(deck1, deck2), new PlayRandomBehaviour(), new PlayRandomBehaviour(), cardCatalogue, format);
		context.setLogic(new GameLogic(random.nextLong()));
		context.init();

		while (context.getTurn() < TARGET_TURN && context.getStatus() == GameStatus.RUNNING) {
			context.startTurn(context.getActivePlayerId());
			while (context.takeActionInTurn()) {
				// advance the game naturally using the configured behaviours
			}
		}

		if (context.getStatus() != GameStatus.RUNNING) {
			throw new IllegalStateException("Benchmark seed did not produce a stable midgame state");
		}

		return context;
	}
}
