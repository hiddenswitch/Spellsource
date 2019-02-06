package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When this aura is in play, {@link net.demilich.metastone.game.spells.ReservoirSpell} effects are never activated and
 * {@link net.demilich.metastone.game.spells.desc.condition.ReservoirCondition} always evalutes to {@code false}.
 * <p>
 * Implements N'Zoth's Battlemaiden.
 */
public final class ReservoirsNeverActiveAura extends Aura {

	public ReservoirsNeverActiveAura(AuraDesc desc) {
		super(desc);
		setDesc(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}
}
