package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WitchwoodTests extends TestBase {

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
