package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.TargetAcquisitionEvent;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

import java.util.Collections;
import java.util.List;

/**
 * The Capt'n Cookish aura changes to random the player selected targets of spells cast by the owner of the aura. Since
 * it changes targeting randomly, it is considered a kind of Noggenfogger aura.
 */
public final class CaptnCookishAura extends NoggenfoggerAura {

	public CaptnCookishAura(AuraDesc desc) {
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

