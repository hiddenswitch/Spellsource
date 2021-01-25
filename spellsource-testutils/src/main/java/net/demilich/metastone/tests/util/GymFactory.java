package net.demilich.metastone.tests.util;

public class GymFactory {
	private TestBase testBase;

	public GymFactory(TestBase testBase) {
		this.testBase = testBase;
	}

	TestBase.GymConsumer first;
	TestBase.GymConsumer after = ((context, player, opponent) -> {
	});

	public void run(TestBase.GymConsumer consumer) {
		testBase.runGym(first.andThen(consumer).andThen(after));
	}
}
