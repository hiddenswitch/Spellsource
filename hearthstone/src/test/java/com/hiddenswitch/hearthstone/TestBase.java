package com.hiddenswitch.hearthstone;

import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;

public class TestBase extends net.demilich.metastone.tests.util.TestBase {
	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.BLUE;
	}

	@Override
	public DeckFormat getDefaultFormat() {
		return DeckFormat.getFormat("Wild");
	}
}
