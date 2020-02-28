package com.hiddenswitch.spellsource.tests.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTests extends TestBase {

	@Test
	public void testCorruptionCopyInteraction() {
		// Test copying using Faceless manipulator
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "spell_corruption", target);
			Minion manipulator = playMinionCard(context, player, "minion_faceless_manipulator", target);
			context.endTurn();
			context.endTurn();
			assertTrue(target.isDestroyed());
			assertTrue(manipulator.isDestroyed());
		});

		// Test copying using Elixir of Shadows
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "spell_corruption", target);
			playCard(context, player, "spell_elixir_of_shadows", target);
			Minion clone = player.getMinions().get(0);
			context.endTurn();
			context.endTurn();
			assertTrue(target.isDestroyed());
			assertTrue(clone.isDestroyed());
		});
	}

	@Test
	public void testRaidLeader() {
		runGym((context, player, opponent) -> {
			Minion raidLeader = playMinionCard(context, player, "minion_raid_leader");
			useHeroPower(context, player);
			Minion silverHandRecruit = player.getMinions().get(1);
			assertEquals(silverHandRecruit.getSourceCard().getCardId(), "token_silver_hand_recruit");
			destroy(context, raidLeader);
			assertEquals(silverHandRecruit.getZone(), Zones.BATTLEFIELD);
			assertEquals(player.getMinions().size(), 1);
		}, "GOLD", "GOLD");
	}

	private Card getTheCoin(CardList cards) {
		for (Card card : cards) {
			if (card.getCardId().equalsIgnoreCase("spell_the_coin")) {
				return card;
			}
		}
		return null;
	}

	@Test
	public void testBattlecry() {
		runGym((context, mage, warrior) -> {
			TestMinionCard devMonster = new TestMinionCard(3, 3);
			SpellDesc damageSpell = DamageSpell.create(EntityReference.ENEMY_HERO, 3);
			BattlecryDesc testBattlecry = new BattlecryDesc();
			testBattlecry.spell = damageSpell;
			testBattlecry.spell.setTarget(warrior.getHero().getReference());
			testBattlecry.spell = damageSpell;
			devMonster.getMinion().setBattlecry(testBattlecry);
			context.getLogic().receiveCard(mage.getId(), devMonster);
			context.performAction(mage.getId(), devMonster.play());

			assertEquals(warrior.getHero().getHp(), warrior.getHero().getMaxHp() - 3);
		}, "BLUE", "RED");
	}

	@Test
	public void testHeroAttack() {
		runGym((context, mage, druid) -> {
			int damage = 1;
			TestMinionCard devMonsterCard = new TestMinionCard(damage, 2);
			playCard(context, mage, devMonsterCard);

			SpellDesc heroBuffSpell = BuffSpell.create(EntityReference.FRIENDLY_HERO, damage, 0);
			context.getLogic().castSpell(druid.getId(), heroBuffSpell, druid.getHero().getReference(), null, TargetSelection.NONE, false, null);
			context.getLogic().endTurn(druid.getId());

			Actor devMonster = getSingleMinion(mage.getMinions());
			GameAction minionAttackAction = new PhysicalAttackAction(devMonster.getReference());
			minionAttackAction.setTarget(druid.getHero());
			context.performAction(mage.getId(), minionAttackAction);
			// monster attacked; it should not be damaged by the hero
			assertEquals(druid.getHero().getHp(), druid.getHero().getMaxHp() - damage);
			assertEquals(devMonster.getHp(), devMonster.getMaxHp());
			context.getLogic().endTurn(mage.getId());

			context.getLogic().castSpell(druid.getId(), heroBuffSpell, druid.getHero().getReference(), null, TargetSelection.NONE, false, null);
			GameAction heroAttackAction = new PhysicalAttackAction(druid.getHero().getReference());
			heroAttackAction.setTarget(devMonster);
			context.performAction(mage.getId(), heroAttackAction);
			// hero attacked; both entities should be damaged
			assertEquals(druid.getHero().getHp(), druid.getHero().getMaxHp() - 2 * damage);
			assertEquals(devMonster.getHp(), devMonster.getMaxHp() - damage);
		}, "BLUE", "BROWN");
	}

	@Test
	public void testMinionAttack() {
		runGym((context, mage, warrior) -> {
			Card card1 = new TestMinionCard(5, 5);
			context.getLogic().receiveCard(mage.getId(), card1);
			context.performAction(mage.getId(), card1.play());

			Card card2 = new TestMinionCard(1, 1);
			context.getLogic().receiveCard(warrior.getId(), card2);
			context.performAction(warrior.getId(), card2.play());

			assertEquals(mage.getMinions().size(), 1);
			assertEquals(warrior.getMinions().size(), 1);

			Actor attacker = getSingleMinion(mage.getMinions());
			Actor defender = getSingleMinion(warrior.getMinions());

			GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
			attackAction.setTarget(defender);
			context.performAction(mage.getId(), attackAction);

			assertEquals(attacker.getHp(), attacker.getMaxHp() - defender.getAttack());
			assertEquals(defender.getHp(), defender.getMaxHp() - attacker.getAttack());
			assertEquals(defender.isDestroyed(), true);

			assertEquals(mage.getMinions().size(), 1);
			assertEquals(warrior.getMinions().size(), 0);
		}, "BLUE", "RED");
	}

	@Test
	public void testSummon() {
		runGym((context, mage, opponent) -> {
			Card devMonster = new TestMinionCard(1, 1);
			context.getLogic().receiveCard(mage.getId(), devMonster);
			assertEquals(mage.getHand().getCount(), 1);
			context.performAction(mage.getId(), devMonster.play());
			assertEquals(mage.getHand().isEmpty(), true);
			Actor minion = getSingleMinion(mage.getMinions());
			assertEquals(minion.getName(), devMonster.getName());
			assertEquals(minion.getAttack(), 1);
			assertEquals(minion.getHp(), 1);
			assertEquals(minion.isDestroyed(), false);

			Card devMonster2 = new TestMinionCard(2, 2);
			context.getLogic().receiveCard(mage.getId(), devMonster2);
			GameAction summonAction = devMonster2.play();
			summonAction.setTarget(minion);
			context.performAction(mage.getId(), summonAction);

			assertEquals(mage.getMinions().size(), 2);
			Actor left = mage.getMinions().get(0);
			Actor right = mage.getMinions().get(1);
			assertEquals(left.getAttack(), 2);
			assertEquals(right.getAttack(), 1);
		}, "BLUE", "RED");
	}

	@Test
	public void testTheCoin() {
		GameContext context = createContext("BLUE", "RED", true, DeckFormat.getFormat("Standard"));
		Player mage = context.getPlayer1();
		Player warrior = context.getPlayer2();

		Card theCoin = getTheCoin(mage.getHand());
		assertEquals(theCoin, null);
		theCoin = getTheCoin(warrior.getHand());
		assertNotEquals(theCoin, null);
	}

}
