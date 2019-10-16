package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ArchaeologistTests extends TestBase {
	@Test
	public void testArchivistKrag() {
		runGym((context, player, opponent) -> {
			Card lunstone1 = shuffleToDeck(context, player, DeckFormat.spellsource().getSecondPlayerBonusCards()[0]);
			lunstone1.setAttribute(Attribute.STARTED_IN_DECK);
			Card lunstone2 = shuffleToDeck(context, player, DeckFormat.spellsource().getSecondPlayerBonusCards()[0]);
			player.setMana(9);
			playCard(context, player, "minion_archivist_krag");
			Assert.assertEquals(player.getMana(), 1, "played 1 lunstone");
			Assert.assertEquals(player.getDeck().size(), 1);
		});
	}
}
