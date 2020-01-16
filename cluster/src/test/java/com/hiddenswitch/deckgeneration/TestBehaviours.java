package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.HeroPowerAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;


public class TestBehaviours extends TestBase {
	@Test
	public void testValidActionsFilterForDamagingSelf() {
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
	public void testValidBattlecryActionsFilterForDamagingSelf() {
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
	public void testValidBattlecryActionsFilterForDamagingOwnMinions() {
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
	public void testPlayRandomWithDefinedMulligan() {
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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorSomeMinionsDoNotAttackEnemyHero() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			HashSet<String> minionsThatDoNotAttackEnemyHero = new HashSet<>();
			minionsThatDoNotAttackEnemyHero.add("minion_with_damage_3");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_HERO), Collections.singletonList(minionsThatDoNotAttackEnemyHero));
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}


	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorSomeMinionsDoNotAttackEnemyMinion() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");
			HashSet<String> minionsThatDoNotAttackEnemyMinions = new HashSet<>();
			minionsThatDoNotAttackEnemyMinions.add("minion_with_damage_3");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION), Collections.singletonList(minionsThatDoNotAttackEnemyMinions));
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}


	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorAlwaysAttackEnemyHero() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.ALWAYS_ATTACK_ENEMY_HERO);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}


	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorMinionDoesNotAttack() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");

			List<DecisionType> decisionTypeList = new ArrayList<>();
			decisionTypeList.add(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_HERO);
			decisionTypeList.add(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION);

			List<HashSet<String>> cardListForEachDecision = new ArrayList<>();

			HashSet<String> minionsThatDoNotAttack = new HashSet<>();
			minionsThatDoNotAttack.add("minion_with_damage_3");

			cardListForEachDecision.add(minionsThatDoNotAttack);
			cardListForEachDecision.add(minionsThatDoNotAttack);

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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorMulligan() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "minion_stat_3");
			receiveCard(context, player, "minion_stat_2");
			HashSet<String> cardsToKeep = new HashSet<>();
			cardsToKeep.add("minion_with_damage_3");
			cardsToKeep.add("minion_stat_3");
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.KEEP_CARDS_ON_MULLIGAN), Collections.singletonList(cardsToKeep));
			List<Card> discardedCards = behaviour.mulligan(context, player, player.getHand());
			assertTrue(discardedCards.get(0).getCardId().equals("minion_stat_2"));
			assertTrue(discardedCards.size() == 1);
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorCannotEndTurn() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_stat_1");

			List<DecisionType> decisionTypeList = new ArrayList<>();
			decisionTypeList.add(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION);

			List<HashSet<String>> cardListForEachDecision = new ArrayList<>();
			HashSet<String> minionsThatCannotAttackEnemyMinions = new HashSet<>();
			minionsThatCannotAttackEnemyMinions.add("minion_with_damage_3");
			cardListForEachDecision.add(minionsThatCannotAttackEnemyMinions);

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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorStillCanAttackTauntMinionWithOnlyHitEnemyHeroActive() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_goldshire_footman");
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.ALWAYS_ATTACK_ENEMY_HERO);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
			context.endTurn();
			context.endTurn();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize == actions.size());
		});
	}


	@Test
	public void testGetTargetedMinionEntityIdForBuffSpellFromMetaSpell() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "spell_blessing_of_might");
			receiveCard(context, player, "spell_barkskin");
			receiveCard(context, player, "spell_earth_shock");
			player.setMana(1);
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.CANNOT_BUFF_ENEMY_MINIONS);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
			List<GameAction> actions = context.getValidActions();

			boolean spellHasTargetedBuff1 = behaviour.targetedMinionIsBuffedBySpell(((PlaySpellCardAction) actions.get(0)).getSpell());
			boolean spellHasTargetedBuff2 = behaviour.targetedMinionIsBuffedBySpell(((PlaySpellCardAction) actions.get(1)).getSpell());
			boolean spellHasTargetedBuff3 = behaviour.targetedMinionIsBuffedBySpell(((PlaySpellCardAction) actions.get(2)).getSpell());
			assertTrue(spellHasTargetedBuff1 && spellHasTargetedBuff2 && !spellHasTargetedBuff3);
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForBuffingEnemyMinions() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_barkskin");
			player.setMana(1);
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.CANNOT_BUFF_ENEMY_MINIONS);

			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
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
	public void testGetTargetedMinionEntityIdForHealingSpellFromMetaSpell() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_void_terror");
			receiveCard(context, player, "spell_flash_of_light");
			receiveCard(context, player, "spell_sacrificial_pact");
			receiveCard(context, player, "spell_holy_light");
			player.setMana(2);
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.CANNOT_BUFF_ENEMY_MINIONS);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForHealingEnemyEntities() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_with_damage_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_regenerate");
			player.setMana(0);
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.CANNOT_HEAL_ENEMY_ENTITIES);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForHealingFullHealthEntities() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_stat_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_regenerate");
			player.setMana(0);
			HashSet<DecisionType> decisionTypes = new HashSet<>();
			decisionTypes.add(DecisionType.CANNOT_HEAL_FULL_HEALTH_ENTITIES);
			PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(decisionTypes);
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 4 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOpponentEntities = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getAttributeValue(Attribute.HP) == target.getAttributeValue(Attribute.MAX_HP))
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOpponentEntities.isEmpty());
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeCardsCannotTargetEnemyEntities() {
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		HashSet<String> cardsThatCannotBeUsedOnEnemyEntities = new HashSet<>();
		cardsThatCannotBeUsedOnEnemyEntities.addAll(neverUseOnEnemyMinions.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnEnemyEntities));

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_stat_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_ancestral_healing");
			player.setMana(0);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeCardsCannotTargetEnemyEntities2() {
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		HashSet<String> cardsThatCannotBeUsedOnEnemyEntities = new HashSet<>();
		cardsThatCannotBeUsedOnEnemyEntities.addAll(neverUseOnEnemyMinions.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnEnemyEntities));

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_stat_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_blessing_of_kings");
			player.setMana(10);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOpponentEntities = actions.stream()
					.filter(action -> (
							!HeroPowerAction.class.isAssignableFrom(action.getClass())
									&& action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getOwner() == opponent.getOwner())
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOpponentEntities.isEmpty());
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeBattlecriesCannotTargetEnemyEntities() {
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		HashSet<String> cardsThatCannotBeUsedOnEnemyEntities = new HashSet<>();
		cardsThatCannotBeUsedOnEnemyEntities.addAll(neverUseOnEnemyMinions.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnEnemyEntities));

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_earthen_ring_farseer");

			player.setMana(3);
			overrideBattlecry(context, player, battlecryActions -> {
				int originalSize = battlecryActions.size();
				List<GameAction> actions = battlecryActions.stream().map(battlecryAction -> (GameAction) battlecryAction).collect(Collectors.toList());
				behaviour.filterActions(context, player, actions);
				assertTrue(originalSize - 1 == actions.size());

				// There should be no valid actions that target enemy entities
				List<GameAction> actionsThatTargetOpponentEntities = actions.stream()
						.filter(action -> (
								action.getTargets(context, player.getIndex())
										.stream()
										.filter(target -> target.getOwner() == opponent.getOwner())
										.findFirst()
										.isPresent())).collect(Collectors.toList());
				assertTrue(actionsThatTargetOpponentEntities.isEmpty());
				return battlecryActions.get(0);
			});
			playCard(context, player, "minion_earthen_ring_farseer");
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeHeroPowersCannotTargetEnemyEntities() {
		NeverUseOnEnemyMinions neverUseOnEnemyMinions = new NeverUseOnEnemyMinions();
		HashSet<String> cardsThatCannotBeUsedOnEnemyEntities = new HashSet<>();
		cardsThatCannotBeUsedOnEnemyEntities.addAll(neverUseOnEnemyMinions.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnEnemyEntities));

		runGym((context, player, opponent) -> {
			Hero priest = CardCatalogue.getCardById("hero_anduin").createHero(player);
			player.setHero(priest);
			player.getHero().setHp(30);
			player.setMana(2);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

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
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeCardsCannotTargetOwnEntities() {
		NeverUseOnOwnMinion neverUseOnOwnMinion = new NeverUseOnOwnMinion();
		HashSet<String> cardsThatCannotBeUsedOnOwnEntities = new HashSet<>();
		cardsThatCannotBeUsedOnOwnEntities.addAll(neverUseOnOwnMinion.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnOwnEntities));

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_stat_3");
			playCard(context, opponent, "minion_goldshire_footman");
			receiveCard(context, player, "spell_moonfire");
			player.setMana(0);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 2 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOwnEntities = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getOwner() == player.getOwner())
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOwnEntities.isEmpty());
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeBattlecriesCannotTargetOwnEntities() {
		NeverUseOnOwnMinion neverUseOnOwnMinion = new NeverUseOnOwnMinion();
		HashSet<String> cardsThatCannotBeUsedOnOwnEntities = new HashSet<>();
		cardsThatCannotBeUsedOnOwnEntities.addAll(neverUseOnOwnMinion.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnOwnEntities));

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_fire_elemental");

			player.setMana(10);
			overrideBattlecry(context, player, battlecryActions -> {
				int originalSize = battlecryActions.size();
				List<GameAction> actions = battlecryActions.stream().map(battlecryAction -> (GameAction) battlecryAction).collect(Collectors.toList());
				behaviour.filterActions(context, player, actions);
				assertTrue(originalSize - 1 == actions.size());

				// There should be no valid actions that target enemy entities
				List<GameAction> actionsThatTargetOwnEntities = actions.stream()
						.filter(action -> (
								action.getTargets(context, player.getIndex())
										.stream()
										.filter(target -> target.getOwner() == player.getOwner())
										.findFirst()
										.isPresent())).collect(Collectors.toList());
				assertTrue(actionsThatTargetOwnEntities.isEmpty());
				return battlecryActions.get(0);
			});
			playCard(context, player, "minion_fire_elemental");
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeHeroPowersCannotTargetOwnEntities() {
		NeverUseOnOwnMinion neverUseOnOwnMinion = new NeverUseOnOwnMinion();
		HashSet<String> cardsThatCannotBeUsedOnOwnEntities = new HashSet<>();
		cardsThatCannotBeUsedOnOwnEntities.addAll(neverUseOnOwnMinion.classicAndBasicSets);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES), Collections.singletonList(cardsThatCannotBeUsedOnOwnEntities));

		runGym((context, player, opponent) -> {
			player.setMana(2);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

			// There should be no valid actions that target enemy entities
			List<GameAction> actionsThatTargetOwnEntities = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> target.getOwner() == player.getOwner())
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(actionsThatTargetOwnEntities.isEmpty());
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForSomeDamageCardsCannotTargetWeakMinions() {
		HashSet<String> cardsThatCannotBeUsedOnWeakMinions = new HashSet<>();
		cardsThatCannotBeUsedOnWeakMinions.add("spell_explosive_shot");
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(Collections.singletonList(DecisionType.SOME_DAMAGE_SPELLS_CANNOT_TARGET_WEAK_MINIONS), Collections.singletonList(cardsThatCannotBeUsedOnWeakMinions));

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, opponent, "minion_chillwind_yeti");
			receiveCard(context, player, "spell_explosive_shot");
			player.setMana(5);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

			// There should be no valid actions that target player
			List<GameAction> spellCardActionsThatTargetLowHealthMinions = actions.stream()
					.filter(action ->
							(!(action instanceof HeroPowerAction)) && (
									action.getTargets(context, player.getIndex())
											.stream()
											.filter(target -> target.getOwner() == player.getOwner())
											.findFirst()
											.isPresent())).collect(Collectors.toList());
			assertTrue(spellCardActionsThatTargetLowHealthMinions.isEmpty());
		});
	}

	@Test
	public void testPlayRandomWithoutSelfDamageWithDefinedBehaviorForCannotAttackWithMinionThatWillDieAndNotKillOtherMinion() {
		HashSet<DecisionType> booleanDecisionTypes = new HashSet<>();
		booleanDecisionTypes.add(DecisionType.CANNOT_ATTACK_WITH_A_MINION_THAT_WILL_DIE_AND_NOT_KILL_OTHER_MINION);
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions(booleanDecisionTypes);

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, opponent, "minion_chillwind_yeti");
			context.endTurn();
			context.endTurn();
			player.setMana(0);

			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(context, player, actions);
			assertTrue(originalSize - 1 == actions.size());

			// There should be no valid actions that target player
			List<GameAction> attackActionsThatKillAttackingMinionButNotTheDefendingMinion = actions.stream()
					.filter(action -> (
							action.getTargets(context, player.getIndex())
									.stream()
									.filter(target -> Minion.class.isAssignableFrom(target.getClass()))
									.findFirst()
									.isPresent())).collect(Collectors.toList());
			assertTrue(attackActionsThatKillAttackingMinionButNotTheDefendingMinion.isEmpty());
		});
	}

	@Test
	public void testValidActionsBySource() {
		PlayRandomWithoutSelfDamageWithDefinedDecisions behaviour = new PlayRandomWithoutSelfDamageWithDefinedDecisions();

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, opponent, "minion_chillwind_yeti");
			receiveCard(context, player, "spell_arcane_missiles");
			receiveCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			context.endTurn();
			player.setMana(2);

			List<GameAction> actions = context.getValidActions();
			List<List<GameAction>> sortedActions = behaviour.validActionsBySource(actions);
			assertTrue(sortedActions.size() == 5);
			for (int i = 0; i < sortedActions.size(); i++) {
				for (GameAction action : sortedActions.get(i))
					assertTrue(sortedActions.get(i).get(0).getSourceReference().equals(action.getSourceReference()));
			}
		});
	}
}


