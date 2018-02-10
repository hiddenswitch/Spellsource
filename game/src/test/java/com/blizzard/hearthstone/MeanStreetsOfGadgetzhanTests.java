package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MeanStreetsOfGadgetzhanTests extends TestBase {

	@Test
	public void testShakuTheCollector() {
		runGym((context, player, opponent) -> {
			Minion shaku = playMinionCard(context, player, "minion_shaku_the_collector");
			context.endTurn();
			context.endTurn();
			attack(context, player, shaku, opponent.getHero());
			Assert.assertEquals(player.getHand().get(0).getHeroClass(), opponent.getHero().getHeroClass());
		});
	}

	@Test
	public void testRazaTheChained() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_raza_the_chained");
			player.setMaxMana(2);
			player.setMana(2);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play().withTargetReference(opponent.getHero().getReference()));
			Assert.assertEquals(player.getMana(), 2);
		});
	}

	@Test
	public void testInkmasterSolia() {
		runGym((context, player, opponent) -> {
			Card fireball = receiveCard(context, player, "spell_fireball");
			Assert.assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost());
			playCard(context, player, "minion_inkmaster_solia");
			Assert.assertEquals(costOf(context, player, fireball), 0);
			context.endTurn();
			Assert.assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost());
		});
	}
}
