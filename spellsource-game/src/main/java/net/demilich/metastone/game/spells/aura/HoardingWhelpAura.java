package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * "Both players" now only affects the friendly player.
 *
 * @see net.demilich.metastone.game.spells.BothPlayersSpell for how this interacts with cards
 */
public final class HoardingWhelpAura extends AbstractFriendlyCardAura {

	public HoardingWhelpAura(AuraDesc desc) {
		super(desc);
	}
}
