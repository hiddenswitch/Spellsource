package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.TargetAcquisitionEvent;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * The Capt'n Cookie aura is a kind of Noggenfogger aura.
 */
public final class CaptnCookieAura extends NoggenfoggerAura {

	public CaptnCookieAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	protected List<Entity> getValidTargets(GameContext context, TargetAcquisitionEvent event) {
		if (event.getActionType() == ActionType.SPELL) {
			return super.getValidTargets(context, event);
		}
		return Collections.emptyList();
	}
}
