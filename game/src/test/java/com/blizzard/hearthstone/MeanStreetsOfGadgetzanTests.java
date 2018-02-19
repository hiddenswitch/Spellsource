package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;

public class MeanStreetsOfGadgetzanTests extends TestBase {

	@Test
	public void testVirmenSensei() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "token_silver_hand_recruit");
			MinionCard card = (MinionCard) receiveCard(context, player, "minion_virmen_sensei");

			CountDownLatch latch = new CountDownLatch(1);
			overrideBattlecry(player, battlecryActions -> {
				Assert.assertEquals(battlecryActions.size(), 1);
				Assert.assertEquals(battlecryActions.get(0).getTargetReference(), beast.getReference());
				latch.countDown();
				return battlecryActions.get(0);
			});

			context.getLogic().performGameAction(player.getId(), card.play());
			Assert.assertEquals(latch.getCount(), 0);
		});
	}

	@Test
	public void testWrathion() {
		runGym((context, player, opponent) -> {
			int preFatigue = player.getHero().getHp();
			playCard(context, player, "minion_wrathion");
			Assert.assertEquals(player.getHero().getHp(), preFatigue - 1, "Wrathion should successfully deal fatigue.");
		});

		runGym((context, player, opponent) -> {
			int hp = player.getHero().getHp();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_wrathion");
			Assert.assertEquals(player.getHero().getHp(), hp, "Wrathion should not have dealt fatigue.");
		});

		runGym((context, player, opponent) -> {
			int hp = player.getHero().getHp();
			shuffleToDeck(context, player, "minion_ysera");
			playCard(context, player, "minion_wrathion");
			Assert.assertEquals(player.getHero().getHp(), hp - 1, "Wrathion should not have dealt fatigue.");
		});
	}

	@Test
	public void testLunarVisions() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			shuffleToDeck(context, player, "spell_lunar_visions");
			playCard(context, player, "minion_grand_archivist");
			context.endTurn();
			Assert.assertEquals(player.getHand().size(), 2);
			Assert.assertTrue(player.getHand().stream().allMatch(card -> costOf(context, player, card)
					== card.getBaseManaCost() - 2));
		});
	}

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
