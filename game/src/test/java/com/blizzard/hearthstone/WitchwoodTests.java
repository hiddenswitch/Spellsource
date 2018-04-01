package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WitchwoodTests extends TestBase {

	@Test
	public void testNightmareAmalgam() {
		// Test card cost modifier
		runGym((context, player, opponent) -> {
			Card nightmare = receiveCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_mechwarper");
			Assert.assertEquals(costOf(context, player, nightmare), nightmare.getBaseManaCost() - 1);
		});

		// Test Murlocs cost health
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_seadevil_stinger");
			int hp = player.getHero().getHp();
			playCard(context, player, "minion_nightmare_amalgam");
			Assert.assertEquals(player.getHero().getHp(), hp - CardCatalogue.getCardById("minion_nightmare_amalgam").getBaseManaCost());
		});

		// Test RaceCondition
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_patches_the_pirate");
			playCard(context, player, "minion_nightmare_amalgam");
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_patches_the_pirate");
		});

		// Test CardFilter
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Card thing = receiveCard(context, player, "minion_thing_from_below");
			Assert.assertEquals(costOf(context, player, thing), thing.getBaseManaCost() - 1);
		});

		// Test RaceFilter
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Minion murkEye = playMinionCard(context, player, "minion_old_murk-eye");
			Assert.assertEquals(murkEye.getAttack(), murkEye.getBaseAttack() + 1);
		});
	}

	@Test
	public void testBlackCat() {
		runGym((context, player, opponent) -> {
			Card stillInDeck = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_black_cat");
			Assert.assertEquals(stillInDeck.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card stillInDeck = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_black_cat");
			Assert.assertEquals(stillInDeck.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testPumpkinPeasant() {
		runGym((context, player, opponent) -> {
			Card pumpkin = receiveCard(context, player, "minion_pumpkin_peasant");
			playCard(context, player, "minion_grimestreet_outfitter");
			context.endTurn();
			context.endTurn();
			Minion summonedPumpkin = playMinionCard(context, player, pumpkin);
			Assert.assertEquals(summonedPumpkin.getAttack(), pumpkin.getBaseHp() + 1);
		});
	}
}
