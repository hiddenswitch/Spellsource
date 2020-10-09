package com.hiddenswitch.spellsource.gameplaytest;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.tests.util.TestBase;

import java.util.concurrent.atomic.AtomicReference;

public class TestMain {

	public static GameContext runGym() {
		final TestBase testBase = new TestBase();
		AtomicReference<GameContext> gameContext = new AtomicReference<>();
		testBase.runGym((context, player, opponent) -> {
			gameContext.set(context);
		});
		return gameContext.get();
	}

	public static GameContext runGym(String friendlyClass, String enemyClass) {
		final TestBase testBase = new TestBase();
		if (friendlyClass == null || friendlyClass.equals("ANY")) {
			friendlyClass = testBase.getDefaultHeroClass();
		}
		if (enemyClass == null || enemyClass.equals("ANY")) {
			enemyClass = testBase.getDefaultHeroClass();
		}
		AtomicReference<GameContext> gameContext = new AtomicReference<>();
		testBase.runGym((context, player, opponent) -> {
			gameContext.set(context);
		}, friendlyClass, enemyClass);
		return gameContext.get();
	}
}
