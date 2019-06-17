package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.HeroPowerAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;


public class TestBehaviours extends TestBase {
	@Test
	public static void testValidActionsFilterForDamagingSelf() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_shock_0");
			context.getPlayer2().getHero().setHp(5);
			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}

	@Test
	public static void testValidBattlecryActionsFilterForDamagingSelf() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			overrideBattlecry(context, player, battlecryActions -> {
				int originalSize = battlecryActions.size();
				List<GameAction> actions = battlecryActions.stream().map(battlecryAction -> (GameAction) battlecryAction).collect(Collectors.toList());
				behaviour.filterActions(player, actions);
				assertTrue(originalSize - 1 == actions.size());
				return battlecryActions.get(0);
			});
			playCard(context, player, "minion_with_damage_3");
		});
	}

	@Test
	public static void testValidBattlecryActionsFilterForDamagingOwnMinions() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "minion_stat_3");
			playCard(context, player, "minion_stat_3");

			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			behaviour.ownMinionTargetingIsEnabled(false);
			overrideBattlecry(context, player, battlecryActions -> {
				int originalSize = battlecryActions.size();
				List<GameAction> actions = battlecryActions.stream().map(battlecryAction -> (GameAction) battlecryAction).collect(Collectors.toList());
				behaviour.filterActions(player, actions);
				assertTrue(originalSize - 2 == actions.size());
				return battlecryActions.get(0);
			});
			playCard(context, player, "minion_with_damage_3");
		});
	}

	@Test
	public static void testPlayRandomWithDefinedMulligan() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "minion_stat_3");
			receiveCard(context, player, "minion_stat_2");
			List<String> cardsToKeep = new ArrayList<>();
			cardsToKeep.add("minion_with_damage_3");
			cardsToKeep.add("minion_stat_3");
			PlayRandomWithDefinedMulligans behaviour = new PlayRandomWithDefinedMulligans(cardsToKeep);
			List<Card> discardedCards = behaviour.mulligan(context, player, player.getHand());
			assertTrue(discardedCards.get(0).getCardId().equals("minion_stat_2"));
			assertTrue(discardedCards.size() == 1);
		});
	}

	// Tests the PlayRandomWithoutSelfDamageWithDefinedBehavior for certain decisions:
	// 1. Mulligan
	// 2. Attacking enemy face
	// 3. Attacking enemy minions
	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorSomeMinionsDoNotAttackEnemyHero() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_HERO), Collections.singletonList(Collections.singletonList("minion_with_damage_3")));
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}


	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorSomeMinionsDoNotAttackEnemyMinion() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION), Collections.singletonList(Collections.singletonList("minion_with_damage_3")));
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}


	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorAlwaysAttackEnemyHero() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.ALWAYS_ATTACK_ENEMY_HERO));
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}


	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorMinionDoesNotAttack() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");

			List<DecisionType> decisionTypeList = new ArrayList<>();
			decisionTypeList.add(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_HERO);
			decisionTypeList.add(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION);

			List<List<String>> cardListForEachDecision = new ArrayList<>();
			cardListForEachDecision.add(Collections.singletonList("minion_with_damage_3"));
			cardListForEachDecision.add(Collections.singletonList("minion_with_damage_3"));

			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypeList, cardListForEachDecision);
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 2 == actions.size());
		});
	}

	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorMulligan() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "minion_stat_3");
			receiveCard(context, player, "minion_stat_2");
			List<String> cardsToKeep = new ArrayList<>();
			cardsToKeep.add("minion_with_damage_3");
			cardsToKeep.add("minion_stat_3");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.KEEP_CARDS_ON_MULLIGAN), Collections.singletonList(cardsToKeep));
			List<Card> discardedCards = behaviour.mulligan(context, player, player.getHand());
			assertTrue(discardedCards.get(0).getCardId().equals("minion_stat_2"));
			assertTrue(discardedCards.size() == 1);
		});
	}

	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorCannotEndTurn() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");

			List<DecisionType> decisionTypeList = new ArrayList<>();
			decisionTypeList.add(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION);

			List<List<String>> cardListForEachDecision = new ArrayList<>();
			cardListForEachDecision.add(Collections.singletonList("minion_with_damage_3"));

			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypeList, cardListForEachDecision);
			behaviour.setCanEndTurnIfAttackingEnemyHeroIsValid(false);

			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 2 == actions.size());
		});
	}

	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorStillCanAttackTauntMinionWithOnlyHitEnemyHeroActive() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_goldshire_footman");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.ALWAYS_ATTACK_ENEMY_HERO));
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize == actions.size());
		});
	}


	@Test
	public static void testGetTargetedMinionEntityIdForBuffSpellFromMetaSpell() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "spell_blessing_of_might");
			receiveCard(context, player, "spell_barkskin");
			receiveCard(context, player, "spell_earth_shock");
			player.setMana(1);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.CANNOT_BUFF_ENEMY_MINIONS));
			List<GameAction> actions = context.getValidActions();

			boolean spellHasTargetedBuff1 = behaviour.targetedMinionIsBuffedBySpell(((PlaySpellCardAction) actions.get(0)).getSpell());
			boolean spellHasTargetedBuff2 = behaviour.targetedMinionIsBuffedBySpell(((PlaySpellCardAction) actions.get(1)).getSpell());
			boolean spellHasTargetedBuff3 = behaviour.targetedMinionIsBuffedBySpell(((PlaySpellCardAction) actions.get(2)).getSpell());
			assertTrue(spellHasTargetedBuff1 && spellHasTargetedBuff2 && !spellHasTargetedBuff3);
		});
	}

	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForBuffingEnemyMinions() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_barkskin");
			player.setMana(1);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.CANNOT_BUFF_ENEMY_MINIONS));
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOpponentMinions = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getOwner() == opponent.getOwner())
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOpponentMinions.isEmpty());
		});
	}

	@Test
	public static void testGetTargetedMinionEntityIdForHealingSpellFromMetaSpell() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_void_terror");
			receiveCard(context, player, "spell_flash_of_light");
			receiveCard(context, player, "spell_sacrificial_pact");
			receiveCard(context, player, "spell_holy_light");
			player.setMana(2);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.CANNOT_BUFF_ENEMY_MINIONS));
			List<GameAction> actions = context.getValidActions();

			// The damage hero powers should all be false
			for (int i = 0; i < 4; i++) {
				assertTrue(!behaviour.targetedEntityIsHealedBySpell(((HeroPowerAction) actions.get(i)).getSpell()));
			}

			// The healing spell flash of light should be true
			for (int i = 5; i < 8; i++) {
				assertTrue(behaviour.targetedEntityIsHealedBySpell(((PlaySpellCardAction) actions.get(i)).getSpell()));
			}

			// The spell sacrificial pact should be false
			assertTrue(!behaviour.targetedEntityIsHealedBySpell(((PlaySpellCardAction) actions.get(8)).getSpell()));

			// The spell healing spell holy light should be false
			for (int i = 9; i < 13; i++) {
				assertTrue(behaviour.targetedEntityIsHealedBySpell(((PlaySpellCardAction) actions.get(i)).getSpell()));
			}
		});
	}

	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForHealingEnemyEntities() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_regenerate");
			player.setMana(0);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.CANNOT_HEAL_ENEMY_ENTITIES));
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 2 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOpponentEntities = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getOwner() == opponent.getOwner())
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOpponentEntities.isEmpty());
		});
	}

	@Test
	public static void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForHealingFullHealthEntities() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_stat_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_regenerate");
			player.setMana(0);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.CANNOT_HEAL_FULL_HEALTH_ENTITIES));
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 4 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOpponentEntities = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getOwner() == opponent.getOwner())
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOpponentEntities.isEmpty());
		});
	}
}
