package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class WraithTests extends TestBase {
	@Test
	public void testPlagueOfFlesh() {
		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_3_2");
			playCard(context, player, "spell_plague_of_flesh");
			assertTrue(enemyMinion.isDestroyed());
		}));

		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), enemyMinion.getBaseAttack() - 1);
			assertEquals(enemyMinion.getHp(), enemyMinion.getBaseHp() - 2);
		}));

		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_1_3");
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), enemyMinion.getBaseAttack());
			assertEquals(enemyMinion.getHp(), enemyMinion.getBaseHp() - 2);
		}));

		runGym((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_1_3");
			enemyMinion.setAttack(0);
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), 0);
			assertEquals(enemyMinion.getHp(), enemyMinion.getBaseHp() - 2);
		});

		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_4_5");
			enemyMinion.setHp(4);
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), enemyMinion.getBaseAttack() - 1);
			assertEquals(enemyMinion.getHp(), 2);
		}));
	}

	@Test
	public void testCurseOfPain() {
		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_3_2");
			playCard(context, player, "spell_curse_of_pain", enemyMinion);
			attack(context, opponent, enemyMinion, player.getHero());
			assertTrue(enemyMinion.isDestroyed());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
		}));

		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_test_toxic");
			playCard(context, player, "spell_curse_of_pain", minion);
			minion.setHp(5);
			attack(context, player, minion, opponent.getHero());
			assertEquals(minion.getHp(), 1);
			assertFalse(minion.isDestroyed());
		}));
	}

	@Test
	public void testBloodGolem() {
		runGym(((context, player, opponent) -> {
			Card golem = receiveCard(context, player, "minion_blood_golem");
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			player.setMana(10);
			playCard(context, player, golem);
			assertEquals(player.getMana(), 5);
		}));

		runGym(((context, player, opponent) -> {
			Card golem = receiveCard(context, player, "minion_blood_golem");
			player.setMana(10);
			playCard(context, player, golem);
			assertEquals(player.getMana(), 3);
		}));
	}

	@Test
	public void testDarkArtist() {
		runGym(((context, player, opponent) -> {
			overrideBattlecry(context, player, battlecryActions -> battlecryActions.get(0));
			Minion minion = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_dark_artist");
			assertEquals(minion.getAttack(), minion.getBaseAttack() - 2);
			assertEquals(minion.getHp(), minion.getBaseHp() - 2);
		}));
		runGym(((context, player, opponent) -> {
			overrideBattlecry(context, player, battlecryActions -> battlecryActions.get(0));
			Minion minion = playMinionCard(context, opponent, "minion_test_2_3");
			playMinionCard(context, player, "minion_dark_artist");
			assertEquals(minion.getAttack(), minion.getBaseAttack() - 1);
			assertEquals(minion.getHp(), minion.getBaseHp() - 2);
		}));
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			playMinionCardWithBattlecry(context, player, "minion_dark_artist", target);
			assertTrue(target.isDestroyed());
		});
	}

	@Test
	public void testNothingToWaste() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "pact_nothing_to_waste");
			playCard(context, player, "spell_test_deal_5_to_enemy_hero");
			assertEquals(player.getMinions().size(), 2);
		}));

		runGym(((context, player, opponent) -> {
			playCard(context, player, "pact_nothing_to_waste");
			playCard(context, opponent, "minion_test_1_3");
			playCard(context, opponent, "minion_test_1_3");
			playCard(context, player, "spell_test_1_aoe");
			assertEquals(player.getMinions().size(), 2);
		}));
	}
}
