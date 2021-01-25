package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When this aura is in play, spells with minion targets also target adjacent minions.
 */
public class SpellTargetsAdjacentAura extends AbstractFriendlyCardAura {

	public SpellTargetsAdjacentAura(AuraDesc desc) {
		super(desc);
	}
}
