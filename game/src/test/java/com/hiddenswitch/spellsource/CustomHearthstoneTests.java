package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class CustomHearthstoneTests extends TestBase {
	@Test
	public void testMysticSkull() {
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_mystic_skull", bloodfenRaptor);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			Minion newBloodfenRaptor = playMinionCard(context, player, (MinionCard) player.getHand().get(0));
			Assert.assertEquals(newBloodfenRaptor.getAttack(), 5);
		});
	}

	@Test
	public void testGiantDisappointment() {
		runGym((context, player, opponent) -> {
			Card card = CardCatalogue.getCardById("minion_giant_disappointment");
			context.getLogic().receiveCard(player.getId(), card);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, card), 8);
		});
	}

	@Test
	public void testPowerTrip() {
		// We reach turn 10 so we have 10 mana, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 10; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Our opponent gives us 10 mana somehow, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 2; i++) {
				context.endTurn();
				context.endTurn();
			}
			context.endTurn();
			Assert.assertEquals(player.getMaxMana(), 3);
			for (int i = 0; i < 7; i++) {
				playCard(context, opponent, "minion_arcane_golem");
				Assert.assertEquals(player.getMaxMana(), 3 + i + 1);
			}
			Assert.assertEquals(player.getMaxMana(), 10);
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Check that minions have +1/+1
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(bloodfenRaptor.getAttack(), bloodfenRaptor.getBaseAttack() + 1);
			Assert.assertEquals(bloodfenRaptor.getHp(), bloodfenRaptor.getBaseHp() + 1);
			context.endTurn();
			Minion opponentMinion = playMinionCard(context, player, "minion_chillwind_yeti");
			context.endTurn();
			playCardWithTarget(context, player, "spell_mind_control", opponentMinion);
			Assert.assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack() + 1);
			Assert.assertEquals(opponentMinion.getHp(), opponentMinion.getBaseHp() + 1);
		});
	}

	@Test
	public void testDanceMistress() {
		// When this minion is healed, check if Crazed Dancer is summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_spirit_lash");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", dancemistress);
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_crazed_dancer");
			// Check if the Crazed Dancer has attack and hp of 2
			Assert.assertEquals(player.getMinions().get(1).getBaseAttack(), 2);
			Assert.assertEquals(player.getMinions().get(1).getBaseHp(), 2);
		});

	}

	@Test
	public void testSpikeToedBooterang() {
		// Attacks a opponent's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, opponent, "minion_river_crocolisk");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			Assert.assertEquals(opponent.getMinions().get(0).getHp(), 1);
		});

		// Attacks player's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, player, "minion_river_crocolisk");
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			Assert.assertEquals(player.getMinions().get(0).getHp(), 1);
		});

		// Defeats a Divine Shield
		runGym((context, player, opponent) -> {
			Minion silvermoonGuardian = playMinionCard(context, opponent, "minion_silvermoon_guardian");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", silvermoonGuardian);
			Assert.assertEquals(opponent.getMinions().get(0).getHp(), 2);
		});

		// If attacking Imp Gang Boss, summons two 1/1 Imps for opponent
		runGym((context, player, opponent) -> {
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", impGangBoss);
			Assert.assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_imp");
			Assert.assertEquals(opponent.getMinions().get(2).getSourceCard().getCardId(), "token_imp");
		});
	}

	@Test
	public void testStablePortal() {
		// Correctly adds a Beast to player's hand with a mana cost 2 less
		runGym((context, player, opponent) -> {
			GameLogic spiedLogic = Mockito.spy(context.getLogic());
			context.setLogic(spiedLogic);

			Mockito.doAnswer(invocation ->
					CardCatalogue.getCardById("minion_malorne"))
					.when(spiedLogic)
					.removeRandom(Mockito.anyList());

			playCard(context, player, "spell_stable_portal");
			Card card = player.getHand().get(0);
			Assert.assertEquals(card.getCardId(), "minion_malorne");
			int baseMana = card.getBaseManaCost();
			Assert.assertEquals(baseMana, 7);
			Assert.assertEquals(card.getRace(), Race.BEAST);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, card), baseMana - 2);
		});
	}

	@Test
	public void testWyrmrestSniper() {
		// Friendly Dragon survives damage so 3 damage is dealt to the opponent hero
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_sleepy_dragon");
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			Assert.assertFalse(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Friendly Dragon does not survive damage, no damage is dealt
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_sleepy_dragon");
			// Set hp to 1 so it dies
			minion.setHp(1);
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertTrue(minion.isDestroyed());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Enemy Dragon survives damage, no damage is dealt to the opponent's hero
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			Minion minion = playMinionCard(context, opponent, "minion_sleepy_dragon");

			// Damages minions by 1
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(minion.isDestroyed());
			Assert.assertEquals(minion.getHp(), minion.getBaseHp() - 1);
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});
	}
}
