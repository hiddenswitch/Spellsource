package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeanStreetsOfGadgetzanTests extends TestBase {

	@Test
	public void testMayorNoggenfogger() {
		// Noggen correctly overrides to allow taunters and stealths
		runGym((context, player, opponent) -> {
			Minion noggen = playMinionCard(context, player, "minion_mayor_noggenfogger");
			Minion footman = playMinionCard(context, player, "minion_goldshire_footman");
			Minion stealth = playMinionCard(context, player, "minion_worgen_infiltrator");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			GameLogic spyLogic = Mockito.spy(context.getLogic());
			context.setLogic(spyLogic);
			CountDownLatch latch = new CountDownLatch(1);
			Mockito.doAnswer(invocation -> {
				List<Entity> targets = invocation.getArgument(0);
				Assert.assertEquals(targets.size(), 4, "Noggen, Footman, Worgen and the hero are all the valid targets");
				List<Integer> ids = targets.stream().map(Entity::getReference).map(EntityReference::getId).collect(Collectors.toList());
				Assert.assertTrue(ids.containsAll(Stream.of(noggen, footman, stealth, player.getHero()).map(Entity::getReference).map(EntityReference::getId).collect(Collectors.toList())));
				latch.countDown();
				return invocation.callRealMethod();
			}).when(spyLogic).getRandom(Mockito.anyList());
			attack(context, opponent, wolfrider, noggen);
			Assert.assertEquals(latch.getCount(), 0L);
		});

		// Noggen correctly overrides filtered battlecries
		runGym((context, player, opponent) -> {
			Minion noggen = playMinionCard(context, player, "minion_mayor_noggenfogger");
			Minion pirate1 = playMinionCard(context, player, "minion_bloodsail_raider");
			Minion pirate2 = playMinionCard(context, player, "minion_bloodsail_raider");

			GameLogic spyLogic = Mockito.spy(context.getLogic());
			context.setLogic(spyLogic);
			CountDownLatch latch = new CountDownLatch(1);
			Mockito.doAnswer(invocation -> {
				List<Entity> targets = invocation.getArgument(0);
				Assert.assertEquals(targets.size(), 2, "Pirates are the only valid targets");
				List<Integer> ids = targets.stream().map(Entity::getReference).map(EntityReference::getId).collect(Collectors.toList());
				Assert.assertTrue(ids.containsAll(Stream.of(pirate1, pirate2).map(Entity::getReference).map(EntityReference::getId).collect(Collectors.toList())));
				latch.countDown();
				return invocation.callRealMethod();
			}).when(spyLogic).getRandom(Mockito.anyList());
			playMinionCard(context, player, "minion_golakka_crawler");
			Assert.assertEquals(latch.getCount(), 0L);
		});
	}

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
			Assert.assertTrue(player.getHand().get(0).hasHeroClass(opponent.getHero().getHeroClass()));
		});
	}

	@Test
	public void testRazaTheChained() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_raza_the_chained");
			player.setMaxMana(2);
			player.setMana(2);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play().withTargetReference(opponent.getHero().getReference()));
			Assert.assertEquals(player.getMana(), 1);
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
