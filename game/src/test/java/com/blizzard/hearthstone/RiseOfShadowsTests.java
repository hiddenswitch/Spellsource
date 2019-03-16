package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RiseOfShadowsTests extends TestBase {

	@Test
	public void testTwinSpell() {
		runGym((context, player, opponent) -> {
			Card aid = receiveCard(context, player, "spell_the_forests_aid");
			playCard(context, player, aid);
			assertEquals(player.getHand().size(), 1);
			aid = player.getHand().get(0);
			assertFalse(aid.getDescription().contains("Twinspell"));
			assertFalse(aid.hasAttribute(Attribute.TWINSPELL));
			playCard(context, player, aid);

			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testHagathasScheme() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				receiveCard(context, player, "spell_hagathas_scheme");
			}
			Minion gargoyle = playMinionCard(context, opponent, "minion_stoneskin_gargoyle");
			for (int i = 1; i <= 4; i++) {
				Card card = player.getHand().get(0);
				assertTrue(card.getDescription(context, player).contains("Deal " + i + " damage"));
				playCard(context, player, card);
				assertEquals(gargoyle.getHp(), 4 - i);
				context.endTurn();
				context.endTurn();
			}
		});
	}

	@Test
	public void testTogwagglesScheme() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				receiveCard(context, player, "spell_togwaggles_scheme");
			}
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			for (int i = 1; i <= 4; i++) {
				Card card = player.getHand().get(0);
				assertTrue(card.getDescription(context, player).contains("Shuffle " + i));
				int deckCount = player.getDeck().getCount();
				playCard(context, player, card, wisp);
				assertEquals(player.getDeck().size(), deckCount + i);
				context.endTurn();
				context.endTurn();
			}
		});

	}

	@Test
	public void testEvilMiscreant() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_evil_miscreant");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "minion_evil_miscreant");
			assertEquals(player.getHand().size(), 2);
			for (int i = 0; i < 1; i++) {
				assertTrue(player.getHand().get(i).hasAttribute(Attribute.LACKEY));
			}
		});

	}
}
