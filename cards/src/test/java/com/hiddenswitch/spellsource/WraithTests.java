package com.hiddenswitch.spellsource;

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
	}
}
