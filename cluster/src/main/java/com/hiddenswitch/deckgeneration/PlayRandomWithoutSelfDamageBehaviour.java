package com.hiddenswitch.deckgeneration;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public class PlayRandomWithoutSelfDamageBehaviour extends PlayRandomBehaviour {
	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		this.filterActions(player, validActions);
		return super.requestAction(context, player, validActions);
	}


	public void filterActions(Player player, List<GameAction> validActions) {
		validActions.removeIf(action -> {
			if (!(action instanceof PlaySpellCardAction)) {
				return false;
			}

			PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
			SpellDesc spellDesc = spellCardAction.getSpell();
			if (spellDesc == null) {
				return false;
			}

			boolean isDamageSpell = DamageSpell.class.isAssignableFrom(spellDesc.getDescClass());
			boolean targetsFriendlyHero = action.getTargetReference() != null && action.getTargetReference().equals(player.getHero().getReference());
			return isDamageSpell && targetsFriendlyHero;
		});
	}
}
