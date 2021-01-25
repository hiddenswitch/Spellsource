package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class DecayTests extends TestBase {
	@Test
	public void testBasicMinionDecay() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_test_decay");
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());

			// End own first turn, should deal damage
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp() - 1);

			// End opponent first turn, should not deal damage
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp() - 1);

			// End own second turn, should destroy minion
			context.endTurn();
			assertTrue(testMinion.isDestroyed());
		});
	}

	@Test
	public void testBasicMinionDecayWithMindControl() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_test_decay");
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());
			playCard(context, player, "spell_test_give_away", testMinion);

			// End own first turn, should not deal damage
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());

			// End opponent first turn, should deal damage
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp() - 1);

			// End own second turn, should not deal damage again
			context.endTurn();
			assertFalse(testMinion.isDestroyed());
		});
	}

	@Test
	public void testToxicMinionDecay() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_test_toxic");
			playCard(context, player, "spell_test_give_decay", testMinion);
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());

			// End own first turn, should deal damage and destroy because of toxic
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp() - 1);
			assertTrue(testMinion.isDestroyed());
		});
	}

	@Test
	public void testDodgeMinionDecay() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_test_dodge_2");
			playCard(context, player, "spell_test_give_decay", testMinion);
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());

			// End own first turn, should not take damage but instead lose its dodge
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());
			assertFalse(testMinion.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testLifestealMinionDecay() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_test_lifesteal");
			playCard(context, player, "spell_test_give_decay", testMinion);
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());

			// End own first turn, should take damage and restore health to owner
			player.getHero().setHp(20);
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp() - 1);
			assertEquals(player.getHero().getHp(), 21);
		});
	}

	@Test
	public void testDeflectMinionDecay() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_test_deflect");
			playCard(context, player, "spell_test_give_decay", testMinion);
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());

			// End own first turn, not die and instead deal damage to the owner
			context.endTurn();
			assertEquals(testMinion.getHp(), testMinion.getMaxHp());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - 1);
		});
	}

	@Test
	public void testBasicWeaponDecay() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_test_decay");
			Weapon weapon = player.getWeaponZone().get(0);
			assertEquals(weapon.getDurability(), weapon.getMaxDurability());

			// End own first turn, should deal damage
			context.endTurn();
			assertEquals(weapon.getDurability(), weapon.getMaxDurability() - 1);

			// End opponent first turn, should not deal damage
			context.endTurn();
			assertEquals(weapon.getDurability(), weapon.getMaxDurability() - 1);

			// End own second turn, weapon should be destroyed
			context.endTurn();
			assertTrue(weapon.isBroken());
		});
	}
}