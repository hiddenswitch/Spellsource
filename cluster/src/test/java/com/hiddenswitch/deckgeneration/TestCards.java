package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.Assert;
import org.testng.annotations.Test;

public class TestCards extends TestBase {
	@Test
	public void testWinWhenStartsInHand() {
		CardCatalogue.loadCardsFromPackage();

		DebugContext context = createContext(HeroClass.BLUE, HeroClass.BLUE, false, DeckFormat.CUSTOM);
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayer1().getHand().add(CardCatalogue.getCardById("spell_win_when_starts_in_hand"));

		context.init();
		Assert.assertTrue(context.getPlayer2().getHero().isDestroyed());
	}
}
