package net.demilich.metastone.tests.util;

public class GymFactory {
	TestBase.GymConsumer first;
	TestBase.GymConsumer after = ((context, player, opponent) -> {
	});

	public void run(TestBase.GymConsumer consumer) {
		TestBase.runGym(first.andThen(consumer).andThen(after));
	}
}
