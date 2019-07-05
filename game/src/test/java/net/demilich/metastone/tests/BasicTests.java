package net.demilich.metastone.tests;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BasicTests extends TestBase {

	@Test
	public void testCorruptionCopyInteraction() {
		// Test copying using Faceless manipulator
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "spell_corruption", target);
			Minion manipulator = playMinionCardWithBattlecry(context, player, "minion_faceless_manipulator", target);
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
			Assert.assertEquals(silverHandRecruit.getSourceCard().getCardId(), "token_silver_hand_recruit");
			destroy(context, raidLeader);
			Assert.assertEquals(silverHandRecruit.getZone(), Zones.BATTLEFIELD);
			Assert.assertEquals(player.getMinions().size(), 1);
		}, HeroClass.GOLD, HeroClass.GOLD);
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

			Assert.assertEquals(warrior.getHero().getHp(), warrior.getHero().getMaxHp() - 3);
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testHeroAttack() {
		runGym((context, mage, druid) -> {
			int damage = 1;
			TestMinionCard devMonsterCard = new TestMinionCard(damage, 2);
			playCard(context, mage, devMonsterCard);

			SpellDesc heroBuffSpell = BuffSpell.create(EntityReference.FRIENDLY_HERO, damage, 0);
			context.getLogic().castSpell(druid.getId(), heroBuffSpell, druid.getHero().getReference(), null, false);
			context.getLogic().endTurn(druid.getId());

			Actor devMonster = getSingleMinion(mage.getMinions());
			GameAction minionAttackAction = new PhysicalAttackAction(devMonster.getReference());
			minionAttackAction.setTarget(druid.getHero());
			context.performAction(mage.getId(), minionAttackAction);
			// monster attacked; it should not be damaged by the hero
			Assert.assertEquals(druid.getHero().getHp(), druid.getHero().getMaxHp() - damage);
			Assert.assertEquals(devMonster.getHp(), devMonster.getMaxHp());
			context.getLogic().endTurn(mage.getId());

			context.getLogic().castSpell(druid.getId(), heroBuffSpell, druid.getHero().getReference(), null, false);
			GameAction heroAttackAction = new PhysicalAttackAction(druid.getHero().getReference());
			heroAttackAction.setTarget(devMonster);
			context.performAction(mage.getId(), heroAttackAction);
			// hero attacked; both entities should be damaged
			Assert.assertEquals(druid.getHero().getHp(), druid.getHero().getMaxHp() - 2 * damage);
			Assert.assertEquals(devMonster.getHp(), devMonster.getMaxHp() - damage);
		}, HeroClass.BLUE, HeroClass.BROWN);
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

			Assert.assertEquals(mage.getMinions().size(), 1);
			Assert.assertEquals(warrior.getMinions().size(), 1);

			Actor attacker = getSingleMinion(mage.getMinions());
			Actor defender = getSingleMinion(warrior.getMinions());

			GameAction attackAction = new PhysicalAttackAction(attacker.getReference());
			attackAction.setTarget(defender);
			context.performAction(mage.getId(), attackAction);

			Assert.assertEquals(attacker.getHp(), attacker.getMaxHp() - defender.getAttack());
			Assert.assertEquals(defender.getHp(), defender.getMaxHp() - attacker.getAttack());
			Assert.assertEquals(defender.isDestroyed(), true);

			Assert.assertEquals(mage.getMinions().size(), 1);
			Assert.assertEquals(warrior.getMinions().size(), 0);
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testSummon() {
		runGym((context, mage, opponent) -> {
			Card devMonster = new TestMinionCard(1, 1);
			context.getLogic().receiveCard(mage.getId(), devMonster);
			Assert.assertEquals(mage.getHand().getCount(), 1);
			context.performAction(mage.getId(), devMonster.play());
			Assert.assertEquals(mage.getHand().isEmpty(), true);
			Actor minion = getSingleMinion(mage.getMinions());
			Assert.assertEquals(minion.getName(), devMonster.getName());
			Assert.assertEquals(minion.getAttack(), 1);
			Assert.assertEquals(minion.getHp(), 1);
			Assert.assertEquals(minion.isDestroyed(), false);

			Card devMonster2 = new TestMinionCard(2, 2);
			context.getLogic().receiveCard(mage.getId(), devMonster2);
			GameAction summonAction = devMonster2.play();
			summonAction.setTarget(minion);
			context.performAction(mage.getId(), summonAction);

			Assert.assertEquals(mage.getMinions().size(), 2);
			Actor left = mage.getMinions().get(0);
			Actor right = mage.getMinions().get(1);
			Assert.assertEquals(left.getAttack(), 2);
			Assert.assertEquals(right.getAttack(), 1);
		}, HeroClass.BLUE, HeroClass.RED);
	}

	@Test
	public void testTheCoin() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		Player warrior = context.getPlayer2();

		Card theCoin = getTheCoin(mage.getHand());
		Assert.assertEquals(theCoin, null);
		theCoin = getTheCoin(warrior.getHand());
		Assert.assertNotEquals(theCoin, null);
	}

}
