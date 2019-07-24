package com.hiddenswitch.spellsource;

/**
 * The base cards module.
 *
 * @see AbstractCardResources for more about how this class is used.
 */
public final class BaseCardsModule extends CardsModule {

	@Override
	protected void configure() {
		add(BaseCardResources.class);
	}
}

