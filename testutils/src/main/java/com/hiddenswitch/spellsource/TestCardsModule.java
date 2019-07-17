package com.hiddenswitch.spellsource;

public final class TestCardsModule extends CardsModule {

	@Override
	protected void configure() {
		add(TestCardResources.class);
	}
}

