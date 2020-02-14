package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
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

	@Test
	public void testSpiritFromLongPast() {
		runGym((context, player, opponent) -> {
			destroy(context, playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId()));
			playCard(context, player, "minion_spirit_from_long_past");
			assertEquals(3, player.getGraveyard().size(), "should contain two cards and the minion");
			context.endTurn();
			context.endTurn();
			assertEquals(1, player.getGraveyard().size(), "should have drawn the 1/1 neutral out of the graveyard - both it and its source card");
			assertEquals(CardCatalogue.getOneOneNeutralMinionCardId(), player.getHand().get(0).getCardId(), "should have drawn 1/1 neutral");
		});
	}

	@Test
	public void testGravedig() {
		runGym((context, player, opponent) -> {
			destroy(context, playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId()));
			assertEquals(2, player.getGraveyard().size(), "should contain both the card and the minion");
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(1, discoverActions.size());
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_gravedig");
			assertEquals(1, player.getHand().size());
			assertEquals(CardCatalogue.getOneOneNeutralMinionCardId(), player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSandFilter() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_sand_filter");
			context.endTurn();
			assertEquals(3, player.getWeaponZone().get(0).getDurability());
			overrideDiscover(context, player, discoverActions -> {
				fail();
				return discoverActions.get(0);
			});
			playCard(context, opponent, "minion_shady_stranger");
		});
	}
}
