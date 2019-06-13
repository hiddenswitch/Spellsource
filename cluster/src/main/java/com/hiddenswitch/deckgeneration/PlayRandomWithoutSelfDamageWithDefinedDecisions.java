package com.hiddenswitch.deckgeneration;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.EndTurnAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Behaviour that inherits all of PlayRandomWithoutSelfDamageBehaviour,
// but also allows for additional specifications
public class PlayRandomWithoutSelfDamageWithDefinedDecisions extends PlayRandomWithoutSelfDamageBehaviour {
	List<String> cardsToKeepOnMulligan = new ArrayList<>();
	List<String> minionsThatDoNotAttackEnemyHero = new ArrayList<>();
	List<String> minionsThatDoNotAttackEnemyMinions = new ArrayList<>();
	boolean alwaysAttackEnemyHero = false;
	boolean canEndTurnIfAttackingEnemyHeroIsValid = true;

	public void setCanEndTurnIfAttackingEnemyHeroIsValid(boolean canEndTurnIfAttackingEnemyHeroIsValid) {
		this.canEndTurnIfAttackingEnemyHeroIsValid = canEndTurnIfAttackingEnemyHeroIsValid;
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
		}
	}

	public PlayRandomWithoutSelfDamageWithDefinedDecisions(List<DecisionType> booleanDecisionTypes) {
		updateBooleanDecisionTypes(booleanDecisionTypes);
	}

	public PlayRandomWithoutSelfDamageWithDefinedDecisions(List<DecisionType> cardListDecisionTypes, List<List<String>> cardsListForEachDecision, List<DecisionType> booleanDecisionTypes) {
		this(cardListDecisionTypes, cardsListForEachDecision);
		updateBooleanDecisionTypes(booleanDecisionTypes);
	}

	public void updateBooleanDecisionTypes(List<DecisionType> booleanDecisionTypes) {
		if (booleanDecisionTypes.contains(DecisionType.ALWAYS_ATTACK_ENEMY_HERO)) {
			alwaysAttackEnemyHero = true;
		} else {
			alwaysAttackEnemyHero = false;
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

	public void filterActions(GameContext context, Player player, List<GameAction> validActions) {
		Player opponent = context.getOpponent(player);
		filterEnemyFaceHits(player, opponent, validActions);

		if (opponent.getMinions().stream().filter(minion -> (minion.getAttribute(Attribute.TAUNT) != null)
		).collect(Collectors.toList()).size() == 0) {
			filterEnemyMinionHits(player, opponent, validActions);
		}

		filterEndTurn(player, opponent, validActions);
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

	public void filterEndTurn(Player player, Player opponent, List<GameAction> validActions) {
		if (!canEndTurnIfAttackingEnemyHeroIsValid && canAttackEnemyHero(opponent, validActions)) {
			validActions.removeIf(actionToRemove -> actionToRemove instanceof EndTurnAction);
		}
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
