package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When in play, cards affected by this aura will also give the lifedrain healing bonus to {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#SECONDARY_TARGET} entities (excluding the entity that normally
 * receives the healing).
 */
public final class LifedrainHealsAdditionalAura extends Aura {

	public LifedrainHealsAdditionalAura(AuraDesc desc) {
		super(desc);
	}
}
