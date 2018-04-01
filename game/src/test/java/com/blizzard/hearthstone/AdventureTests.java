package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AdventureTests extends TestBase {

	@Test
	public void testNefarian() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_nefarian");
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			Assert.assertTrue(player.getHand().get(1).hasHeroClass(opponent.getHero().getHeroClass()));
		}, HeroClass.VIOLET, HeroClass.GOLD);
	}

	@Test
	public void testChieftainScarvash() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_chieftain_scarvash");
			Card friendly = receiveCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(costOf(context, player, friendly), friendly.getBaseManaCost());
			context.endTurn();
			Card enemy = receiveCard(context, opponent, "minion_bloodfen_raptor");
			Assert.assertEquals(costOf(context, player, friendly), friendly.getBaseManaCost());
			Assert.assertEquals(costOf(context, opponent, enemy), enemy.getBaseManaCost() + 1);
		});
	}

}
