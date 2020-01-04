package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArchaeologistTests extends TestBase {
	@Test
	public void testArchivistKrag() {
		runGym((context, player, opponent) -> {
			Card lunstone1 = shuffleToDeck(context, player, DeckFormat.spellsource().getSecondPlayerBonusCards()[0]);
			lunstone1.setAttribute(Attribute.STARTED_IN_DECK);
			Card lunstone2 = shuffleToDeck(context, player, DeckFormat.spellsource().getSecondPlayerBonusCards()[0]);
			player.setMana(9);
			playCard(context, player, "minion_archivist_krag");
			assertEquals(player.getMana(), 1, "played 1 lunstone");
			assertEquals(player.getDeck().size(), 1);
		});
	}
}
