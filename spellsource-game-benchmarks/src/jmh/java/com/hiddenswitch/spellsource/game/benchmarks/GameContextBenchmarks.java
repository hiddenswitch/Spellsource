package com.hiddenswitch.spellsource.game.benchmarks;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class GameContextBenchmarks {

	@Setup
	public void setup() {
		CardCatalogue.loadCardsFromPackage();
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Measurement(time = 30, timeUnit = TimeUnit.SECONDS, iterations = 2)
	@OutputTimeUnit(TimeUnit.MINUTES)
	public void gameStateValueBehaviour() throws InterruptedException {
		var gameContext = GameContext.fromTwoRandomDecks(10101L);
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
