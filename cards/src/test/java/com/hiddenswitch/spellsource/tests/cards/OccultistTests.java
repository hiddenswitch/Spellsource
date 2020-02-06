package com.hiddenswitch.spellsource.tests.cards;

import co.paralleluniverse.common.util.Objects;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OccultistTests extends TestBase {

	@Test
	public void testRevelation() {
		for (int j = 0; j < 10; j++) {
			final int k = j;
			runGym((context, player, opponent) -> {
				for (int i = 0; i < k; i++) {
					context.endTurn();
					context.endTurn();
				}

				Card card = receiveCard(context, player, "spell_lunstone");
				playCard(context, player, "spell_revelation");
				assertEquals(player.getMana(), 0);
				assertEquals(player.getMaxMana(), 0);
				assertEquals(costOf(context, player, card), 1);
			});
		}

	}

	@Test
	public void testImperfectDuplicate() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			assertEquals(target.getSourceCard().getBaseManaCost(), 1);
			playCard(context, player, "spell_imperfect_duplicate", target);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getAttack(), 1);
			assertEquals(player.getMinions().get(1).getHp(), 1);
			assertNotEquals(player.getMinions().get(1).getSourceCard().getCardId(), CardCatalogue.getOneOneNeutralMinionCardId());
		});
	}

	@Test
	public void testUnearthedHorrorXitaluInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_xitalu");
			Minion target = playMinionCard(context, player, "minion_unearthed_horror");
			playCard(context, player, "spell_underwater_horrors", target);
			assertEquals(player.getDeck().size(), 3);
			for (Card card : player.getDeck()) {
				assertEquals(card.getBonusAttack(), 7);
				assertEquals(card.getBonusHp(), 7);
			}
		});
	}

	@Test
	public void testGhatanothoa() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "token_ghatanothoa");
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			String firstHp = player.getHeroPowerZone().get(0).getCardId();
			target.setMaxHp(5);
			target.setHp(3);
			player.setMana(GameLogic.MAX_MANA);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> Objects.equal(ga.getSourceReference(), player.getHeroPowerZone().get(0).getReference())));
			useHeroPower(context, player, target.getReference());
			assertTrue(target.getHp() == 1 || target.getHp() == 5);
			String secondHp = player.getHeroPowerZone().get(0).getCardId();
			assertNotEquals(firstHp, secondHp);
			player.setMana(GameLogic.MAX_MANA);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> Objects.equal(ga.getSourceReference(), player.getHeroPowerZone().get(0).getReference())));
		});
	}

	@Test
	public void testLostCitysGuardian() {
		for (int i = 0; i < 5; i++) {
			runGym((context, player, opponent) -> {
				shuffleToDeck(context, player, "minion_neutral_test_1");
				shuffleToDeck(context, player, "minion_neutral_test");
				shuffleToDeck(context, player, "minion_neutral_test_big");

				overrideDiscover(context, player, "minion_neutral_test");
				playCard(context, player, "spell_lost_city_champion");
				assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_neutral_test");
				assertTrue(player.getMinions().get(0).hasAttribute(Attribute.TAUNT));
			});
		}
	}

	@Test
	public void testStarSculptor() {
		runGym((context, player, opponent) -> {
			Minion test = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_star_sculptor", test);
			assertEquals(3, player.getMinions().size());
			assertFalse(test.hasAttribute(Attribute.TAUNT));
			assertTrue(player.getMinions().get(2).hasAttribute(Attribute.TAUNT));
		});
	}
}
