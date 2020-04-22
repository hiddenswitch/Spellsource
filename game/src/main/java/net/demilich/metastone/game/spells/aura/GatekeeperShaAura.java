package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Gives {@link net.demilich.metastone.game.spells.desc.valueprovider.GatekeeperShaValueProvider} the amount to increase
 * the numbers written on spells by.
 */
public final class GatekeeperShaAura extends AbstractFriendlyCardAura {

	public GatekeeperShaAura(AuraDesc desc) {
		super(desc);
	}

	/**
	 * The numbers increase specified on this card.
	 *
	 * @return The {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#VALUE}.
	 */
	public int getNumbersIncrease() {
		return getDesc().getValue();
	}
}
