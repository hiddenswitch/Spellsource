package net.demilich.metastone.game.logic;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.utils.TurnState;
import net.demilich.metastone.game.actions.EndTurnAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.HasChooseOneActions;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

public class ActionLogic implements Serializable {
	private final TargetLogic targetLogic = new TargetLogic();

	@Suspendable
	public GameAction getAutoHeroPower(GameContext context, Player player) {
		return getHeroPowerActions(context, player).get(0);
	}

	@Suspendable
	private List<GameAction> getHeroAttackActions(GameContext context, Player player) {
		List<GameAction> heroAttackActions = new ArrayList<GameAction>();
		Hero hero = player.getHero();
		if (!hero.canAttackThisTurn()) {
			return heroAttackActions;
		}
		rollout(new PhysicalAttackAction(hero.getReference()), context, player, heroAttackActions);

		return heroAttackActions;
	}

	@Suspendable
	private List<GameAction> getHeroPowerActions(GameContext context, Player player) {
		List<GameAction> heroPowerActions = new ArrayList<GameAction>();
		Card heroPower = player.getHero().getHeroPower();

		EntityReference heroPowerReference = new EntityReference(heroPower.getId()
		);
		if (!context.getLogic().canPlayCard(player.getId(), heroPowerReference)) {
			return heroPowerActions;
		}
		if (heroPower.isChooseOne()) {
			HasChooseOneActions chooseOneCard = heroPower;
			for (GameAction chooseOneAction : chooseOneCard.playOptions()) {
				rollout(chooseOneAction, context, player, heroPowerActions);
			}
		} else {
			rollout(heroPower.play(), context, player, heroPowerActions);
		}

		return heroPowerActions;
	}

	private List<GameAction> getPhysicalAttackActions(GameContext context, Player player) {
		List<GameAction> physicalAttackActions = new ArrayList<GameAction>();
		physicalAttackActions.addAll(getHeroAttackActions(context, player));

		for (Minion minion : player.getMinions()) {
			if (!minion.canAttackThisTurn()) {
				continue;
			}

			List<PhysicalAttackTargetOverrideAura> filters = context.getTriggersAssociatedWith(minion.getReference()).stream()
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
			EntityReference EntityReference = new EntityReference(card.getId());
			if (!context.getLogic().canPlayCard(player.getId(), EntityReference)) {
				continue;
			}

			if (card.isChooseOne()) {
				HasChooseOneActions chooseOneCard = card;
				if (context.getLogic().hasAttribute(player, Attribute.BOTH_CHOOSE_ONE_OPTIONS) && chooseOneCard.hasBothOptions()) {
					GameAction chooseOneAction = chooseOneCard.playBothOptions();
					rollout(chooseOneAction, context, player, playCardActions);
				} else {
					for (GameAction chooseOneAction : chooseOneCard.playOptions()) {
						rollout(chooseOneAction, context, player, playCardActions);
					}
				}
			} else {
				rollout(card.play(), context, player, playCardActions);
			}

		}
		return playCardActions;
	}

	@Suspendable
	public List<GameAction> getValidActions(GameContext context, Player player) {
		List<GameAction> validActions = new ArrayList<GameAction>();
		validActions.addAll(getPhysicalAttackActions(context, player));
		validActions.addAll(getPlayCardActions(context, player));
		if (context.getTurnState() != TurnState.TURN_ENDED) {
			final EndTurnAction endTurnAction = new EndTurnAction();
			endTurnAction.setSource(player.getReference());
			validActions.add(endTurnAction);
		}

		// Assign the ids
		for (int i = 0; i < validActions.size(); i++) {
			validActions.get(i).setId(i);
		}

		return validActions;
	}

	@Suspendable
	public boolean hasAutoHeroPower(GameContext context, Player player) {
		Card heroPower = player.getHero().getHeroPower();

		EntityReference heroPowerReference = new EntityReference(heroPower.getId());
		return (context.getLogic().canPlayCard(player.getId(), heroPowerReference) && heroPower.getTargetSelection() == TargetSelection.AUTO);
	}

	public void rollout(GameAction action, GameContext context, Player player, Collection<GameAction> actions) {
		context.getLogic().processTargetModifiers(player, action);
		if (action.getTargetRequirement() == TargetSelection.NONE || action.getTargetRequirement() == TargetSelection.AUTO) {
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
