package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.TargetAcquisitionEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.TargetAcquisitionTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class NoggenfoggerAura extends Aura {

	public NoggenfoggerAura(AuraDesc desc) {
		super(new TargetAcquisitionTrigger(), null, NullSpell.create());
		setDesc(desc);
	}

	@Override
	public void onGameEvent(GameEvent originalEvent) {
		TargetAcquisitionEvent event = (TargetAcquisitionEvent) originalEvent;
		if (event.getAction() == null) {
			throw new NullPointerException("All target acquisition events now should come in with valid actions.");
		}

		GameContext gc = originalEvent.getGameContext();
		List<Entity> validTargets;

		if (event.getAction().getActionType() == ActionType.PHYSICAL_ATTACK) {
			// Noggenfogger ignores taunt and stealth but doesn't choose friendlies
			Player sourcePlayer = gc.getPlayer(originalEvent.getSourcePlayerId());
			validTargets = gc.resolveTarget(sourcePlayer, event.getAction().getSource(gc), EntityReference.ENEMY_CHARACTERS);
		} else {
			validTargets = gc.getLogic().getValidTargets(event.getSourcePlayerId(), event.getAction());
		}

		if (validTargets.size() == 0) {
			// An earlier event removed all valid targets
			return;
		}
		gc.setTargetOverride(gc.getLogic().getRandom(validTargets).getReference());
	}
}
