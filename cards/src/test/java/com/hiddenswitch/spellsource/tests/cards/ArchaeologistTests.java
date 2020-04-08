package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.minions.Minion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
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
			assertEquals(2, player.getGraveyard().size(), "should have drawn the 1/1 neutral out of the graveyard, but now the neutral will not be eligible to draw again");
			assertEquals(CardCatalogue.getOneOneNeutralMinionCardId(), player.getHand().get(0).getCardId(), "should have drawn 1/1 neutral");
			assertEquals(1, player.getHand().size(), "contains the one card");
			context.endTurn();
			context.endTurn();
			assertEquals(1, player.getHand().size(), "no cards left to draw");
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

	@Test
	public void testDynoblow() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_dynoblow");
			assertEquals(testMinion.getBaseHp() - 8, testMinion.getHp()); //only was damaged the first time
		});
	}

	@Test
	public void testTaletellers() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "token_ember_elemental");
			playCard(context, player, "token_ember_elemental");
			Minion poorGuy = playMinionCard(context, player, "token_ember_elemental");
			destroy(context, poorGuy);
			playCard(context, player, "minion_taletellers");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testGoldRush() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_gold_rush");
			for (int i = 0; i < 8; i++) {
				assertEquals(0, costOf(context, player, player.getDeck().get(i)));
			}
		});
	}
}
