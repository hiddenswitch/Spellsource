package com.hiddenswitch.hearthstone;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.tests.util.GymFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.*;

public class MeanStreetsOfGadgetzanTests extends TestBase {

	@Test
	public void testMistressOfMixtures() {
		runGym((context, player, opponent) -> {
			Minion mistress = playMinionCard(context, player, "minion_mistress_of_mixtures");
			player.getHero().setHp(10);
			opponent.getHero().setHp(10);
			destroy(context, mistress);
			assertEquals(player.getHero().getHp(), 14);
			assertEquals(opponent.getHero().getHp(), 14);
		});
	}

	@Test
	public void testMayorNoggenfogger() {
		// Noggen correctly overrides to allow taunters and stealths
		runGym((context, player, opponent) -> {
			Minion noggen = playMinionCard(context, player, "minion_mayor_noggenfogger");
			Minion footman = playMinionCard(context, player, "minion_goldshire_footman");
			Minion stealth = playMinionCard(context, player, "minion_worgen_infiltrator");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
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
			attack(context, opponent, charger, noggen);
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
			Card card = receiveCard(context, player, "minion_virmen_sensei");

			CountDownLatch latch = new CountDownLatch(1);
			overrideBattlecry(context, player, battlecryActions -> {
				Assert.assertEquals(battlecryActions.size(), 1);
				Assert.assertEquals(battlecryActions.get(0).getTargetReference(), beast.getReference());
				latch.countDown();
				return battlecryActions.get(0);
			});

			context.performAction(player.getId(), card.play());
			Assert.assertEquals(latch.getCount(), 0);
		});
	}

	@Test
	public void testWrathion() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			player.getAttributes().remove(Attribute.DISABLE_FATIGUE);
		});

		factory.run((context, player, opponent) -> {
			int preFatigue = player.getHero().getHp();
			playCard(context, player, "minion_wrathion");
			Assert.assertEquals(player.getHero().getHp(), preFatigue - 1, "Wrathion should successfully deal fatigue.");
		});

		factory.run((context, player, opponent) -> {
			int hp = player.getHero().getHp();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_wrathion");
			Assert.assertEquals(player.getHero().getHp(), hp, "Wrathion should not have dealt fatigue.");
		});

		factory.run((context, player, opponent) -> {
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
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testRazaTheChained() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_raza_the_chained");
			player.setMaxMana(2);
			player.setMana(2);
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play().withTargetReference(opponent.getHero().getReference()));
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
