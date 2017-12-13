package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KoboldsAndCatacombsTests extends TestBase {
	@Test
	public void testIronwoodGolem() {
		runGym((context, player, opponent) -> {
			Minion ironwoodGolem = playMinionCard(context,player,"minion_ironwood_golem");
			context.endTurn();
			context.endTurn();
			Assert.assertFalse(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_claw");
			Assert.assertFalse(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_claw");
			Assert.assertTrue(ironwoodGolem.canAttackThisTurn());
			context.endTurn();
			playCardWithTarget(context,opponent,"spell_fireball", player.getHero());
			context.endTurn();
			Assert.assertFalse(ironwoodGolem.canAttackThisTurn());
		});
	}

	@Test
	public void testJasperSpellstone() {
		runGym((context, player, opponent) -> {
			/*
			Spellstones accumulate progress...
			 */
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_lesser_jasper_spellstone"));
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
		});
	}

	@Test
	public void testAstralTigerVersusMalorne() {
		runGym((context, player, opponent) -> {
			/*
			  Astral Tiger shuffles a COPY. If you play cards that trigger a deathrattle, this
			  will put a copy on to your deck without removing the current one.
			  (Malorne's says "Shuffle this minion in to your deck" so it literally puts that specific minion from the
			  board in to your deck)
			 */

			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_astral_tiger")).count(), 0L);
			Minion astralTiger = playMinionCard(context, player, "minion_astral_tiger");
			playCardWithTarget(context, player, "spell_play_dead", astralTiger);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_astral_tiger");
			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_astral_tiger")).count(), 1L);
		});

		runGym((context, player, opponent) -> {
			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_malorne")).count(), 0L);
			Minion malorne = playMinionCard(context, player, "minion_malorne");
			playCardWithTarget(context, player, "spell_play_dead", malorne);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_malorne")).count(), 1L);
		});
	}
}
