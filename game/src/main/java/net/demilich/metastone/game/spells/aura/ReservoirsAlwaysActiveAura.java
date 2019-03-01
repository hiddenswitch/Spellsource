package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When this aura is in play, the {@link net.demilich.metastone.game.spells.desc.condition.ReservoirCondition} always
 * evaluates to {@code true} and {@link net.demilich.metastone.game.spells.ReservoirSpell} always include their bonus
 * effects.
 */
public final class ReservoirsAlwaysActiveAura extends Aura {

	public ReservoirsAlwaysActiveAura(AuraDesc desc) {
		super(desc);
		setDesc(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}
}

