package com.hiddenswitch.deckgeneration;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.HealSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Behaviour that inherits all of PlayRandomWithoutSelfDamageBehaviour,
// but also allows for additional specifications
public class PlayRandomWithoutSelfDamageWithDefinedDecisions extends PlayRandomWithoutSelfDamageBehaviour {
	List<String> cardsToKeepOnMulligan = new ArrayList<>();
	List<String> minionsThatDoNotAttackEnemyHero = new ArrayList<>();
	List<String> minionsThatDoNotAttackEnemyMinions = new ArrayList<>();
	List<String> cardsThatCannotTargetOwnEntities = new ArrayList<>();
	List<String> cardsThatCannotTargetEnemyEntities = new ArrayList<>();

	boolean alwaysAttackEnemyHero = false;
	boolean canEndTurnIfAttackingEnemyHeroIsValid = true;
	boolean canBuffEnemyMinions = true;
	boolean canHealEnemyEntities = true;
	boolean canHealFullHealthEntities = true;

	public void setCanHealFullHealthEntities(boolean canHealFullHealthEntities) {
		this.canHealFullHealthEntities = canHealFullHealthEntities;
	}

	public void setCanEndTurnIfAttackingEnemyHeroIsValid(boolean canEndTurnIfAttackingEnemyHeroIsValid) {
		this.canEndTurnIfAttackingEnemyHeroIsValid = canEndTurnIfAttackingEnemyHeroIsValid;
	}

	public void setCanHealEnemyEntities(boolean canHealEnemyEntities) {
		this.canHealEnemyEntities = canHealEnemyEntities;
	}

	public void setAlwaysAttackEnemyHero(boolean alwaysAttackEnemyHero) {
		this.alwaysAttackEnemyHero = alwaysAttackEnemyHero;
	}

	public void setCanBuffEnemyMinions(boolean canBuffEnemyMinions) {
		this.canBuffEnemyMinions = canBuffEnemyMinions;
	}

	/**
	 * @param decisionTypes            A list of decision types that indicates what decisions we are manipulating
	 * @param cardsListForEachDecision A list of cards that corresponds to each decision type
	 */
	public PlayRandomWithoutSelfDamageWithDefinedDecisions(List<DecisionType> decisionTypes, List<List<String>> cardsListForEachDecision) {
		assert decisionTypes.size() == cardsListForEachDecision.size();

		for (int i = 0; i < decisionTypes.size(); i++) {
			if (decisionTypes.get(i).equals(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_HERO)) {
				minionsThatDoNotAttackEnemyHero = cardsListForEachDecision.get(i);
			}
			if (decisionTypes.get(i).equals(DecisionType.SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION)) {
				minionsThatDoNotAttackEnemyMinions = cardsListForEachDecision.get(i);
			}
			if (decisionTypes.get(i).equals(DecisionType.KEEP_CARDS_ON_MULLIGAN)) {
				cardsToKeepOnMulligan = cardsListForEachDecision.get(i);
			}
			if (decisionTypes.get(i).equals(DecisionType.SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES)) {
				cardsThatCannotTargetEnemyEntities = cardsListForEachDecision.get(i);
			}
			if (decisionTypes.get(i).equals(DecisionType.SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES)) {
				cardsThatCannotTargetOwnEntities = cardsListForEachDecision.get(i);
			}
		}
	}

	public PlayRandomWithoutSelfDamageWithDefinedDecisions(List<DecisionType> booleanDecisionTypes) {
		updateBooleanDecisionTypes(booleanDecisionTypes);
	}

	public PlayRandomWithoutSelfDamageWithDefinedDecisions(List<DecisionType> cardListDecisionTypes, List<List<String>> cardsListForEachDecision, List<DecisionType> booleanDecisionTypes) {
		this(cardListDecisionTypes, cardsListForEachDecision);
		updateBooleanDecisionTypes(booleanDecisionTypes);
	}

	/**
	 * Updates the boolean parameters of the AI behaviour
	 *
	 * @param booleanDecisionTypes The list of boolean parameters that the AI
	 *                             must follow
	 */

	public void updateBooleanDecisionTypes(List<DecisionType> booleanDecisionTypes) {
		if (booleanDecisionTypes.contains(DecisionType.ALWAYS_ATTACK_ENEMY_HERO)) {
			alwaysAttackEnemyHero = true;
		} else {
			alwaysAttackEnemyHero = false;
		}
		if (booleanDecisionTypes.contains(DecisionType.CANNOT_END_TURN_IF_ATTACKING_ENEMY_HERO_IS_VALID)) {
			canEndTurnIfAttackingEnemyHeroIsValid = false;
		} else {
			canEndTurnIfAttackingEnemyHeroIsValid = true;
		}
		if (booleanDecisionTypes.contains(DecisionType.CANNOT_BUFF_ENEMY_MINIONS)) {
			canBuffEnemyMinions = false;
		} else {
			canBuffEnemyMinions = true;
		}
		if (booleanDecisionTypes.contains(DecisionType.CANNOT_HEAL_ENEMY_ENTITIES)) {
			canHealEnemyEntities = false;
		} else {
			canHealEnemyEntities = true;
		}
		if (booleanDecisionTypes.contains(DecisionType.CANNOT_HEAL_FULL_HEALTH_ENTITIES)) {
			canHealFullHealthEntities = false;
		} else {
			canHealFullHealthEntities = true;
		}
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (!cardsToKeepOnMulligan.contains(card.getCardId())) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		filterActions(context, player, validActions);
		return super.requestAction(context, player, validActions);
	}

	/**
	 * Filter all unwanted actions based on AI behaviour parameters
	 *
	 * @param context      the {@link GameContext} of the particular game
	 * @param player       the {@link Player} that this behaviour controls
	 * @param validActions the initial list of {@link GameAction} moves
	 *                     that the player can do
	 */
	public void filterActions(GameContext context, Player player, List<GameAction> validActions) {
		Player opponent = context.getOpponent(player);
		filterEnemyFaceHits(player, opponent, validActions);

		if (opponent.getMinions().stream().filter(minion -> (minion.getAttribute(Attribute.TAUNT) != null)
		).collect(Collectors.toList()).size() == 0) {
			filterEnemyMinionHits(player, opponent, validActions);
		}
		filterBuffSpellsOnEnemyMinions(player, opponent, validActions, context);
		filterHealingSpellsOnEnemyMinions(player, opponent, validActions, context);
		filterHealingSpellsOnFullHealthMinions(player, opponent, validActions, context);
		filterCardsThatCannotTargetCertainPlayerEntities(context, opponent, validActions, cardsThatCannotTargetEnemyEntities);
		filterCardsThatCannotTargetCertainPlayerEntities(context, player, validActions, cardsThatCannotTargetOwnEntities);
		filterEndTurn(opponent, validActions);
	}

	/**
	 * Filters actions that play a card from a list of cards on a certain given player's entity
	 *
	 * @param context           The context for the game state
	 * @param playerToNotTarget The player that a card on our list of cards is not allowed to target
	 * @param validActions      The list of actions that we are filtering through
	 * @param invalidCards      The list of card ids that are not allowed to target the given player's entities
	 */
	public void filterCardsThatCannotTargetCertainPlayerEntities(GameContext context, Player playerToNotTarget, List<GameAction> validActions, List<String> invalidCards) {
		if (invalidCards.isEmpty()) {
			return;
		}
		validActions.removeIf(action -> {
			if (HeroPowerAction.class.isAssignableFrom(action.getClass())) {
				HeroPowerAction heroPowerAction = (HeroPowerAction) action;
				Entity sourceEntity = context.resolveSingleTarget(heroPowerAction.getSourceReference());
				Card sourceCard = sourceEntity.getSourceCard();
				String sourceCardId = sourceCard.getCardId();
				if (!invalidCards.contains(sourceCardId)) {
					return false;
				}
				Entity targetEntity = context.resolveSingleTarget(heroPowerAction.getTargetReference());
				return targetEntity.getOwner() == playerToNotTarget.getOwner();
			}
			if (PlaySpellCardAction.class.isAssignableFrom(action.getClass())) {
				PlaySpellCardAction playSpellCardAction = (PlaySpellCardAction) action;
				Entity sourceEntity = context.resolveSingleTarget(playSpellCardAction.getSourceReference());
				Card sourceCard = sourceEntity.getSourceCard();
				String sourceCardId = sourceCard.getCardId();
				if (!invalidCards.contains(sourceCardId)) {
					return false;
				}
				Entity targetEntity = context.resolveSingleTarget(playSpellCardAction.getTargetReference());
				return targetEntity.getOwner() == playerToNotTarget.getOwner();
			}
			if (BattlecryAction.class.isAssignableFrom(action.getClass())) {
				BattlecryAction battlecryAction = (BattlecryAction) action;
				Entity sourceEntity = context.resolveSingleTarget(battlecryAction.getSourceReference());
				Card sourceCard = sourceEntity.getSourceCard();
				String sourceCardId = sourceCard.getCardId();
				if (!invalidCards.contains(sourceCardId)) {
					return false;
				}
				Entity targetEntity = context.resolveSingleTarget(battlecryAction.getTargetReference());
				return targetEntity.getOwner() == playerToNotTarget.getOwner();
			}
			return false;
		});
	}

	/**
	 * Filters out all {@link PhysicalAttackAction} actions such that
	 * the attacker is in minionsThatDoNotAttackEnemyHero and the target is
	 * indeed the enemy hero
	 *
	 * @param player       The player that is doing the action
	 * @param opponent     The opponent of the acting player
	 * @param validActions The list of valid actions before the filtering
	 */
	public void filterEnemyFaceHits(Player player, Player opponent, List<GameAction> validActions) {
		validActions.removeIf(action -> {
			if (!(action instanceof PhysicalAttackAction)) {
				return false;
			}
			String attackerId = getMinionIdByReferenceForPlayer(player, ((PhysicalAttackAction) action).getAttackerReference());
			return (action.getTargetReference().equals(opponent.getHero().getReference()) && minionsThatDoNotAttackEnemyHero.contains(attackerId));
		});
	}

	/**
	 * Same as filterEnemyFaceHits, except for enemy minions instead
	 */
	public void filterEnemyMinionHits(Player player, Player opponent, List<GameAction> validActions) {
		validActions.removeIf(action -> {
			if (!(action instanceof PhysicalAttackAction)) {
				return false;
			}
			String attackerId = getMinionIdByReferenceForPlayer(player, ((PhysicalAttackAction) action).getAttackerReference());
			if (!alwaysAttackEnemyHero && !minionsThatDoNotAttackEnemyMinions.contains(attackerId)) {
				return false;
			}
			return (opponent.getMinions().stream().filter(minion -> minion.getReference().equals(action.getTargetReference())).collect(Collectors.toList()).size() == 1);
		});
	}

	/**
	 * Filter out the option to end the turn if certain conditions are satisfied
	 *
	 * @param opponent     The opposing player
	 * @param validActions A list of valid {@link GameAction} to filter
	 */

	public void filterEndTurn(Player opponent, List<GameAction> validActions) {
		if (!canEndTurnIfAttackingEnemyHeroIsValid && canAttackEnemyHero(opponent, validActions)) {
			validActions.removeIf(actionToRemove -> actionToRemove instanceof EndTurnAction);
		}
	}

	/**
	 * Remove all spells, battlecries, etc. that buff enemy minions by targeting them
	 * (buffing as a side effect is allowed)
	 *
	 * @param player       the {@link Player} that this behaviour controls
	 * @param opponent     the {@link Player} that the player is against
	 * @param validActions A list of valid {@link GameAction} to filter
	 * @param context      The {@link GameContext} for this particular game
	 */

	public void filterBuffSpellsOnEnemyMinions(Player player, Player opponent, List<GameAction> validActions, GameContext context) {
		if (canBuffEnemyMinions) {
			return;
		}
		validActions.removeIf(action -> {
			if (checkIfMetaSpell(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getOwner() == opponent.getOwner()
						&& targetedMinionIsBuffedBySpell(((PlaySpellCardAction) action).getSpell());
			}
			if (checkIfBuffSpell(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getOwner() == opponent.getOwner()
						&& targetedMinionIsBuffedBySpell(((PlaySpellCardAction) action).getSpell());
			}

			if (checkIfBuffBattlecry(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getOwner() == opponent.getOwner()
						&& targetedMinionIsBuffedBySpell(((BattlecryAction) action).getSpell());
			}

			return false;

		});
	}

	/**
	 * Remove all spells, battlecries, etc. that heal enemy minions by targeting them
	 * (healing as a side effect is allowed)
	 *
	 * @param player       the {@link Player} that this behaviour controls
	 * @param opponent     the {@link Player} that the player is against
	 * @param validActions A list of valid {@link GameAction} to filter
	 * @param context      The {@link GameContext} for this particular game
	 */

	public void filterHealingSpellsOnEnemyMinions(Player player, Player opponent, List<GameAction> validActions, GameContext context) {
		if (canHealEnemyEntities) {
			return;
		}
		validActions.removeIf(action -> {
			if (checkIfMetaSpell(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getOwner() == opponent.getOwner()
						&& targetedEntityIsHealedBySpell(((PlaySpellCardAction) action).getSpell());
			}
			if (checkIfHealingSpell(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getOwner() == opponent.getOwner()
						&& targetedEntityIsHealedBySpell(((PlaySpellCardAction) action).getSpell());
			}

			if (checkIfHealingBattlecry(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getOwner() == opponent.getOwner()
						&& targetedEntityIsHealedBySpell(((BattlecryAction) action).getSpell());
			}

			return false;

		});
	}

	/**
	 * Remove all spells, battlecries, etc. that heal enemy minions by targeting them
	 * (healing as a side effect is allowed)
	 *
	 * @param player       the {@link Player} that this behaviour controls
	 * @param opponent     the {@link Player} that the player is against
	 * @param validActions A list of valid {@link GameAction} to filter
	 * @param context      The {@link GameContext} for this particular game
	 */

	public void filterHealingSpellsOnFullHealthMinions(Player player, Player opponent, List<GameAction> validActions, GameContext context) {
		if (canHealFullHealthEntities) {
			return;
		}
		validActions.removeIf(action -> {
			if (checkIfMetaSpell(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getAttributeValue(Attribute.HP) == action.getTargets(context, player.getIndex()).get(0).getAttributeValue(Attribute.MAX_HP)
						&& targetedEntityIsHealedBySpell(((PlaySpellCardAction) action).getSpell());
			}
			if (checkIfHealingSpell(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getAttributeValue(Attribute.HP) == action.getTargets(context, player.getIndex()).get(0).getAttributeValue(Attribute.MAX_HP)
						&& targetedEntityIsHealedBySpell(((PlaySpellCardAction) action).getSpell());
			}

			if (checkIfHealingBattlecry(action)) {
				return action.getTargets(context, player.getIndex()) != null
						&& action.getTargets(context, player.getIndex()).get(0).getAttributeValue(Attribute.HP) == action.getTargets(context, player.getIndex()).get(0).getAttributeValue(Attribute.MAX_HP)
						&& targetedEntityIsHealedBySpell(((BattlecryAction) action).getSpell());
			}

			return false;

		});
	}


	public boolean checkIfMetaSpell(GameAction action) {
		if (!(action instanceof PlaySpellCardAction)) {
			return false;
		}

		PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
		SpellDesc spellDesc = spellCardAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return MetaSpell.class.isAssignableFrom(spellDesc.getDescClass());
	}


	public boolean checkIfBuffBattlecry(GameAction action) {
		if (!(action instanceof BattlecryAction)) {
			return false;
		}

		BattlecryAction spellCardAction = (BattlecryAction) action;
		SpellDesc spellDesc = spellCardAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return BuffSpell.class.isAssignableFrom(spellDesc.getDescClass());


	}

	public boolean checkIfBuffSpell(GameAction action) {
		if (!(action instanceof PlaySpellCardAction)) {
			return false;
		}

		PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
		SpellDesc spellDesc = spellCardAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return BuffSpell.class.isAssignableFrom(spellDesc.getDescClass());
	}

	/**
	 * Checks if the particular spell buffs whatever entity it targets
	 * (the target is null in this case)
	 *
	 * @param spellDesc The {@link SpellDesc} that we are analyzing
	 * @return whether or not the spell buffs whatever entity it targets
	 */
	public boolean targetedMinionIsBuffedBySpell(SpellDesc spellDesc) {
		Predicate<HasEntrySet.BfsNode<Enum, Object>> hasTarget = (node) -> {
			if (!(node.getValue() instanceof SpellDesc)) {
				return false;
			}

			SpellDesc spell = (SpellDesc) node.getValue();
			return spell.getTarget() != null;
		};
		return spellDesc.bfs().build()
				.anyMatch(node -> {
					if (!(node.getValue() instanceof SpellDesc)) {
						return false;
					}
					SpellDesc spell = (SpellDesc) node.getValue();

					return node.predecessors().noneMatch(hasTarget) && !hasTarget.test(node) && BuffSpell.class.isAssignableFrom(spell.getDescClass());
				});
	}


	public boolean checkIfHealingBattlecry(GameAction action) {
		if (!(action instanceof BattlecryAction)) {
			return false;
		}

		BattlecryAction spellCardAction = (BattlecryAction) action;
		SpellDesc spellDesc = spellCardAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return HealSpell.class.isAssignableFrom(spellDesc.getDescClass());
	}

	public boolean checkIfHealingHeroPower(GameAction action) {
		if (!(action instanceof HeroPowerAction)) {
			return false;
		}

		HeroPowerAction heroPowerAction = (HeroPowerAction) action;
		SpellDesc spellDesc = heroPowerAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return HealSpell.class.isAssignableFrom(spellDesc.getDescClass());
	}

	public boolean checkIfHealingSpell(GameAction action) {
		if (!(action instanceof PlaySpellCardAction)) {
			return false;
		}

		PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
		SpellDesc spellDesc = spellCardAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return HealSpell.class.isAssignableFrom(spellDesc.getDescClass());
	}

	/**
	 * Checks if the particular spell heals whatever entity it targets
	 * (the target is null in this case)
	 *
	 * @param spellDesc The {@link SpellDesc} that we are analyzing
	 * @return whether or not the spell heals whatever entity it targets
	 */
	public boolean targetedEntityIsHealedBySpell(SpellDesc spellDesc) {
		Predicate<HasEntrySet.BfsNode<Enum, Object>> hasTarget = (node) -> {
			if (!(node.getValue() instanceof SpellDesc)) {
				return false;
			}

			SpellDesc spell = (SpellDesc) node.getValue();
			return spell.getTarget() != null;
		};
		return spellDesc.bfs().build()
				.anyMatch(node -> {
					if (!(node.getValue() instanceof SpellDesc)) {
						return false;
					}
					SpellDesc spell = (SpellDesc) node.getValue();

					return node.predecessors().noneMatch(hasTarget) && !hasTarget.test(node) && HealSpell.class.isAssignableFrom(spell.getDescClass());
				});
	}

	/**
	 * @param player          the player that the minion belongs to
	 * @param minionReference the reference to the minion instance on the board
	 * @return The Id of the card whose reference is given if it belongs to the player or null if none found
	 */
	public String getMinionIdByReferenceForPlayer(Player player, EntityReference minionReference) {
		List<String> result = player.getMinions().stream().filter(minion -> minion.getReference().equals(minionReference)).map(minion -> minion.getSourceCard().getCardId()).collect(Collectors.toList());
		return result.size() == 0 ? null : result.get(0);
	}

	public boolean canAttackEnemyHero(Player opponent, List<GameAction> validActions) {
		for (int i = 0; i < validActions.size(); i++) {
			GameAction action = validActions.get(i);
			if (action instanceof PhysicalAttackAction && action.getTargetReference().equals(opponent.getHero().getReference())) {
				return true;
			}
		}
		return false;
	}
}
