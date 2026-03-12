package com.hiddenswitch.spellsource.game.benchmarks;

import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.tests.util.TestBase;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Fork(1)
public class GameContextBenchmarks {

	@Setup
	public void setup() {
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Warmup(iterations = 1, time = 30, timeUnit = TimeUnit.SECONDS)
	@Measurement(time = 60, timeUnit = TimeUnit.SECONDS, iterations = 3)
	@OutputTimeUnit(TimeUnit.MINUTES)
	public void gameStateValueBehaviour() throws InterruptedException {
		var gameContext = TestBase.fromTwoRandomDecks(10101L);
		gameContext.setBehaviour(0, new GameStateValueBehaviour()
				.setThrowsExceptions(false)
				.setMaxDepth(2)
				.setParallel(false)
				.setTimeout(0)
				.setLethalTimeout(0));
		gameContext.setBehaviour(1, new GameStateValueBehaviour()
				.setThrowsExceptions(false)
				.setMaxDepth(2)
				.setParallel(false)
				.setTimeout(0)
				.setLethalTimeout(0));
		gameContext.play();
	}
}
