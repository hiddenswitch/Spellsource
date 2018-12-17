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
 * The Capt'n Cookie aura changes to random the player selected targets of spells cast by the owner of the aura. Since
 * it changes targeting randomly, it is considered a kind of Noggenfogger aura.
 */
public final class CaptnCookieAura extends NoggenfoggerAura {

	private static final long serialVersionUID = -4573949372353008326L;

	public CaptnCookieAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	protected List<Entity> getValidTargets(GameContext context, TargetAcquisitionEvent event) {
		if (event.getActionType() == ActionType.SPELL && event.getSourcePlayerId() == getOwner()) {
			return super.getValidTargets(context, event);
		}
		return Collections.emptyList();
	}
}
