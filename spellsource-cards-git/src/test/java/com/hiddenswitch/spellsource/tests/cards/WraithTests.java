package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class WraithTests extends TestBase {

	@Test
	public void testBackForMore() {
		runGym((context, player, opponent) -> {
			var cardCount = GameLogic.MAX_HAND_CARDS - 1;
			for (var i = 0; i < cardCount; i++) {
				receiveCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			}
			Card selection = player.getHand().get(0);
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(cardCount, discoverActions.size(), "should be whole hand");
				// should be ordered
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_back_for_more");
			assertTrue(selection.hasAttribute(Attribute.DISCARDED));
			assertEquals(player.getHand().size(), cardCount - 1);
		});
	}

	@Test
	public void testPlagueOfFlesh() {
		int ATTACK_DEBUFF = 2;
		int HP_DEBUFF = 2;
		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, 0, HP_DEBUFF);
			playCard(context, player, "spell_plague_of_flesh");
			assertTrue(enemyMinion.isDestroyed());
		}));

		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, ATTACK_DEBUFF + 1, HP_DEBUFF + 1);
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), enemyMinion.getBaseAttack() - ATTACK_DEBUFF);
			assertEquals(enemyMinion.getHp(), enemyMinion.getBaseHp() - HP_DEBUFF);
		}));

		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, 1, HP_DEBUFF + 1);
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getBaseAttack(), enemyMinion.getAttack());
			assertEquals(enemyMinion.getHp(), enemyMinion.getBaseHp() - HP_DEBUFF);
		}));

		runGym((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, 0, HP_DEBUFF + 1);
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), 0);
			assertEquals(enemyMinion.getHp(), enemyMinion.getBaseHp() - HP_DEBUFF);
		});

		runGym(((context, player, opponent) -> {
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_4_5");
			enemyMinion.setHp(4);
			playCard(context, player, "spell_plague_of_flesh");
			assertFalse(enemyMinion.isDestroyed());
			assertEquals(enemyMinion.getAttack(), enemyMinion.getBaseAttack() - ATTACK_DEBUFF);
			assertEquals(enemyMinion.getHp(), 4 - HP_DEBUFF);
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
			assertEquals(player.getMana(), 10 - (golem.getBaseManaCost() - golem.getSourceCard().getDesc().getManaCostModifier().getInt(ValueProviderArg.IF_TRUE)));
		}));

		runGym(((context, player, opponent) -> {
			Card golem = receiveCard(context, player, "minion_blood_golem");
			player.setMana(10);
			playCard(context, player, golem);
			assertEquals(player.getMana(), 10 - golem.getBaseManaCost());
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
			playMinionCard(context, player, "minion_dark_artist", target);
			assertTrue(target.isDestroyed());
		});
	}

	@Test
	public void testNothingToWaste() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "pact_nothing_to_waste");
			playCard(context, player, "spell_test_deal_5_to_enemy_hero");
			assertEquals(1, player.getMinions().size());
		}));

		runGym(((context, player, opponent) -> {
			playCard(context, player, "pact_nothing_to_waste");
			playCard(context, opponent, "minion_test_1_3");
			playCard(context, opponent, "minion_test_1_3");
			playCard(context, player, "spell_test_1_aoe");
			assertEquals(player.getMinions().size(), 1);
		}));
	}

	@Test
	public void testAutoCannibalism() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			Card autoCannibalism = receiveCard(context, player, "spell_auto_cannibalism");
			assertTrue(context.getLogic().canPlayCard(player, autoCannibalism));
			player.getHero().setHp(15);
			assertTrue(context.getLogic().canPlayCard(player, autoCannibalism));
			int hp = player.getHero().getHp();
			int hpCost = 14;
			int perMinionLifesteal = 2;
			int minions = opponent.getMinions().size();
			playCard(context, player, autoCannibalism);
			assertEquals(hp - hpCost + perMinionLifesteal * minions, player.getHero().getHp());
			assertFalse(player.getHero().isDestroyed());
		});
	}
}
