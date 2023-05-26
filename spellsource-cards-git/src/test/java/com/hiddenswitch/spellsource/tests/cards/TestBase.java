package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;

public class TestBase extends net.demilich.metastone.tests.util.TestBase {

	@Override
	public DeckFormat getDefaultFormat() {
		return CardCatalogue.classpath().spellsource();
	}

	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.TEST;
	}
}
