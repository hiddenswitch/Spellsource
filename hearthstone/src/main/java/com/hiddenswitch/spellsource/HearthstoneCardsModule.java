package com.hiddenswitch.spellsource;

public final class HearthstoneCardsModule extends CardsModule {

	@Override
	protected void configure() {
		add(HearthstoneCardResources.class);
	}
}
