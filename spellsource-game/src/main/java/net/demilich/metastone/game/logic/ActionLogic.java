package net.demilich.metastone.game.logic;

import com.google.common.collect.Sets;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.ChooseOneOverride;
import net.demilich.metastone.game.cards.desc.compiler.GameStateArena;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.aura.PhysicalAttackTargetOverrideAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
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

	private void collectHeroAttackActions(GameContext context, Player player, List<GameAction> actions) {
		Hero hero = player.getHero();
		if (!hero.canAttackThisTurn(context)) {
			return;
		}
		rollout(new PhysicalAttackAction(hero.getReference()), context, player, actions);
	}

	private void collectHeroPowerActions(GameContext context, Player player, List<GameAction> actions) {
		Card heroPower = player.getHeroPowerZone().get(0);

		if (!context.getLogic().canPlayCard(player, heroPower)) {
			return;
		}
		if (heroPower.isChooseOne()) {
			rolloutChooseOnesWithOverrides(context, player, actions, heroPower);
		} else {
			heroPower.processTargetSelectionOverride(context, player);
			rollout(heroPower.play(), context, player, actions);
		}
	}

	private void collectPhysicalAttackActions(GameContext context, Player player, List<GameAction> physicalAttackActions) {
		collectHeroAttackActions(context, player, physicalAttackActions);

		var minions = player.getMinions();
		for (int m = 0, n = minions.size(); m < n; m++) {
			Minion minion = minions.get(m);
			if (!minion.canAttackThisTurn(context)) {
				continue;
			}

			List<PhysicalAttackTargetOverrideAura> filters =
					GameStateArena.getPhysicalAttackTargetOverrideAurasFast(context, minion);
			if (filters != null) {
				Set<EntityReference> common = new LinkedHashSet<>();

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
	}

	private void collectPlayCardActions(GameContext context, Player player, List<GameAction> playCardActions) {
		collectHeroPowerActions(context, player, playCardActions);

		var hand = player.getHand();
		for (int c = 0, n = hand.size(); c < n; c++) {
			Card card = hand.get(c);
			if (!context.getLogic().canPlayCard(player, card)) {
				continue;
			}

			if (card.isChooseOne()) {
				rolloutChooseOnesWithOverrides(context, player, playCardActions, card);
			} else {
				card.processTargetSelectionOverride(context, player);
				rollout(card.play(), context, player, playCardActions);
			}

		}
	}

	private void rolloutChooseOnesWithOverrides(GameContext context, Player player, List<GameAction> playCardActions, Card card) {
		ChooseOneOverride override = context.getLogic().getChooseOneAuraOverrides(player, card);
		if (override == ChooseOneOverride.NONE && card.hasAttribute(Attribute.BOTH_CHOOSE_ONE_OPTIONS)) {
			override = ChooseOneOverride.BOTH_COMBINED;
		}
		switch (override) {
			case BOTH_COMBINED:
				rollout(card.playBothOptions(context), context, player, playCardActions);
				break;
			case ALWAYS_FIRST:
				rollout(card.playOptions(context)[0], context, player, playCardActions);
				break;
			case ALWAYS_SECOND:
				rollout(card.playOptions(context)[1], context, player, playCardActions);
				break;
			case NONE:
			default:
				for (PlayCardAction action : card.playOptions(context)) {
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
	private static boolean tapAllowsZone(net.demilich.metastone.game.spells.desc.TapDesc tap, com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones zone, boolean isPermanent) {
		var zones = tap.getZones();
		if (zones != null) {
			for (var z : zones) {
				if (z == zone) {
					return true;
				}
			}
			return false;
		}
		// Default: PERMANENT cards (locations) tap from BATTLEFIELD, others (Forge) tap from HAND
		if (isPermanent) {
			return zone == com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.BATTLEFIELD;
		} else {
			return zone == com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.HAND;
		}
	}

	private void collectTapActions(GameContext context, Player player, List<GameAction> tapActions) {
		// Minion taps (activated abilities on the battlefield)
		var minions = player.getMinions();
		for (int m = 0, n = minions.size(); m < n; m++) {
			Minion minion = minions.get(m);
			Card sourceCard = minion.getSourceCard();
			var taps = sourceCard.getDesc().getTaps();
			if (taps == null) {
				continue;
			}
			if (minion.getAttributeValue(Attribute.USED_THIS_TURN) > 0) {
				continue;
			}
			boolean isPermanent = minion.hasAttribute(Attribute.PERMANENT);
			for (int i = 0; i < taps.length; i++) {
				var tap = taps[i];
				if (!tapAllowsZone(tap, com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.BATTLEFIELD, isPermanent)) {
					continue;
				}
				if (tap.getCost() > 0 && player.getMana() < tap.getCost()) {
					continue;
				}
				SpellDesc spell = tap.getSpell();
				if (tap.getCondition() != null && !tap.getCondition().create().isFulfilled(context, player, minion, null)) {
					continue;
				}
				TargetSelection targetSelection = tap.getTargetSelection();
				if (targetSelection == null) {
					targetSelection = TargetSelection.NONE;
				}
				TapAction action = new TapAction(spell, minion, minion.getReference(), targetSelection, i, tap.getCost());
				rollout(action, context, player, tapActions);
			}
		}
		// Hand card taps (e.g. Forge)
		var hand = player.getHand();
		for (int c = 0, n = hand.size(); c < n; c++) {
			Card card = hand.get(c);
			var taps = card.getDesc().getTaps();
			if (taps == null) {
				continue;
			}
			boolean isPermanent = card.hasAttribute(Attribute.PERMANENT);
			for (int i = 0; i < taps.length; i++) {
				var tap = taps[i];
				if (!tapAllowsZone(tap, com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.HAND, isPermanent)) {
					continue;
				}
				if (tap.getCost() > 0 && player.getMana() < tap.getCost()) {
					continue;
				}
				SpellDesc spell = tap.getSpell();
				if (tap.getCondition() != null && !tap.getCondition().create().isFulfilled(context, player, card, null)) {
					continue;
				}
				TargetSelection targetSelection = tap.getTargetSelection();
				if (targetSelection == null) {
					targetSelection = TargetSelection.NONE;
				}
				TapAction action = new TapAction(spell, card, card.getReference(), targetSelection, i, tap.getCost());
				rollout(action, context, player, tapActions);
			}
		}
	}

	public List<GameAction> getValidActions(GameContext context, Player player) {
		List<GameAction> validActions = new ArrayList<GameAction>(16);
		collectPhysicalAttackActions(context, player, validActions);
		collectPlayCardActions(context, player, validActions);
		collectTapActions(context, player, validActions);
		if (context.getTurnState() != TurnState.TURN_ENDED) {
			final EndTurnAction endTurnAction = new EndTurnAction(player.getId());
			endTurnAction.setSourceReference(player.getReference());
			validActions.add(endTurnAction);
		}

		// Assign the ids
		for (int i = 0, n = validActions.size(); i < n; i++) {
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
		if (action == null) {
			return;
		}
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
