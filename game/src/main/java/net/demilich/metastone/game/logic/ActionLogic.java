package net.demilich.metastone.game.logic;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ChooseOneOverride;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class turns game actions into a list of possible actions for the player.
 *
 * @see GameContext#getValidActions() for the entry point into the action logic.
 * @see #rollout(GameAction, GameContext, Player, Collection) for the meat-and-bones of turning a {@link GameAction}
 * instance into multiple game actions, one for each target.
 */
public class ActionLogic implements Serializable {
	private final TargetLogic targetLogic = new TargetLogic();

	@Suspendable
	private List<GameAction> getHeroAttackActions(GameContext context, Player player) {
		List<GameAction> heroAttackActions = new ArrayList<GameAction>();
		Hero hero = player.getHero();
		if (!hero.canAttackThisTurn(context)) {
			return heroAttackActions;
		}
		rollout(new PhysicalAttackAction(hero.getReference()), context, player, heroAttackActions);

		return heroAttackActions;
	}

	@Suspendable
	private List<GameAction> getHeroPowerActions(GameContext context, Player player) {
		List<GameAction> heroPowerActions = new ArrayList<GameAction>();
		Card heroPower = player.getHeroPowerZone().get(0);

		EntityReference heroPowerReference = new EntityReference(heroPower.getId());
		if (!context.getLogic().canPlayCard(player.getId(), heroPowerReference)) {
			return heroPowerActions;
		}
		if (heroPower.isChooseOne()) {
			rolloutChooseOnesWithOverrides(context, player, heroPowerActions, heroPower);
		} else {
			heroPower.processTargetSelectionOverride(context, player);
			rollout(heroPower.play(), context, player, heroPowerActions);
		}

		return heroPowerActions;
	}

	private List<GameAction> getPhysicalAttackActions(GameContext context, Player player) {
		List<GameAction> physicalAttackActions = new ArrayList<GameAction>();
		physicalAttackActions.addAll(getHeroAttackActions(context, player));

		for (Minion minion : player.getMinions()) {
			if (!minion.canAttackThisTurn(context)) {
				continue;
			}

			List<PhysicalAttackTargetOverrideAura> filters = context.getLogic().getActiveTriggers(minion.getReference()).stream()
					.filter(trigger -> trigger instanceof PhysicalAttackTargetOverrideAura)
					.map(trigger -> (PhysicalAttackTargetOverrideAura) trigger).collect(Collectors.toList());
			if (!filters.isEmpty()) {
				Set<EntityReference> common = new HashSet<>();

				for (Integer targetId : filters.get(0).getAffectedEntities()) {
					common.add(new EntityReference(targetId));
				}

				for (int i = 1; i < filters.size(); i++) {
					common = Sets.intersection(common, filters.get(i).getAffectedEntities().stream().map(EntityReference::new).collect(Collectors.toSet()));
				}

				for (EntityReference target : common) {
					PhysicalAttackAction attackAction = new PhysicalAttackAction(minion.getReference());
					attackAction.setTargetReference(target);
					physicalAttackActions.add(attackAction);
				}

			} else {
				rollout(new PhysicalAttackAction(minion.getReference()), context, player, physicalAttackActions);
			}
		}
		return physicalAttackActions;
	}

	@Suspendable
	private List<GameAction> getPlayCardActions(GameContext context, Player player) {
		List<GameAction> playCardActions = new ArrayList<GameAction>();
		playCardActions.addAll(getHeroPowerActions(context, player));

		for (Card card : player.getHand()) {
			EntityReference cardReference = card.getReference();
			if (!context.getLogic().canPlayCard(player.getId(), cardReference)) {
				continue;
			}

			if (card.isChooseOne()) {
				rolloutChooseOnesWithOverrides(context, player, playCardActions, card);
			} else {
				card.processTargetSelectionOverride(context, player);
				rollout(card.play(), context, player, playCardActions);
			}

		}
		return playCardActions;
	}

	private void rolloutChooseOnesWithOverrides(GameContext context, Player player, List<GameAction> playCardActions, Card card) {
		ChooseOneOverride override = context.getLogic().getChooseOneAuraOverrides(player, card);
		switch (override) {
			case BOTH_COMBINED:
				rollout(card.playBothOptions(), context, player, playCardActions);
				break;
			case ALWAYS_FIRST:
				rollout(card.playOptions()[0], context, player, playCardActions);
				break;
			case ALWAYS_SECOND:
				rollout(card.playOptions()[1], context, player, playCardActions);
				break;
			case NONE:
			default:
				for (PlayCardAction action : card.playOptions()) {
					rollout(action, context, player, playCardActions);
				}
				break;
		}
	}

	/**
	 * Iterates through the cards and minions belonging to the player, and determines what actions are available. This
	 * also takes into account the available mana, game rules, etc.
	 *
	 * @param context
	 * @param player
	 * @return
	 */
	@Suspendable
	public List<GameAction> getValidActions(GameContext context, Player player) {
		List<GameAction> validActions = new ArrayList<GameAction>();
		validActions.addAll(getPhysicalAttackActions(context, player));
		validActions.addAll(getPlayCardActions(context, player));
		if (context.getTurnState() != TurnState.TURN_ENDED) {
			final EndTurnAction endTurnAction = new EndTurnAction(player.getId());
			endTurnAction.setSourceReference(player.getReference());
			validActions.add(endTurnAction);
		}

		// Assign the ids
		for (int i = 0; i < validActions.size(); i++) {
			validActions.get(i).setId(i);
		}

		return validActions;
	}

	/**
	 * Rolls out actions. For actions that have {@code targetRequirement} values that aren't {@link TargetSelection#NONE},
	 * returning new actions whose {@link GameAction#getTargetReference()} is a valid target.
	 *
	 * @param action
	 * @param context
	 * @param player
	 * @param actions
	 */
	public void rollout(GameAction action, GameContext context, Player player, Collection<GameAction> actions) {
		context.getLogic().processTargetModifiers(action);
		if (action.getTargetRequirement() == TargetSelection.NONE) {
			actions.add(action);
		} else {
			for (Entity validTarget : targetLogic.getValidTargets(context, player, action)) {
				GameAction rolledOutAction = action.clone();
				rolledOutAction.setTarget(validTarget);
				actions.add(rolledOutAction);
			}
		}
	}

}
