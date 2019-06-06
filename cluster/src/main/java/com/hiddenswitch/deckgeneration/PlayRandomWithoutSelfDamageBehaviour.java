package com.hiddenswitch.deckgeneration;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public class PlayRandomWithoutSelfDamageBehaviour extends PlayRandomBehaviour {
	boolean canTargetOwnMinions = true;
	boolean canTargetOwnFace = false;

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		this.filterActions(player, validActions);
		return super.requestAction(context, player, validActions);
	}


	public void filterActions(Player player, List<GameAction> validActions) {
		if (!canTargetOwnFace) {
			filterFaceHits(player, validActions);
		}
		if (!canTargetOwnMinions) {
			filterOwnMinionHits(player, validActions);
		}
	}

	public void filterFaceHits(Player player, List<GameAction> validActions) {
		validActions.removeIf(action -> {
			if (!(checkIfDamageSpell(action) || checkIfDamageBattlecry(action))) {
				return false;
			}
			boolean targetsFriendlyHero = action.getTargetReference() != null && action.getTargetReference().equals(player.getHero().getReference());
			return targetsFriendlyHero;
		});
	}

	public void filterOwnMinionHits(Player player, List<GameAction> validActions) {
		validActions.removeIf(action -> {
			if (!(checkIfDamageSpell(action) || checkIfDamageBattlecry(action))) {
				return false;
			}
			EntityZone<Minion> minions = player.getMinions();
			boolean spellHasTarget = action.getTargetReference() != null;
			boolean targetsFriendlyMinion = false;
			if (spellHasTarget) {
				for (Minion minion : minions) {
					if (action.getTargetReference().equals(minion.getReference())) {
						targetsFriendlyMinion = true;
						break;
					}
				}
			}
			return targetsFriendlyMinion;
		});
	}

	public boolean checkIfDamageSpell(GameAction action) {
		if (!(action instanceof PlaySpellCardAction)) {
			return false;
		}

		PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
		SpellDesc spellDesc = spellCardAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return DamageSpell.class.isAssignableFrom(spellDesc.getDescClass());
	}

	public boolean checkIfDamageBattlecry(GameAction action) {
		if (!(action instanceof BattlecryAction)) {
			return false;
		}
		BattlecryAction battlecryAction = (BattlecryAction) action;
		SpellDesc spellDesc = battlecryAction.getSpell();
		if (spellDesc == null) {
			return false;
		}

		return DamageSpell.class.isAssignableFrom((spellDesc.getDescClass()));
	}

	public void ownMinionTargetingIsEnabled(boolean canTargetOwnMinions) {
		this.canTargetOwnMinions = canTargetOwnMinions;
	}
}
